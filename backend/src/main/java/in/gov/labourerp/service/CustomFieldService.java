package in.gov.labourerp.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.gov.labourerp.dto.customfield.CustomFieldDefinitionDto;
import in.gov.labourerp.dto.customfield.CustomFieldValueDto;
import in.gov.labourerp.entity.Business;
import in.gov.labourerp.entity.CustomFieldDefinition;
import in.gov.labourerp.entity.CustomFieldValue;
import in.gov.labourerp.repository.BusinessRepository;
import in.gov.labourerp.repository.CustomFieldDefinitionRepository;
import in.gov.labourerp.repository.CustomFieldValueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CustomFieldService {

    private final CustomFieldDefinitionRepository definitionRepository;
    private final CustomFieldValueRepository valueRepository;
    private final BusinessRepository businessRepository;
    private final ObjectMapper objectMapper;
    private final AuditLogService auditLogService;

    public CustomFieldService(CustomFieldDefinitionRepository definitionRepository,
                              CustomFieldValueRepository valueRepository,
                              BusinessRepository businessRepository,
                              ObjectMapper objectMapper,
                              AuditLogService auditLogService) {
        this.definitionRepository = definitionRepository;
        this.valueRepository = valueRepository;
        this.businessRepository = businessRepository;
        this.objectMapper = objectMapper;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public List<CustomFieldDefinitionDto> getDefinitions(Long businessId, String entityType, boolean activeOnly) {
        List<CustomFieldDefinition> list = activeOnly
                ? definitionRepository.findByBusinessIdAndEntityTypeAndIsActiveTrueOrderByDisplayOrderAsc(businessId, entityType)
                : definitionRepository.findByBusinessIdAndEntityTypeOrderByDisplayOrderAsc(businessId, entityType);

        return list.stream().map(this::mapDefinitionToDto).collect(Collectors.toList());
    }

    @Transactional
    public CustomFieldDefinitionDto createDefinition(CustomFieldDefinitionDto dto, String username, String ipAddress) {
        Business business = businessRepository.findById(dto.getBusinessId())
                .orElseThrow(() -> new RuntimeException("Business not found with ID: " + dto.getBusinessId()));

        if (definitionRepository.findByBusinessIdAndEntityTypeAndFieldKey(dto.getBusinessId(), dto.getEntityType(), dto.getFieldKey()).isPresent()) {
            throw new RuntimeException("Field key already exists for entity: " + dto.getFieldKey());
        }

        CustomFieldDefinition def = new CustomFieldDefinition();
        def.setBusiness(business);
        def.setEntityType(dto.getEntityType().toUpperCase());
        def.setFieldKey(dto.getFieldKey().toLowerCase().replaceAll("\\s+", "_"));
        def.setFieldLabel(dto.getFieldLabel());
        def.setFieldType(dto.getFieldType().toUpperCase());
        def.setRequired(dto.isRequired());
        def.setDefaultValue(dto.getDefaultValue());
        def.setPlaceholder(dto.getPlaceholder());
        def.setDisplayOrder(dto.getDisplayOrder());
        def.setActive(dto.isActive());

        if (dto.getOptions() != null && !dto.getOptions().isEmpty()) {
            try {
                def.setOptionsJson(objectMapper.writeValueAsString(dto.getOptions()));
            } catch (Exception e) {
                def.setOptionsJson(dto.getOptionsJson());
            }
        } else {
            def.setOptionsJson(dto.getOptionsJson());
        }

        CustomFieldDefinition saved = definitionRepository.save(def);

        auditLogService.logAction(
                dto.getBusinessId(), null, username, "CREATE", "CUSTOM_FIELD_DEF",
                saved.getId().toString(), null, "Created custom field: " + saved.getFieldLabel() + " on " + saved.getEntityType(), ipAddress
        );

        return mapDefinitionToDto(saved);
    }

    @Transactional(readOnly = true)
    public Map<String, String> getEntityFieldValues(Long entityId) {
        List<CustomFieldValue> values = valueRepository.findByEntityId(entityId);
        Map<String, String> map = new HashMap<>();
        for (CustomFieldValue cfv : values) {
            map.put(cfv.getDefinition().getFieldKey(), cfv.getFieldValue());
        }
        return map;
    }

    @Transactional
    public void saveEntityFieldValues(Long businessId, String entityType, Long entityId, Map<String, String> values) {
        if (values == null || values.isEmpty()) return;

        List<CustomFieldDefinition> definitions = definitionRepository.findByBusinessIdAndEntityTypeOrderByDisplayOrderAsc(businessId, entityType);
        for (CustomFieldDefinition def : definitions) {
            if (values.containsKey(def.getFieldKey())) {
                String val = values.get(def.getFieldKey());
                CustomFieldValue cfv = valueRepository.findByDefinitionIdAndEntityId(def.getId(), entityId)
                        .orElse(new CustomFieldValue(def, entityId, val));
                cfv.setFieldValue(val);
                valueRepository.save(cfv);
            }
        }
    }

    private CustomFieldDefinitionDto mapDefinitionToDto(CustomFieldDefinition def) {
        CustomFieldDefinitionDto dto = new CustomFieldDefinitionDto();
        dto.setId(def.getId());
        dto.setBusinessId(def.getBusiness().getId());
        dto.setEntityType(def.getEntityType());
        dto.setFieldKey(def.getFieldKey());
        dto.setFieldLabel(def.getFieldLabel());
        dto.setFieldType(def.getFieldType());
        dto.setRequired(def.isRequired());
        dto.setDefaultValue(def.getDefaultValue());
        dto.setPlaceholder(def.getPlaceholder());
        dto.setOptionsJson(def.getOptionsJson());
        dto.setDisplayOrder(def.getDisplayOrder());
        dto.setActive(def.isActive());

        if (def.getOptionsJson() != null && !def.getOptionsJson().isBlank()) {
            try {
                List<String> options = objectMapper.readValue(def.getOptionsJson(), new TypeReference<List<String>>() {});
                dto.setOptions(options);
            } catch (Exception e) {
                dto.setOptions(Collections.emptyList());
            }
        }
        return dto;
    }
}
