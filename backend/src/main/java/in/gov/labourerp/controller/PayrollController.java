package in.gov.labourerp.controller;

import in.gov.labourerp.dto.common.ApiResponse;
import in.gov.labourerp.dto.common.PageResponse;
import in.gov.labourerp.dto.payroll.PayrollGenerateRequest;
import in.gov.labourerp.dto.payroll.PayrollRunDto;
import in.gov.labourerp.service.PayrollService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payroll")
public class PayrollController {

    private final PayrollService payrollService;

    public PayrollController(PayrollService payrollService) {
        this.payrollService = payrollService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PayrollRunDto>>> getPayrollRuns(
            @RequestParam Long businessId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Long siteId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<PayrollRunDto> result = payrollService.getPayrollRuns(businessId, year, month, siteId, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.fromPage(result)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PayrollRunDto>> getPayrollRunById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(payrollService.getPayrollRunById(id)));
    }

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<PayrollRunDto>> generatePayroll(
            @Valid @RequestBody PayrollGenerateRequest request,
            Authentication auth,
            HttpServletRequest httpRequest) {
        String username = auth != null ? auth.getName() : "ADMIN";
        String ip = httpRequest.getRemoteAddr();
        PayrollRunDto generated = payrollService.generatePayroll(request, username, ip);
        return ResponseEntity.ok(ApiResponse.success("Payroll generated successfully", generated));
    }
}
