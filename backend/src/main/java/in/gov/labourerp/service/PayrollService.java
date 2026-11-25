package in.gov.labourerp.service;

import in.gov.labourerp.dto.payroll.PayrollGenerateRequest;
import in.gov.labourerp.dto.payroll.PayrollItemDto;
import in.gov.labourerp.dto.payroll.PayrollRunDto;
import in.gov.labourerp.entity.*;
import in.gov.labourerp.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PayrollService {

    private final PayrollRunRepository payrollRunRepository;
    private final PayrollItemRepository payrollItemRepository;
    private final BusinessRepository businessRepository;
    private final SiteRepository siteRepository;
    private final LabourerRepository labourerRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final AdvanceRepository advanceRepository;
    private final AdvanceRecoveryRepository advanceRecoveryRepository;
    private final PaymentRepository paymentRepository;
    private final BusinessSettingRepository businessSettingRepository;
    private final AuditLogService auditLogService;

    public PayrollService(PayrollRunRepository payrollRunRepository,
                          PayrollItemRepository payrollItemRepository,
                          BusinessRepository businessRepository,
                          SiteRepository siteRepository,
                          LabourerRepository labourerRepository,
                          AttendanceRecordRepository attendanceRecordRepository,
                          AdvanceRepository advanceRepository,
                          AdvanceRecoveryRepository advanceRecoveryRepository,
                          PaymentRepository paymentRepository,
                          BusinessSettingRepository businessSettingRepository,
                          AuditLogService auditLogService) {
        this.payrollRunRepository = payrollRunRepository;
        this.payrollItemRepository = payrollItemRepository;
        this.businessRepository = businessRepository;
        this.siteRepository = siteRepository;
        this.labourerRepository = labourerRepository;
        this.attendanceRecordRepository = attendanceRecordRepository;
        this.advanceRepository = advanceRepository;
        this.advanceRecoveryRepository = advanceRecoveryRepository;
        this.paymentRepository = paymentRepository;
        this.businessSettingRepository = businessSettingRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public Page<PayrollRunDto> getPayrollRuns(Long businessId, Integer year, Integer month, Long siteId, Pageable pageable) {
        Page<PayrollRun> page = payrollRunRepository.findByFilters(businessId, year, month, siteId, pageable);
        return page.map(this::mapRunToDto);
    }

    @Transactional(readOnly = true)
    public PayrollRunDto getPayrollRunById(Long id) {
        PayrollRun run = payrollRunRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payroll run not found with ID: " + id));
        PayrollRunDto dto = mapRunToDto(run);

        List<PayrollItem> items = payrollItemRepository.findByPayrollRunId(run.getId());
        dto.setItems(items.stream().map(this::mapItemToDto).collect(Collectors.toList()));
        return dto;
    }

    @Transactional
    public PayrollRunDto generatePayroll(PayrollGenerateRequest request, String username, String ipAddress) {
        Business business = businessRepository.findById(request.getBusinessId())
                .orElseThrow(() -> new RuntimeException("Business not found with ID: " + request.getBusinessId()));

        final Site targetSite = (request.getSiteId() != null)
                ? siteRepository.findById(request.getSiteId()).orElse(null)
                : null;

        YearMonth ym = YearMonth.of(request.getYear(), request.getMonth());
        LocalDate startDate = request.getStartDate() != null ? request.getStartDate() : ym.atDay(1);
        LocalDate endDate = request.getEndDate() != null ? request.getEndDate() : ym.atEndOfMonth();

        String runCode = "PAYROLL-" + ym.getYear() + "-" + String.format("%02d", ym.getMonthValue())
                + (targetSite != null ? "-" + targetSite.getSiteCode() : "-ALL");

        PayrollRun run = payrollRunRepository.findByBusinessIdAndRunCode(business.getId(), runCode)
                .orElseGet(() -> {
                    PayrollRun newRun = new PayrollRun();
                    newRun.setBusiness(business);
                    newRun.setSite(targetSite);
                    newRun.setRunCode(runCode);
                    newRun.setPeriodMonth(request.getMonth());
                    newRun.setPeriodYear(request.getYear());
                    newRun.setStartDate(startDate);
                    newRun.setEndDate(endDate);
                    newRun.setStatus("GENERATED");
                    newRun.setGeneratedBy(username);
                    return newRun;
                });

        run.setStartDate(startDate);
        run.setEndDate(endDate);
        run.setStatus("GENERATED");
        run.setGeneratedBy(username);
        PayrollRun savedRun = payrollRunRepository.save(run);

        // Fetch candidate labourers
        List<Labourer> candidateLabourers = (targetSite != null)
                ? labourerRepository.findByBusinessIdAndCurrentSiteId(business.getId(), targetSite.getId())
                : labourerRepository.findByBusinessId(business.getId());

        BigDecimal totalGross = BigDecimal.ZERO;
        BigDecimal totalAdvanceDeductions = BigDecimal.ZERO;
        BigDecimal totalNetPayable = BigDecimal.ZERO;
        BigDecimal totalPaid = BigDecimal.ZERO;
        BigDecimal totalRemaining = BigDecimal.ZERO;
        int workerCount = 0;

        List<PayrollItemDto> itemDtos = new ArrayList<>();

        for (Labourer labourer : candidateLabourers) {
            // Find attendance in the period
            List<AttendanceRecord> attendanceRecords = attendanceRecordRepository
                    .findByLabourerIdAndAttendanceDateBetweenOrderByAttendanceDateAsc(labourer.getId(), startDate, endDate);

            BigDecimal presentDays = BigDecimal.ZERO;
            BigDecimal halfDays = BigDecimal.ZERO;
            BigDecimal absentDays = BigDecimal.ZERO;
            BigDecimal paidLeaves = BigDecimal.ZERO;
            BigDecimal weeklyOffs = BigDecimal.ZERO;
            BigDecimal holidays = BigDecimal.ZERO;
            BigDecimal totalOtHours = BigDecimal.ZERO;
            BigDecimal totalOtAmount = BigDecimal.ZERO;
            BigDecimal baseEarnings = BigDecimal.ZERO;

            for (AttendanceRecord ar : attendanceRecords) {
                switch (ar.getStatus().toUpperCase()) {
                    case "PRESENT":
                        presentDays = presentDays.add(BigDecimal.ONE);
                        break;
                    case "HALF_DAY":
                        halfDays = halfDays.add(BigDecimal.ONE);
                        break;
                    case "ABSENT":
                        absentDays = absentDays.add(BigDecimal.ONE);
                        break;
                    case "PAID_LEAVE":
                        paidLeaves = paidLeaves.add(BigDecimal.ONE);
                        break;
                    case "WEEKLY_OFF":
                        weeklyOffs = weeklyOffs.add(BigDecimal.ONE);
                        break;
                    case "HOLIDAY":
                        holidays = holidays.add(BigDecimal.ONE);
                        break;
                }
                totalOtHours = totalOtHours.add(ar.getOvertimeHours());
                totalOtAmount = totalOtAmount.add(ar.getOvertimeAmount());
                baseEarnings = baseEarnings.add(ar.getCalculatedWage());
            }

            BigDecimal grossEarnings = baseEarnings.add(totalOtAmount).setScale(2, RoundingMode.HALF_UP);

            // Compute advance recovery
            BigDecimal outstandingAdvance = advanceRepository.calculateOutstandingAdvanceByLabourer(labourer.getId());
            if (outstandingAdvance == null) outstandingAdvance = BigDecimal.ZERO;

            // Configurable advance recovery % (default 35% of gross earnings)
            BigDecimal maxRecoveryPct = getAdvanceRecoveryPercentage(business.getId());
            BigDecimal maxDeductible = grossEarnings.multiply(maxRecoveryPct).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            BigDecimal advanceRecovery = outstandingAdvance.min(maxDeductible);

            BigDecimal netPayable = grossEarnings.subtract(advanceRecovery).setScale(2, RoundingMode.HALF_UP);

            // Payments already disbursed in this window
            BigDecimal paymentsMade = paymentRepository.calculateTotalPaymentsByLabourer(labourer.getId(), startDate, endDate);
            if (paymentsMade == null) paymentsMade = BigDecimal.ZERO;

            BigDecimal balanceRemaining = netPayable.subtract(paymentsMade).setScale(2, RoundingMode.HALF_UP);

            // Create or update PayrollItem
            PayrollItem item = payrollItemRepository.findByPayrollRunIdAndLabourerId(savedRun.getId(), labourer.getId())
                    .orElse(new PayrollItem());

            item.setPayrollRun(savedRun);
            item.setLabourer(labourer);
            item.setDailyWageRate(labourer.getDailyWage());
            item.setPresentDays(presentDays);
            item.setHalfDays(halfDays);
            item.setAbsentDays(absentDays);
            item.setPaidLeaveDays(paidLeaves);
            item.setWeeklyOffDays(weeklyOffs);
            item.setHolidayDays(holidays);
            item.setOvertimeHours(totalOtHours);
            item.setOvertimeAmount(totalOtAmount);
            item.setBaseEarnings(baseEarnings);
            item.setGrossEarnings(grossEarnings);
            item.setAdvanceRecovery(advanceRecovery);
            item.setOtherDeductions(BigDecimal.ZERO);
            item.setNetPayable(netPayable);
            item.setAmountPaid(paymentsMade);
            item.setRemainingBalance(balanceRemaining);
            item.setStatus(balanceRemaining.compareTo(BigDecimal.ZERO) <= 0 ? "PAID" : (paymentsMade.compareTo(BigDecimal.ZERO) > 0 ? "PARTIALLY_PAID" : "UNPAID"));

            PayrollItem savedItem = payrollItemRepository.save(item);
            itemDtos.add(mapItemToDto(savedItem));

            totalGross = totalGross.add(grossEarnings);
            totalAdvanceDeductions = totalAdvanceDeductions.add(advanceRecovery);
            totalNetPayable = totalNetPayable.add(netPayable);
            totalPaid = totalPaid.add(paymentsMade);
            totalRemaining = totalRemaining.add(balanceRemaining);
            workerCount++;
        }

        savedRun.setTotalWorkers(workerCount);
        savedRun.setTotalGrossEarnings(totalGross);
        savedRun.setTotalAdvanceDeductions(totalAdvanceDeductions);
        savedRun.setTotalNetPayable(totalNetPayable);
        savedRun.setTotalPaidAmount(totalPaid);
        savedRun.setTotalOutstandingBalance(totalRemaining);

        PayrollRun finalRun = payrollRunRepository.save(savedRun);

        auditLogService.logAction(
                business.getId(), null, username, "PAYROLL_GENERATE", "PAYROLL",
                finalRun.getId().toString(), null,
                "Generated periodic payroll (" + finalRun.getRunCode() + ") for " + workerCount + " workers. Net: ₹" + totalNetPayable,
                ipAddress
        );

        PayrollRunDto res = mapRunToDto(finalRun);
        res.setItems(itemDtos);
        return res;
    }

    private BigDecimal getAdvanceRecoveryPercentage(Long businessId) {
        Optional<BusinessSetting> bsOpt = businessSettingRepository.findByBusinessIdAndSettingKey(businessId, "ADVANCE_MAX_RECOVERY_PERCENT");
        if (bsOpt.isPresent()) {
            try {
                return new BigDecimal(bsOpt.get().getSettingValue());
            } catch (Exception e) {}
        }
        return new BigDecimal("35.00");
    }

    private PayrollRunDto mapRunToDto(PayrollRun r) {
        PayrollRunDto dto = new PayrollRunDto();
        dto.setId(r.getId());
        dto.setBusinessId(r.getBusiness().getId());
        if (r.getSite() != null) {
            dto.setSiteId(r.getSite().getId());
            dto.setSiteName(r.getSite().getSiteName());
        }
        dto.setRunCode(r.getRunCode());
        dto.setPeriodMonth(r.getPeriodMonth());
        dto.setPeriodYear(r.getPeriodYear());
        dto.setStartDate(r.getStartDate());
        dto.setEndDate(r.getEndDate());
        dto.setStatus(r.getStatus());
        dto.setTotalWorkers(r.getTotalWorkers());
        dto.setTotalGrossEarnings(r.getTotalGrossEarnings());
        dto.setTotalAdvanceDeductions(r.getTotalAdvanceDeductions());
        dto.setTotalOtherDeductions(r.getTotalOtherDeductions());
        dto.setTotalNetPayable(r.getTotalNetPayable());
        dto.setTotalPaidAmount(r.getTotalPaidAmount());
        dto.setTotalOutstandingBalance(r.getTotalOutstandingBalance());
        dto.setGeneratedBy(r.getGeneratedBy());
        dto.setApprovedBy(r.getApprovedBy());
        dto.setCreatedAt(r.getCreatedAt());
        return dto;
    }

    private PayrollItemDto mapItemToDto(PayrollItem i) {
        PayrollItemDto dto = new PayrollItemDto();
        dto.setId(i.getId());
        dto.setPayrollRunId(i.getPayrollRun().getId());
        dto.setLabourerId(i.getLabourer().getId());
        dto.setLabourCode(i.getLabourer().getLabourCode());
        dto.setLabourName(i.getLabourer().getFullName());
        dto.setDesignation(i.getLabourer().getDesignation());
        dto.setLabourCategory(i.getLabourer().getLabourCategory());
        dto.setDailyWageRate(i.getDailyWageRate());
        dto.setPresentDays(i.getPresentDays());
        dto.setHalfDays(i.getHalfDays());
        dto.setAbsentDays(i.getAbsentDays());
        dto.setPaidLeaveDays(i.getPaidLeaveDays());
        dto.setWeeklyOffDays(i.getWeeklyOffDays());
        dto.setHolidayDays(i.getHolidayDays());
        dto.setOvertimeHours(i.getOvertimeHours());
        dto.setOvertimeAmount(i.getOvertimeAmount());
        dto.setBaseEarnings(i.getBaseEarnings());
        dto.setGrossEarnings(i.getGrossEarnings());
        dto.setAdvanceRecovery(i.getAdvanceRecovery());
        dto.setOtherDeductions(i.getOtherDeductions());
        dto.setNetPayable(i.getNetPayable());
        dto.setAmountPaid(i.getAmountPaid());
        dto.setRemainingBalance(i.getRemainingBalance());
        dto.setStatus(i.getStatus());
        dto.setRemarks(i.getRemarks());
        return dto;
    }
}
