package in.gov.labourerp.controller;

import in.gov.labourerp.dto.common.ApiResponse;
import in.gov.labourerp.dto.common.PageResponse;
import in.gov.labourerp.dto.finance.AdvanceDto;
import in.gov.labourerp.service.AdvanceService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/advances")
public class AdvanceController {

    private final AdvanceService advanceService;

    public AdvanceController(AdvanceService advanceService) {
        this.advanceService = advanceService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AdvanceDto>>> getAdvances(
            @RequestParam Long businessId,
            @RequestParam(required = false) Long labourerId,
            @RequestParam(required = false) Long siteId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<AdvanceDto> result = advanceService.getAdvances(businessId, labourerId, siteId, status, startDate, endDate, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.fromPage(result)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AdvanceDto>> recordAdvance(
            @Valid @RequestBody AdvanceDto dto,
            Authentication auth,
            HttpServletRequest request) {
        String username = auth != null ? auth.getName() : "ACCOUNTANT";
        String ip = request.getRemoteAddr();
        AdvanceDto saved = advanceService.recordAdvance(dto, username, ip);
        return ResponseEntity.ok(ApiResponse.success("Advance disbursed and recorded successfully", saved));
    }
}
