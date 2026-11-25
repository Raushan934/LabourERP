package in.gov.labourerp.controller;

import in.gov.labourerp.dto.common.ApiResponse;
import in.gov.labourerp.dto.common.PageResponse;
import in.gov.labourerp.dto.labour.LabourerDto;
import in.gov.labourerp.service.LabourService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/labour")
public class LabourController {

    private final LabourService labourService;

    public LabourController(LabourService labourService) {
        this.labourService = labourService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<LabourerDto>>> getLabourers(
            @RequestParam Long businessId,
            @RequestParam(required = false) Long siteId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "id,desc") String sort) {

        String[] sortParams = sort.split(",");
        Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));

        Page<LabourerDto> result = labourService.getLabourers(businessId, siteId, category, status, search, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.fromPage(result)));
    }

    @GetMapping("/by-site")
    public ResponseEntity<ApiResponse<List<LabourerDto>>> getLabourersBySite(
            @RequestParam Long businessId,
            @RequestParam Long siteId) {
        return ResponseEntity.ok(ApiResponse.success(labourService.getActiveLabourersBySite(businessId, siteId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LabourerDto>> getLabourerById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(labourService.getLabourerById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<LabourerDto>> createLabourer(
            @Valid @RequestBody LabourerDto dto,
            Authentication auth,
            HttpServletRequest request) {
        String username = auth != null ? auth.getName() : "ADMIN";
        String ip = request.getRemoteAddr();
        LabourerDto created = labourService.createLabourer(dto, username, ip);
        return ResponseEntity.ok(ApiResponse.success("Worker enrolled successfully", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<LabourerDto>> updateLabourer(
            @PathVariable Long id,
            @Valid @RequestBody LabourerDto dto,
            Authentication auth,
            HttpServletRequest request) {
        String username = auth != null ? auth.getName() : "ADMIN";
        String ip = request.getRemoteAddr();
        LabourerDto updated = labourService.updateLabourer(id, dto, username, ip);
        return ResponseEntity.ok(ApiResponse.success("Worker profile updated successfully", updated));
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivateLabourer(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            Authentication auth,
            HttpServletRequest request) {
        String reason = body.getOrDefault("reason", "Administrative deactivation");
        String username = auth != null ? auth.getName() : "ADMIN";
        String ip = request.getRemoteAddr();
        labourService.deactivateLabourer(id, reason, username, ip);
        return ResponseEntity.ok(ApiResponse.success("Worker profile deactivated successfully", null));
    }
}
