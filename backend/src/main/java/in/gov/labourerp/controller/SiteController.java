package in.gov.labourerp.controller;

import in.gov.labourerp.dto.common.ApiResponse;
import in.gov.labourerp.dto.common.PageResponse;
import in.gov.labourerp.dto.site.SiteDto;
import in.gov.labourerp.service.SiteService;
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

@RestController
@RequestMapping("/api/v1/sites")
public class SiteController {

    private final SiteService siteService;

    public SiteController(SiteService siteService) {
        this.siteService = siteService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<SiteDto>>> getSites(
            @RequestParam Long businessId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "id,desc") String sort) {

        String[] sortParams = sort.split(",");
        Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));

        Page<SiteDto> result = siteService.getSites(businessId, status, search, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.fromPage(result)));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<SiteDto>>> getAllActiveSites(@RequestParam Long businessId) {
        return ResponseEntity.ok(ApiResponse.success(siteService.getAllActiveSites(businessId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SiteDto>> getSiteById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(siteService.getSiteById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SiteDto>> createSite(@Valid @RequestBody SiteDto dto,
                                                           Authentication auth,
                                                           HttpServletRequest request) {
        String username = auth != null ? auth.getName() : "ADMIN";
        String ip = request.getRemoteAddr();
        SiteDto created = siteService.createSite(dto, username, ip);
        return ResponseEntity.ok(ApiResponse.success("Site created successfully", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SiteDto>> updateSite(@PathVariable Long id,
                                                           @Valid @RequestBody SiteDto dto,
                                                           Authentication auth,
                                                           HttpServletRequest request) {
        String username = auth != null ? auth.getName() : "ADMIN";
        String ip = request.getRemoteAddr();
        SiteDto updated = siteService.updateSite(id, dto, username, ip);
        return ResponseEntity.ok(ApiResponse.success("Site updated successfully", updated));
    }
}
