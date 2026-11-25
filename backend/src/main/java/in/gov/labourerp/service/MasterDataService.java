package in.gov.labourerp.service;

import in.gov.labourerp.dto.master.MasterValueDto;
import in.gov.labourerp.entity.Business;
import in.gov.labourerp.entity.MasterCategory;
import in.gov.labourerp.entity.MasterValue;
import in.gov.labourerp.repository.BusinessRepository;
import in.gov.labourerp.repository.MasterCategoryRepository;
import in.gov.labourerp.repository.MasterValueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MasterDataService {

    private final MasterCategoryRepository masterCategoryRepository;
    private final MasterValueRepository masterValueRepository;
    private final BusinessRepository businessRepository;
    private final AuditLogService auditLogService;

    public MasterDataService(MasterCategoryRepository masterCategoryRepository,
                             MasterValueRepository masterValueRepository,
                             BusinessRepository businessRepository,
                             AuditLogService auditLogService) {
        this.masterCategoryRepository = masterCategoryRepository;
        this.masterValueRepository = masterValueRepository;
        this.businessRepository = businessRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public List<MasterCategory> getAllCategories() {
        return masterCategoryRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<MasterValueDto> getActiveValues(String categoryCode, Long businessId) {
        return masterValueRepository.findActiveByCategoryCodeAndBusinessId(categoryCode, businessId)
                .stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MasterValueDto> getAllValues(String categoryCode, Long businessId) {
        return masterValueRepository.findAllByCategoryCodeAndBusinessId(categoryCode, businessId)
                .stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Transactional
    public MasterValueDto createMasterValue(MasterValueDto dto, String username, String ipAddress) {
        MasterCategory category;
        if (dto.getCategoryId() != null) {
            category = masterCategoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found with ID: " + dto.getCategoryId()));
        } else if (dto.getCategoryCode() != null) {
            category = masterCategoryRepository.findByCategoryCode(dto.getCategoryCode())
                    .orElseThrow(() -> new RuntimeException("Category not found with Code: " + dto.getCategoryCode()));
        } else {
            throw new RuntimeException("Category ID or Category Code is required");
        }

        Business business = null;
        if (dto.getBusinessId() != null) {
            business = businessRepository.findById(dto.getBusinessId()).orElse(null);
        }

        MasterValue masterValue = new MasterValue();
        masterValue.setCategory(category);
        masterValue.setBusiness(business);
        masterValue.setCode(dto.getCode().toUpperCase().trim());
        masterValue.setName(dto.getName().trim());
        masterValue.setDescription(dto.getDescription());
        masterValue.setColorCode(dto.getColorCode() != null ? dto.getColorCode() : "#1B3557");
        masterValue.setPayable(dto.isPayable());
        if (dto.getWagePercentage() != null) {
            masterValue.setWagePercentage(dto.getWagePercentage());
        }
        masterValue.setSortOrder(dto.getSortOrder());
        masterValue.setActive(dto.isActive());

        MasterValue saved = masterValueRepository.save(masterValue);

        auditLogService.logAction(
                dto.getBusinessId(), null, username, "CREATE", "MASTER_VALUE",
                saved.getId().toString(), null, "Created master value: " + saved.getName() + " in " + category.getCategoryCode(), ipAddress
        );

        return mapToDto(saved);
    }

    @Transactional
    public MasterValueDto updateMasterValue(Long id, MasterValueDto dto, String username, String ipAddress) {
        MasterValue mv = masterValueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Master value not found with ID: " + id));

        mv.setName(dto.getName());
        mv.setDescription(dto.getDescription());
        if (dto.getColorCode() != null) mv.setColorCode(dto.getColorCode());
        mv.setPayable(dto.isPayable());
        if (dto.getWagePercentage() != null) mv.setWagePercentage(dto.getWagePercentage());
        mv.setSortOrder(dto.getSortOrder());
        mv.setActive(dto.isActive());

        MasterValue saved = masterValueRepository.save(mv);

        auditLogService.logAction(
                mv.getBusiness() != null ? mv.getBusiness().getId() : null, null, username, "UPDATE", "MASTER_VALUE",
                saved.getId().toString(), null, "Updated master value: " + saved.getName(), ipAddress
        );

        return mapToDto(saved);
    }

    @Transactional
    public void toggleActiveStatus(Long id, boolean active, String username, String ipAddress) {
        MasterValue mv = masterValueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Master value not found with ID: " + id));
        mv.setActive(active);
        masterValueRepository.save(mv);

        auditLogService.logAction(
                mv.getBusiness() != null ? mv.getBusiness().getId() : null, null, username, "STATUS_CHANGE", "MASTER_VALUE",
                id.toString(), null, "Toggled master value active state to: " + active, ipAddress
        );
    }

    private MasterValueDto mapToDto(MasterValue mv) {
        MasterValueDto dto = new MasterValueDto();
        dto.setId(mv.getId());
        dto.setCategoryId(mv.getCategory().getId());
        dto.setCategoryCode(mv.getCategory().getCategoryCode());
        dto.setBusinessId(mv.getBusiness() != null ? mv.getBusiness().getId() : null);
        dto.setCode(mv.getCode());
        dto.setName(mv.getName());
        dto.setDescription(mv.getDescription());
        dto.setColorCode(mv.getColorCode());
        dto.setPayable(mv.isPayable());
        dto.setWagePercentage(mv.getWagePercentage());
        dto.setSortOrder(mv.getSortOrder());
        dto.setActive(mv.isActive());
        return dto;
    }
}
