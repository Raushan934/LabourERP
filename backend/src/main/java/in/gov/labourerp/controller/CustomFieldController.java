package in.gov.labourerp.controller;

import in.gov.labourerp.dto.common.ApiResponse;
import in.gov.labourerp.dto.customfield.CustomFieldDefinitionDto;
import in.gov.labourerp.service.CustomFieldService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/custom-fields")
public class CustomFieldController {

    private final CustomFieldService customFieldService;

    public CustomFieldController(CustomFieldService customFieldService) {
        this.customFieldService = customFieldService;
    }

    @GetMapping("/definitions")
    public ResponseEntity<ApiResponse<List<CustomFieldDefinitionDto>>> getDefinitions(
            @RequestParam Long businessId,
            @RequestParam String entityType,
            @RequestParam(defaultValue = "true") boolean activeOnly) {
        List<CustomFieldDefinitionDto> defs = customFieldService.getDefinitions(businessId, entityType, activeOnly);
        return ResponseEntity.ok(ApiResponse.success(defs));
    }

    @PostMapping("/definitions")
    public ResponseEntity<ApiResponse<CustomFieldDefinitionDto>> createDefinition(
            @Valid @RequestBody CustomFieldDefinitionDto dto,
            Authentication auth,
            HttpServletRequest request) {
        String username = auth != null ? auth.getName() : "ADMIN";
        String ip = request.getRemoteAddr();
        CustomFieldDefinitionDto created = customFieldService.createDefinition(dto, username, ip);
        return ResponseEntity.ok(ApiResponse.success("Custom field definition added", created));
    }

    @GetMapping("/values/{entityId}")
    public ResponseEntity<ApiResponse<Map<String, String>>> getEntityValues(@PathVariable Long entityId) {
        Map<String, String> values = customFieldService.getEntityFieldValues(entityId);
        return ResponseEntity.ok(ApiResponse.success(values));
    }
}
