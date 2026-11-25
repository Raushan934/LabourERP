package in.gov.labourerp.service;

import in.gov.labourerp.dto.finance.AdvanceDto;
import in.gov.labourerp.entity.Advance;
import in.gov.labourerp.entity.Business;
import in.gov.labourerp.entity.Labourer;
import in.gov.labourerp.entity.Site;
import in.gov.labourerp.repository.AdvanceRepository;
import in.gov.labourerp.repository.BusinessRepository;
import in.gov.labourerp.repository.LabourerRepository;
import in.gov.labourerp.repository.SiteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class AdvanceService {

    private final AdvanceRepository advanceRepository;
    private final BusinessRepository businessRepository;
    private final LabourerRepository labourerRepository;
    private final SiteRepository siteRepository;
    private final AuditLogService auditLogService;

    public AdvanceService(AdvanceRepository advanceRepository,
                          BusinessRepository businessRepository,
                          LabourerRepository labourerRepository,
                          SiteRepository siteRepository,
                          AuditLogService auditLogService) {
        this.advanceRepository = advanceRepository;
        this.businessRepository = businessRepository;
        this.labourerRepository = labourerRepository;
        this.siteRepository = siteRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public Page<AdvanceDto> getAdvances(Long businessId, Long labourerId, Long siteId,
                                        String status, LocalDate startDate, LocalDate endDate,
                                        Pageable pageable) {
        Page<Advance> page = advanceRepository.findByFilters(businessId, labourerId, siteId, status, startDate, endDate, pageable);
        return page.map(this::mapToDto);
    }

    @Transactional
    public AdvanceDto recordAdvance(AdvanceDto dto, String username, String ipAddress) {
        Business business = businessRepository.findById(dto.getBusinessId())
                .orElseThrow(() -> new RuntimeException("Business not found with ID: " + dto.getBusinessId()));

        Labourer labourer = labourerRepository.findById(dto.getLabourerId())
                .orElseThrow(() -> new RuntimeException("Labourer not found with ID: " + dto.getLabourerId()));

        Advance advance = new Advance();
        advance.setBusiness(business);
        advance.setLabourer(labourer);

        if (dto.getSiteId() != null) {
            siteRepository.findById(dto.getSiteId()).ifPresent(advance::setSite);
        } else if (labourer.getCurrentSite() != null) {
            advance.setSite(labourer.getCurrentSite());
        }

        String advCode = dto.getAdvanceCode() != null && !dto.getAdvanceCode().isBlank()
                ? dto.getAdvanceCode()
                : "ADV-" + LocalDate.now().getYear() + "-" + (System.currentTimeMillis() % 100000);

        advance.setAdvanceCode(advCode);
        advance.setAdvanceDate(dto.getAdvanceDate() != null ? dto.getAdvanceDate() : LocalDate.now());
        advance.setAmount(dto.getAmount());
        advance.setPaymentMethod(dto.getPaymentMethod() != null ? dto.getPaymentMethod() : "CASH");
        advance.setAdvanceType(dto.getAdvanceType() != null ? dto.getAdvanceType() : "PERSONAL_ADVANCE");
        advance.setReferenceNumber(dto.getReferenceNumber());
        advance.setPurpose(dto.getPurpose());
        advance.setApprovedBy(dto.getApprovedBy() != null ? dto.getApprovedBy() : username);
        advance.setRecoveredAmount(BigDecimal.ZERO);
        advance.setRecoveryStatus("OPEN");
        advance.setRemarks(dto.getRemarks());
        advance.setCreatedBy(username);

        Advance saved = advanceRepository.save(advance);

        auditLogService.logAction(
                dto.getBusinessId(), null, username, "CREATE", "ADVANCE",
                saved.getId().toString(), null,
                "Issued advance of ₹" + saved.getAmount() + " to worker: " + labourer.getFullName() + " (" + saved.getAdvanceCode() + ")",
                ipAddress
        );

        return mapToDto(saved);
    }

    private AdvanceDto mapToDto(Advance a) {
        AdvanceDto dto = new AdvanceDto();
        dto.setId(a.getId());
        dto.setBusinessId(a.getBusiness().getId());
        if (a.getSite() != null) {
            dto.setSiteId(a.getSite().getId());
            dto.setSiteName(a.getSite().getSiteName());
        }
        dto.setLabourerId(a.getLabourer().getId());
        dto.setLabourCode(a.getLabourer().getLabourCode());
        dto.setLabourName(a.getLabourer().getFullName());
        dto.setAdvanceCode(a.getAdvanceCode());
        dto.setAdvanceDate(a.getAdvanceDate());
        dto.setAmount(a.getAmount());
        dto.setPaymentMethod(a.getPaymentMethod());
        dto.setAdvanceType(a.getAdvanceType());
        dto.setReferenceNumber(a.getReferenceNumber());
        dto.setPurpose(a.getPurpose());
        dto.setApprovedBy(a.getApprovedBy());
        dto.setRecoveredAmount(a.getRecoveredAmount());
        dto.setRemainingBalance(a.getAmount().subtract(a.getRecoveredAmount()));
        dto.setRecoveryStatus(a.getRecoveryStatus());
        dto.setRemarks(a.getRemarks());
        return dto;
    }
}
