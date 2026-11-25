package in.gov.labourerp.service;

import in.gov.labourerp.dto.labour.LabourerDto;
import in.gov.labourerp.entity.*;
import in.gov.labourerp.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LabourService {

    private final LabourerRepository labourerRepository;
    private final BusinessRepository businessRepository;
    private final SiteRepository siteRepository;
    private final WageRateHistoryRepository wageRateHistoryRepository;
    private final AdvanceRepository advanceRepository;
    private final CustomFieldService customFieldService;
    private final AuditLogService auditLogService;

    public LabourService(LabourerRepository labourerRepository,
                         BusinessRepository businessRepository,
                         SiteRepository siteRepository,
                         WageRateHistoryRepository wageRateHistoryRepository,
                         AdvanceRepository advanceRepository,
                         CustomFieldService customFieldService,
                         AuditLogService auditLogService) {
        this.labourerRepository = labourerRepository;
        this.businessRepository = businessRepository;
        this.siteRepository = siteRepository;
        this.wageRateHistoryRepository = wageRateHistoryRepository;
        this.advanceRepository = advanceRepository;
        this.customFieldService = customFieldService;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public Page<LabourerDto> getLabourers(Long businessId, Long siteId, String category,
                                          String status, String search, Pageable pageable) {
        String searchParam = (search != null && !search.trim().isEmpty()) ? "%" + search.toLowerCase().trim() + "%" : null;
        String catParam = (category != null && !category.trim().isEmpty()) ? category.trim() : null;
        String statusParam = (status != null && !status.trim().isEmpty()) ? status.trim() : null;
        Page<Labourer> page = labourerRepository.findByFilters(businessId, siteId, catParam, statusParam, searchParam, pageable);
        return page.map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public List<LabourerDto> getActiveLabourersBySite(Long businessId, Long siteId) {
        return labourerRepository.findByBusinessIdAndCurrentSiteId(businessId, siteId).stream()
                .filter(l -> "ACTIVE".equalsIgnoreCase(l.getStatus()))
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public LabourerDto getLabourerById(Long id) {
        Labourer labourer = labourerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Labourer not found with ID: " + id));
        LabourerDto dto = mapToDto(labourer);
        dto.setCustomFields(customFieldService.getEntityFieldValues(labourer.getId()));
        return dto;
    }

    @Transactional
    public LabourerDto createLabourer(LabourerDto dto, String username, String ipAddress) {
        Business business = businessRepository.findById(dto.getBusinessId())
                .orElseThrow(() -> new RuntimeException("Business not found with ID: " + dto.getBusinessId()));

        if (labourerRepository.existsByBusinessIdAndLabourCode(dto.getBusinessId(), dto.getLabourCode())) {
            throw new RuntimeException("Labour code already exists: " + dto.getLabourCode());
        }

        Labourer labourer = new Labourer();
        labourer.setBusiness(business);
        labourer.setLabourCode(dto.getLabourCode().toUpperCase().trim());
        labourer.setFullName(dto.getFullName().trim());
        labourer.setPhotoUrl(dto.getPhotoUrl());
        labourer.setMobileNumber(dto.getMobileNumber().trim());
        labourer.setAltMobileNumber(dto.getAltMobileNumber());
        labourer.setAadharNumber(dto.getAadharNumber());
        labourer.setAddress(dto.getAddress());
        labourer.setEmergencyContactName(dto.getEmergencyContactName());
        labourer.setEmergencyContactPhone(dto.getEmergencyContactPhone());
        labourer.setDateOfJoining(dto.getDateOfJoining());
        labourer.setDesignation(dto.getDesignation());
        labourer.setLabourCategory(dto.getLabourCategory());
        labourer.setSkillLevel(dto.getSkillLevel() != null ? dto.getSkillLevel() : "SKILLED");
        labourer.setDailyWage(dto.getDailyWage());
        labourer.setOvertimeHourlyRate(dto.getOvertimeHourlyRate() != null ? dto.getOvertimeHourlyRate() : BigDecimal.ZERO);

        if (dto.getCurrentSiteId() != null) {
            siteRepository.findById(dto.getCurrentSiteId()).ifPresent(labourer::setCurrentSite);
        }

        labourer.setStatus(dto.getStatus() != null ? dto.getStatus() : "ACTIVE");
        labourer.setUpiId(dto.getUpiId());
        labourer.setBankAccountNumber(dto.getBankAccountNumber());
        labourer.setBankIfscCode(dto.getBankIfscCode());
        labourer.setBankName(dto.getBankName());
        labourer.setBankBeneficiaryName(dto.getBankBeneficiaryName());
        labourer.setQrCodeUrl(dto.getQrCodeUrl());
        labourer.setNotes(dto.getNotes());
        labourer.setCreatedBy(username);

        ensureSpecificUpiAndQr(labourer);

        Labourer saved = labourerRepository.save(labourer);

        // Record initial wage rate history
        WageRateHistory wrh = new WageRateHistory(
                saved, saved.getDailyWage(), saved.getOvertimeHourlyRate(),
                saved.getDateOfJoining(), "Initial joining wage configuration", username
        );
        wageRateHistoryRepository.save(wrh);

        // Save custom fields
        if (dto.getCustomFields() != null && !dto.getCustomFields().isEmpty()) {
            customFieldService.saveEntityFieldValues(dto.getBusinessId(), "LABOURER", saved.getId(), dto.getCustomFields());
        }

        auditLogService.logAction(
                dto.getBusinessId(), null, username, "CREATE", "LABOURER",
                saved.getId().toString(), null, "Created labour profile: " + saved.getFullName() + " (" + saved.getLabourCode() + ")", ipAddress
        );

        return mapToDto(saved);
    }

    @Transactional
    public LabourerDto updateLabourer(Long id, LabourerDto dto, String username, String ipAddress) {
        Labourer labourer = labourerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Labourer not found with ID: " + id));

        // Check if daily wage or overtime rate changed to preserve wage rate history
        boolean wageChanged = !labourer.getDailyWage().equals(dto.getDailyWage()) ||
                              !labourer.getOvertimeHourlyRate().equals(dto.getOvertimeHourlyRate());

        labourer.setFullName(dto.getFullName());
        labourer.setPhotoUrl(dto.getPhotoUrl());
        labourer.setMobileNumber(dto.getMobileNumber());
        labourer.setAltMobileNumber(dto.getAltMobileNumber());
        labourer.setAadharNumber(dto.getAadharNumber());
        labourer.setAddress(dto.getAddress());
        labourer.setEmergencyContactName(dto.getEmergencyContactName());
        labourer.setEmergencyContactPhone(dto.getEmergencyContactPhone());
        labourer.setDesignation(dto.getDesignation());
        labourer.setLabourCategory(dto.getLabourCategory());
        labourer.setSkillLevel(dto.getSkillLevel());

        if (wageChanged) {
            WageRateHistory wrh = new WageRateHistory(
                    labourer, dto.getDailyWage(), dto.getOvertimeHourlyRate(),
                    LocalDate.now(), "Wage rate updated by admin/manager", username
            );
            wageRateHistoryRepository.save(wrh);
            labourer.setDailyWage(dto.getDailyWage());
            labourer.setOvertimeHourlyRate(dto.getOvertimeHourlyRate());
        }

        if (dto.getCurrentSiteId() != null) {
            siteRepository.findById(dto.getCurrentSiteId()).ifPresent(labourer::setCurrentSite);
        } else {
            labourer.setCurrentSite(null);
        }

        labourer.setStatus(dto.getStatus());
        labourer.setUpiId(dto.getUpiId());
        labourer.setBankAccountNumber(dto.getBankAccountNumber());
        labourer.setBankIfscCode(dto.getBankIfscCode());
        labourer.setBankName(dto.getBankName());
        labourer.setBankBeneficiaryName(dto.getBankBeneficiaryName());
        labourer.setQrCodeUrl(dto.getQrCodeUrl());
        labourer.setNotes(dto.getNotes());
        labourer.setUpdatedBy(username);

        ensureSpecificUpiAndQr(labourer);

        Labourer updated = labourerRepository.save(labourer);

        if (dto.getCustomFields() != null) {
            customFieldService.saveEntityFieldValues(labourer.getBusiness().getId(), "LABOURER", updated.getId(), dto.getCustomFields());
        }

        auditLogService.logAction(
                labourer.getBusiness().getId(), null, username, "UPDATE", "LABOURER",
                updated.getId().toString(), null, "Updated labour profile: " + updated.getFullName(), ipAddress
        );

        return mapToDto(updated);
    }

    @Transactional
    public void deactivateLabourer(Long id, String reason, String username, String ipAddress) {
        Labourer labourer = labourerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Labourer not found with ID: " + id));

        labourer.setStatus("INACTIVE");
        labourer.setNotes((labourer.getNotes() != null ? labourer.getNotes() + "\n" : "") + "Deactivated on " + LocalDate.now() + ". Reason: " + reason);
        labourer.setUpdatedBy(username);
        labourerRepository.save(labourer);

        auditLogService.logAction(
                labourer.getBusiness().getId(), null, username, "STATUS_CHANGE", "LABOURER",
                id.toString(), "ACTIVE", "INACTIVE - Reason: " + reason, ipAddress
        );
    }

    private void ensureSpecificUpiAndQr(Labourer labourer) {
        if (labourer.getUpiId() == null || labourer.getUpiId().isBlank()) {
            String phoneDigits = labourer.getMobileNumber() != null ? labourer.getMobileNumber().replaceAll("[^0-9]", "") : "";
            String upi = (phoneDigits.length() >= 10 ? phoneDigits.substring(phoneDigits.length() - 10) : labourer.getLabourCode().toLowerCase()) + "@upi";
            labourer.setUpiId(upi);
        }
        if (labourer.getBankBeneficiaryName() == null || labourer.getBankBeneficiaryName().isBlank()) {
            labourer.setBankBeneficiaryName(labourer.getFullName());
        }
        if (labourer.getQrCodeUrl() == null || labourer.getQrCodeUrl().isBlank()) {
            try {
                String upiUri = "upi://pay?pa=" + labourer.getUpiId() + "&pn=" + java.net.URLEncoder.encode(labourer.getBankBeneficiaryName(), java.nio.charset.StandardCharsets.UTF_8) + "&cu=INR";
                String qr = "https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=" + java.net.URLEncoder.encode(upiUri, java.nio.charset.StandardCharsets.UTF_8);
                labourer.setQrCodeUrl(qr);
            } catch (Exception ignored) {}
        }
    }

    private LabourerDto mapToDto(Labourer l) {
        LabourerDto dto = new LabourerDto();
        dto.setId(l.getId());
        dto.setBusinessId(l.getBusiness().getId());
        dto.setLabourCode(l.getLabourCode());
        dto.setFullName(l.getFullName());
        dto.setPhotoUrl(l.getPhotoUrl());
        dto.setMobileNumber(l.getMobileNumber());
        dto.setAltMobileNumber(l.getAltMobileNumber());
        dto.setAadharNumber(l.getAadharNumber());
        dto.setAddress(l.getAddress());
        dto.setEmergencyContactName(l.getEmergencyContactName());
        dto.setEmergencyContactPhone(l.getEmergencyContactPhone());
        dto.setDateOfJoining(l.getDateOfJoining());
        dto.setDesignation(l.getDesignation());
        dto.setLabourCategory(l.getLabourCategory());
        dto.setSkillLevel(l.getSkillLevel());
        dto.setDailyWage(l.getDailyWage());
        dto.setOvertimeHourlyRate(l.getOvertimeHourlyRate());

        if (l.getCurrentSite() != null) {
            dto.setCurrentSiteId(l.getCurrentSite().getId());
            dto.setCurrentSiteName(l.getCurrentSite().getSiteName());
        }

        dto.setStatus(l.getStatus());
        dto.setUpiId(l.getUpiId());
        dto.setBankAccountNumber(l.getBankAccountNumber());
        dto.setBankIfscCode(l.getBankIfscCode());
        dto.setBankName(l.getBankName());
        dto.setBankBeneficiaryName(l.getBankBeneficiaryName());
        dto.setQrCodeUrl(l.getQrCodeUrl());
        dto.setNotes(l.getNotes());

        BigDecimal outstanding = advanceRepository.calculateOutstandingAdvanceByLabourer(l.getId());
        dto.setOutstandingAdvance(outstanding != null ? outstanding : BigDecimal.ZERO);

        return dto;
    }
}
