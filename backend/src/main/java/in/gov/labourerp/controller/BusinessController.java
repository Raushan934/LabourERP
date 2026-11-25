package in.gov.labourerp.controller;

import in.gov.labourerp.dto.business.BusinessDto;
import in.gov.labourerp.dto.business.BusinessModuleDto;
import in.gov.labourerp.dto.business.BusinessSettingDto;
import in.gov.labourerp.dto.common.ApiResponse;
import in.gov.labourerp.service.BusinessService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/businesses")
public class BusinessController {

    private final BusinessService businessService;

    public BusinessController(BusinessService businessService) {
        this.businessService = businessService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BusinessDto>>> getAllBusinesses() {
        return ResponseEntity.ok(ApiResponse.success(businessService.getAllBusinesses()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BusinessDto>> getBusinessById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(businessService.getBusinessById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BusinessDto>> createBusiness(@Valid @RequestBody BusinessDto dto,
                                                                   Authentication auth,
                                                                   HttpServletRequest request) {
        String username = auth != null ? auth.getName() : "ADMIN";
        String ip = request.getRemoteAddr();
        BusinessDto created = businessService.createBusiness(dto, username, ip);
        return ResponseEntity.ok(ApiResponse.success("Business profile created successfully", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BusinessDto>> updateBusiness(@PathVariable Long id,
                                                                   @Valid @RequestBody BusinessDto dto,
                                                                   Authentication auth,
                                                                   HttpServletRequest request) {
        String username = auth != null ? auth.getName() : "ADMIN";
        String ip = request.getRemoteAddr();
        BusinessDto updated = businessService.updateBusiness(id, dto, username, ip);
        return ResponseEntity.ok(ApiResponse.success("Business profile updated successfully", updated));
    }

    @GetMapping("/{id}/modules")
    public ResponseEntity<ApiResponse<List<BusinessModuleDto>>> getBusinessModules(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(businessService.getBusinessModules(id)));
    }

    @PatchMapping("/{id}/modules/{moduleCode}")
    public ResponseEntity<ApiResponse<BusinessModuleDto>> toggleBusinessModule(@PathVariable Long id,
                                                                               @PathVariable String moduleCode,
                                                                               @RequestBody Map<String, Boolean> body,
                                                                               Authentication auth) {
        boolean enabled = body.getOrDefault("enabled", true);
        String username = auth != null ? auth.getName() : "ADMIN";
        BusinessModuleDto mod = businessService.toggleBusinessModule(id, moduleCode, enabled, username);
        return ResponseEntity.ok(ApiResponse.success("Module visibility updated", mod));
    }

    @GetMapping("/{id}/settings")
    public ResponseEntity<ApiResponse<List<BusinessSettingDto>>> getBusinessSettings(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(businessService.getBusinessSettings(id)));
    }

    @PutMapping("/{id}/settings/{settingKey}")
    public ResponseEntity<ApiResponse<BusinessSettingDto>> updateBusinessSetting(@PathVariable Long id,
                                                                                 @PathVariable String settingKey,
                                                                                 @RequestBody Map<String, String> body,
                                                                                 Authentication auth) {
        String val = body.getOrDefault("settingValue", "");
        String username = auth != null ? auth.getName() : "ADMIN";
        BusinessSettingDto setting = businessService.updateBusinessSetting(id, settingKey, val, username);
        return ResponseEntity.ok(ApiResponse.success("Business setting updated", setting));
    }
}
