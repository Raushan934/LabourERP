package in.gov.labourerp.service;

import in.gov.labourerp.dto.site.SiteDto;
import in.gov.labourerp.entity.Business;
import in.gov.labourerp.entity.Site;
import in.gov.labourerp.entity.User;
import in.gov.labourerp.repository.BusinessRepository;
import in.gov.labourerp.repository.LabourerRepository;
import in.gov.labourerp.repository.SiteRepository;
import in.gov.labourerp.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SiteService {

    private final SiteRepository siteRepository;
    private final BusinessRepository businessRepository;
    private final UserRepository userRepository;
    private final LabourerRepository labourerRepository;
    private final CustomFieldService customFieldService;
    private final AuditLogService auditLogService;

    public SiteService(SiteRepository siteRepository,
                       BusinessRepository businessRepository,
                       UserRepository userRepository,
                       LabourerRepository labourerRepository,
                       CustomFieldService customFieldService,
                       AuditLogService auditLogService) {
        this.siteRepository = siteRepository;
        this.businessRepository = businessRepository;
        this.userRepository = userRepository;
        this.labourerRepository = labourerRepository;
        this.customFieldService = customFieldService;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public Page<SiteDto> getSites(Long businessId, String status, String search, Pageable pageable) {
        String searchParam = (search != null && !search.trim().isEmpty()) ? "%" + search.toLowerCase().trim() + "%" : null;
        String statusParam = (status != null && !status.trim().isEmpty()) ? status.trim() : null;
        Page<Site> page = siteRepository.findByFilters(businessId, statusParam, searchParam, pageable);
        return page.map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public List<SiteDto> getAllActiveSites(Long businessId) {
        return siteRepository.findByBusinessIdAndStatus(businessId, "ACTIVE")
                .stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SiteDto getSiteById(Long id) {
        Site site = siteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Site not found with ID: " + id));
        SiteDto dto = mapToDto(site);
        dto.setCustomFields(customFieldService.getEntityFieldValues(site.getId()));
        return dto;
    }

    @Transactional
    public SiteDto createSite(SiteDto dto, String username, String ipAddress) {
        Business business = businessRepository.findById(dto.getBusinessId())
                .orElseThrow(() -> new RuntimeException("Business not found with ID: " + dto.getBusinessId()));

        if (siteRepository.existsByBusinessIdAndSiteCode(dto.getBusinessId(), dto.getSiteCode())) {
            throw new RuntimeException("Site code already exists in this business: " + dto.getSiteCode());
        }

        Site site = new Site();
        site.setBusiness(business);
        site.setSiteCode(dto.getSiteCode().toUpperCase().trim());
        site.setSiteName(dto.getSiteName().trim());
        site.setClientName(dto.getClientName());
        site.setSiteCategory(dto.getSiteCategory() != null ? dto.getSiteCategory() : "ELECTRICAL_INSTALLATION");
        site.setLocationAddress(dto.getLocationAddress());
        site.setCity(dto.getCity());
        site.setState(dto.getState());

        if (dto.getSiteManagerId() != null) {
            userRepository.findById(dto.getSiteManagerId()).ifPresent(site::setSiteManager);
        }
        if (dto.getSupervisorId() != null) {
            userRepository.findById(dto.getSupervisorId()).ifPresent(site::setSupervisor);
        }

        site.setStartDate(dto.getStartDate());
        site.setExpectedEndDate(dto.getExpectedEndDate());
        site.setStatus(dto.getStatus() != null ? dto.getStatus() : "ACTIVE");
        site.setNotes(dto.getNotes());
        site.setCreatedBy(username);

        Site saved = siteRepository.save(site);

        if (dto.getCustomFields() != null && !dto.getCustomFields().isEmpty()) {
            customFieldService.saveEntityFieldValues(dto.getBusinessId(), "SITE", saved.getId(), dto.getCustomFields());
        }

        auditLogService.logAction(
                dto.getBusinessId(), null, username, "CREATE", "SITE",
                saved.getId().toString(), null, "Created site: " + saved.getSiteName() + " (" + saved.getSiteCode() + ")", ipAddress
        );

        return mapToDto(saved);
    }

    @Transactional
    public SiteDto updateSite(Long id, SiteDto dto, String username, String ipAddress) {
        Site site = siteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Site not found with ID: " + id));

        site.setSiteName(dto.getSiteName());
        site.setClientName(dto.getClientName());
        site.setSiteCategory(dto.getSiteCategory());
        site.setLocationAddress(dto.getLocationAddress());
        site.setCity(dto.getCity());
        site.setState(dto.getState());

        if (dto.getSiteManagerId() != null) {
            userRepository.findById(dto.getSiteManagerId()).ifPresent(site::setSiteManager);
        } else {
            site.setSiteManager(null);
        }

        if (dto.getSupervisorId() != null) {
            userRepository.findById(dto.getSupervisorId()).ifPresent(site::setSupervisor);
        } else {
            site.setSupervisor(null);
        }

        site.setStartDate(dto.getStartDate());
        site.setExpectedEndDate(dto.getExpectedEndDate());
        site.setStatus(dto.getStatus());
        site.setNotes(dto.getNotes());
        site.setUpdatedBy(username);

        Site updated = siteRepository.save(site);

        if (dto.getCustomFields() != null) {
            customFieldService.saveEntityFieldValues(site.getBusiness().getId(), "SITE", updated.getId(), dto.getCustomFields());
        }

        auditLogService.logAction(
                site.getBusiness().getId(), null, username, "UPDATE", "SITE",
                updated.getId().toString(), null, "Updated site: " + updated.getSiteName(), ipAddress
        );

        return mapToDto(updated);
    }

    private SiteDto mapToDto(Site s) {
        SiteDto dto = new SiteDto();
        dto.setId(s.getId());
        dto.setBusinessId(s.getBusiness().getId());
        dto.setSiteCode(s.getSiteCode());
        dto.setSiteName(s.getSiteName());
        dto.setClientName(s.getClientName());
        dto.setSiteCategory(s.getSiteCategory());
        dto.setLocationAddress(s.getLocationAddress());
        dto.setCity(s.getCity());
        dto.setState(s.getState());

        if (s.getSiteManager() != null) {
            dto.setSiteManagerId(s.getSiteManager().getId());
            dto.setSiteManagerName(s.getSiteManager().getFullName());
        }
        if (s.getSupervisor() != null) {
            dto.setSupervisorId(s.getSupervisor().getId());
            dto.setSupervisorName(s.getSupervisor().getFullName());
        }

        dto.setStartDate(s.getStartDate());
        dto.setExpectedEndDate(s.getExpectedEndDate());
        dto.setStatus(s.getStatus());
        dto.setNotes(s.getNotes());

        dto.setAssignedLabourCount(labourerRepository.countActiveBySite(s.getBusiness().getId(), s.getId()));
        return dto;
    }
}
