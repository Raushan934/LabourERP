package in.gov.labourerp.service;

import in.gov.labourerp.dto.attendance.AttendanceCorrectionRequest;
import in.gov.labourerp.dto.attendance.AttendanceRecordDto;
import in.gov.labourerp.dto.attendance.BulkAttendanceRequest;
import in.gov.labourerp.entity.*;
import in.gov.labourerp.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AttendanceService {

    private final AttendanceRecordRepository attendanceRecordRepository;
    private final AttendanceCorrectionRepository correctionRepository;
    private final BusinessRepository businessRepository;
    private final SiteRepository siteRepository;
    private final LabourerRepository labourerRepository;
    private final MasterValueRepository masterValueRepository;
    private final BusinessSettingRepository businessSettingRepository;
    private final AuditLogService auditLogService;

    public AttendanceService(AttendanceRecordRepository attendanceRecordRepository,
                             AttendanceCorrectionRepository correctionRepository,
                             BusinessRepository businessRepository,
                             SiteRepository siteRepository,
                             LabourerRepository labourerRepository,
                             MasterValueRepository masterValueRepository,
                             BusinessSettingRepository businessSettingRepository,
                             AuditLogService auditLogService) {
        this.attendanceRecordRepository = attendanceRecordRepository;
        this.correctionRepository = correctionRepository;
        this.businessRepository = businessRepository;
        this.siteRepository = siteRepository;
        this.labourerRepository = labourerRepository;
        this.masterValueRepository = masterValueRepository;
        this.businessSettingRepository = businessSettingRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public List<AttendanceRecordDto> getAttendanceForSiteAndDate(Long businessId, Long siteId, LocalDate date) {
        List<AttendanceRecord> records = attendanceRecordRepository.findBySiteIdAndAttendanceDate(siteId, date);
        return records.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Transactional
    public List<AttendanceRecordDto> saveBulkAttendance(BulkAttendanceRequest request, String username, String ipAddress) {
        Business business = businessRepository.findById(request.getBusinessId())
                .orElseThrow(() -> new RuntimeException("Business not found with ID: " + request.getBusinessId()));

        Site site = siteRepository.findById(request.getSiteId())
                .orElseThrow(() -> new RuntimeException("Site not found with ID: " + request.getSiteId()));

        List<AttendanceRecordDto> resultList = new ArrayList<>();

        for (BulkAttendanceRequest.AttendanceItem item : request.getItems()) {
            Labourer labourer = labourerRepository.findById(item.getLabourerId())
                    .orElseThrow(() -> new RuntimeException("Labourer not found with ID: " + item.getLabourerId()));

            Optional<AttendanceRecord> existingOpt = attendanceRecordRepository
                    .findBySiteIdAndLabourerIdAndAttendanceDate(site.getId(), labourer.getId(), request.getAttendanceDate());

            AttendanceRecord record = existingOpt.orElseGet(() -> {
                AttendanceRecord newRec = new AttendanceRecord();
                newRec.setBusiness(business);
                newRec.setSite(site);
                newRec.setLabourer(labourer);
                newRec.setAttendanceDate(request.getAttendanceDate());
                return newRec;
            });

            record.setStatus(item.getStatus().toUpperCase());
            BigDecimal otHours = item.getOvertimeHours() != null ? item.getOvertimeHours() : BigDecimal.ZERO;
            record.setOvertimeHours(otHours);
            record.setRemarks(item.getRemarks());
            record.setMarkedBy(username);

            // Compute dynamic wage multiplier based on status and configurable master value
            BigDecimal multiplier = calculateWageMultiplier(business.getId(), record.getStatus());
            record.setWageMultiplier(multiplier);
            record.setDailyWageApplied(labourer.getDailyWage());

            // calculated wage = dailyWage * multiplier
            BigDecimal calculatedWage = labourer.getDailyWage().multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
            record.setCalculatedWage(calculatedWage);

            // overtime amount = overtimeHours * overtimeHourlyRate
            BigDecimal otAmount = otHours.multiply(labourer.getOvertimeHourlyRate()).setScale(2, RoundingMode.HALF_UP);
            record.setOvertimeAmount(otAmount);

            AttendanceRecord saved = attendanceRecordRepository.save(record);
            resultList.add(mapToDto(saved));
        }

        auditLogService.logAction(
                business.getId(), null, username, "BULK_SAVE", "ATTENDANCE",
                site.getId() + "_" + request.getAttendanceDate(), null,
                "Saved attendance roster for site: " + site.getSiteName() + " on " + request.getAttendanceDate() + " (" + request.getItems().size() + " records)",
                ipAddress
        );

        return resultList;
    }

    @Transactional
    public AttendanceRecordDto correctAttendance(AttendanceCorrectionRequest request, String username, String ipAddress) {
        AttendanceRecord record = attendanceRecordRepository.findById(request.getAttendanceRecordId())
                .orElseThrow(() -> new RuntimeException("Attendance record not found with ID: " + request.getAttendanceRecordId()));

        String prevStatus = record.getStatus();
        BigDecimal prevOt = record.getOvertimeHours();
        BigDecimal prevWage = record.getCalculatedWage();

        record.setStatus(request.getNewStatus().toUpperCase());
        BigDecimal newOt = request.getNewOvertimeHours() != null ? request.getNewOvertimeHours() : BigDecimal.ZERO;
        record.setOvertimeHours(newOt);

        // Recompute multiplier and wages
        BigDecimal multiplier = calculateWageMultiplier(record.getBusiness().getId(), record.getStatus());
        record.setWageMultiplier(multiplier);

        BigDecimal newWage = record.getDailyWageApplied().multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
        record.setCalculatedWage(newWage);

        BigDecimal otAmount = newOt.multiply(record.getLabourer().getOvertimeHourlyRate()).setScale(2, RoundingMode.HALF_UP);
        record.setOvertimeAmount(otAmount);

        AttendanceRecord updated = attendanceRecordRepository.save(record);

        // Create immutable AttendanceCorrection audit trail
        AttendanceCorrection correction = new AttendanceCorrection(
                updated, prevStatus, updated.getStatus(),
                prevOt, updated.getOvertimeHours(),
                prevWage, updated.getCalculatedWage(),
                request.getReason(), username
        );
        correctionRepository.save(correction);

        auditLogService.logAction(
                record.getBusiness().getId(), null, username, "CORRECTION", "ATTENDANCE",
                record.getId().toString(),
                "Status: " + prevStatus + ", OT: " + prevOt + ", Wage: " + prevWage,
                "Status: " + updated.getStatus() + ", OT: " + newOt + ", Wage: " + newWage + " | Reason: " + request.getReason(),
                ipAddress
        );

        return mapToDto(updated);
    }

    public BigDecimal calculateWageMultiplier(Long businessId, String status) {
        switch (status.toUpperCase()) {
            case "PRESENT":
            case "PAID_LEAVE":
            case "WEEKLY_OFF":
            case "HOLIDAY":
                return BigDecimal.ONE;
            case "HALF_DAY":
                // Check if business has custom setting for half day percentage
                Optional<BusinessSetting> bsOpt = businessSettingRepository.findByBusinessIdAndSettingKey(businessId, "HALF_DAY_PERCENT");
                if (bsOpt.isPresent()) {
                    try {
                        BigDecimal pct = new BigDecimal(bsOpt.get().getSettingValue());
                        return pct.divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                    } catch (Exception e) {}
                }
                return new BigDecimal("0.50");
            case "ABSENT":
            default:
                return BigDecimal.ZERO;
        }
    }

    private AttendanceRecordDto mapToDto(AttendanceRecord r) {
        AttendanceRecordDto dto = new AttendanceRecordDto();
        dto.setId(r.getId());
        dto.setBusinessId(r.getBusiness().getId());
        dto.setSiteId(r.getSite().getId());
        dto.setSiteName(r.getSite().getSiteName());
        dto.setLabourerId(r.getLabourer().getId());
        dto.setLabourCode(r.getLabourer().getLabourCode());
        dto.setLabourName(r.getLabourer().getFullName());
        dto.setDesignation(r.getLabourer().getDesignation());
        dto.setLabourCategory(r.getLabourer().getLabourCategory());
        dto.setAttendanceDate(r.getAttendanceDate());
        dto.setStatus(r.getStatus());
        dto.setOvertimeHours(r.getOvertimeHours());
        dto.setWageMultiplier(r.getWageMultiplier());
        dto.setDailyWageApplied(r.getDailyWageApplied());
        dto.setCalculatedWage(r.getCalculatedWage());
        dto.setOvertimeAmount(r.getOvertimeAmount());
        dto.setRemarks(r.getRemarks());
        dto.setMarkedBy(r.getMarkedBy());
        return dto;
    }
}
