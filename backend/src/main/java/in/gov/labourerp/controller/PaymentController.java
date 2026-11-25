package in.gov.labourerp.controller;

import in.gov.labourerp.dto.common.ApiResponse;
import in.gov.labourerp.dto.common.PageResponse;
import in.gov.labourerp.dto.finance.PaymentDto;
import in.gov.labourerp.service.PaymentService;
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
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PaymentDto>>> getPayments(
            @RequestParam Long businessId,
            @RequestParam(required = false) Long labourerId,
            @RequestParam(required = false) Long siteId,
            @RequestParam(required = false) String paymentMethod,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<PaymentDto> result = paymentService.getPayments(businessId, labourerId, siteId, paymentMethod, startDate, endDate, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.fromPage(result)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PaymentDto>> recordPayment(
            @Valid @RequestBody PaymentDto dto,
            Authentication auth,
            HttpServletRequest request) {
        String username = auth != null ? auth.getName() : "ACCOUNTANT";
        String ip = request.getRemoteAddr();
        PaymentDto saved = paymentService.recordPayment(dto, username, ip);
        return ResponseEntity.ok(ApiResponse.success("Payment disbursement recorded successfully", saved));
    }
}
