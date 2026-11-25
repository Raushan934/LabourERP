package in.gov.labourerp.controller;

import in.gov.labourerp.dto.common.ApiResponse;
import in.gov.labourerp.dto.master.MasterValueDto;
import in.gov.labourerp.entity.MasterCategory;
import in.gov.labourerp.service.MasterDataService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/master-data")
public class MasterDataController {

    private final MasterDataService masterDataService;

    public MasterDataController(MasterDataService masterDataService) {
        this.masterDataService = masterDataService;
    }

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<MasterCategory>>> getCategories() {
        return ResponseEntity.ok(ApiResponse.success(masterDataService.getAllCategories()));
    }

    @GetMapping("/values")
    public ResponseEntity<ApiResponse<List<MasterValueDto>>> getValues(
            @RequestParam String category,
            @RequestParam(required = false) Long businessId,
            @RequestParam(defaultValue = "true") boolean activeOnly) {
        List<MasterValueDto> values = activeOnly
                ? masterDataService.getActiveValues(category, businessId)
                : masterDataService.getAllValues(category, businessId);
        return ResponseEntity.ok(ApiResponse.success(values));
    }

    @PostMapping("/values")
    public ResponseEntity<ApiResponse<MasterValueDto>> createMasterValue(
            @Valid @RequestBody MasterValueDto dto,
            Authentication auth,
            HttpServletRequest request) {
        String username = auth != null ? auth.getName() : "ADMIN";
        String ip = request.getRemoteAddr();
        MasterValueDto created = masterDataService.createMasterValue(dto, username, ip);
        return ResponseEntity.ok(ApiResponse.success("Master data entry created successfully", created));
    }

    @PutMapping("/values/{id}")
    public ResponseEntity<ApiResponse<MasterValueDto>> updateMasterValue(
            @PathVariable Long id,
            @Valid @RequestBody MasterValueDto dto,
            Authentication auth,
            HttpServletRequest request) {
        String username = auth != null ? auth.getName() : "ADMIN";
        String ip = request.getRemoteAddr();
        MasterValueDto updated = masterDataService.updateMasterValue(id, dto, username, ip);
        return ResponseEntity.ok(ApiResponse.success("Master data entry updated successfully", updated));
    }

    @PatchMapping("/values/{id}/toggle-active")
    public ResponseEntity<ApiResponse<Void>> toggleActive(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> body,
            Authentication auth,
            HttpServletRequest request) {
        boolean active = body.getOrDefault("active", true);
        String username = auth != null ? auth.getName() : "ADMIN";
        String ip = request.getRemoteAddr();
        masterDataService.toggleActiveStatus(id, active, username, ip);
        return ResponseEntity.ok(ApiResponse.success("Status updated successfully", null));
    }
}
