package in.gov.labourerp.service;

import in.gov.labourerp.dto.transfer.LabourTransferDto;
import in.gov.labourerp.entity.Business;
import in.gov.labourerp.entity.LabourTransfer;
import in.gov.labourerp.entity.Labourer;
import in.gov.labourerp.entity.Site;
import in.gov.labourerp.repository.BusinessRepository;
import in.gov.labourerp.repository.LabourTransferRepository;
import in.gov.labourerp.repository.LabourerRepository;
import in.gov.labourerp.repository.SiteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class TransferService {

    private final LabourTransferRepository transferRepository;
    private final BusinessRepository businessRepository;
    private final LabourerRepository labourerRepository;
    private final SiteRepository siteRepository;
    private final AuditLogService auditLogService;

    public TransferService(LabourTransferRepository transferRepository,
                           BusinessRepository businessRepository,
                           LabourerRepository labourerRepository,
                           SiteRepository siteRepository,
                           AuditLogService auditLogService) {
        this.transferRepository = transferRepository;
        this.businessRepository = businessRepository;
        this.labourerRepository = labourerRepository;
        this.siteRepository = siteRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public Page<LabourTransferDto> getTransfers(Long businessId, Pageable pageable) {
        Page<LabourTransfer> page = transferRepository.findByBusinessIdOrderByTransferDateDesc(businessId, pageable);
        return page.map(this::mapToDto);
    }

    @Transactional
    public LabourTransferDto executeTransfer(LabourTransferDto dto, String username, String ipAddress) {
        Business business = businessRepository.findById(dto.getBusinessId())
                .orElseThrow(() -> new RuntimeException("Business not found with ID: " + dto.getBusinessId()));

        Labourer labourer = labourerRepository.findById(dto.getLabourerId())
                .orElseThrow(() -> new RuntimeException("Labourer not found with ID: " + dto.getLabourerId()));

        Site toSite = siteRepository.findById(dto.getToSiteId())
                .orElseThrow(() -> new RuntimeException("Destination site not found with ID: " + dto.getToSiteId()));

        Site fromSite = labourer.getCurrentSite();

        LabourTransfer transfer = new LabourTransfer();
        transfer.setBusiness(business);
        transfer.setLabourer(labourer);
        transfer.setFromSite(fromSite);
        transfer.setToSite(toSite);
        transfer.setTransferDate(dto.getTransferDate() != null ? dto.getTransferDate() : LocalDate.now());
        transfer.setEffectiveDate(dto.getEffectiveDate() != null ? dto.getEffectiveDate() : LocalDate.now());
        transfer.setReason(dto.getReason());
        transfer.setRemarks(dto.getRemarks());
        transfer.setTransferredBy(username);

        LabourTransfer saved = transferRepository.save(transfer);

        // Update labourer's current site assignment
        labourer.setCurrentSite(toSite);
        labourer.setUpdatedBy(username);
        labourerRepository.save(labourer);

        auditLogService.logAction(
                dto.getBusinessId(), null, username, "TRANSFER", "LABOURER",
                labourer.getId().toString(),
                fromSite != null ? fromSite.getSiteName() : "Unassigned",
                "Transferred to " + toSite.getSiteName() + " | Reason: " + dto.getReason(),
                ipAddress
        );

        return mapToDto(saved);
    }

    private LabourTransferDto mapToDto(LabourTransfer t) {
        LabourTransferDto dto = new LabourTransferDto();
        dto.setId(t.getId());
        dto.setBusinessId(t.getBusiness().getId());
        dto.setLabourerId(t.getLabourer().getId());
        dto.setLabourCode(t.getLabourer().getLabourCode());
        dto.setLabourName(t.getLabourer().getFullName());
        dto.setDesignation(t.getLabourer().getDesignation());

        if (t.getFromSite() != null) {
            dto.setFromSiteId(t.getFromSite().getId());
            dto.setFromSiteName(t.getFromSite().getSiteName());
        }

        dto.setToSiteId(t.getToSite().getId());
        dto.setToSiteName(t.getToSite().getSiteName());
        dto.setTransferDate(t.getTransferDate());
        dto.setEffectiveDate(t.getEffectiveDate());
        dto.setReason(t.getReason());
        dto.setRemarks(t.getRemarks());
        dto.setTransferredBy(t.getTransferredBy());
        return dto;
    }
}
