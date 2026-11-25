package in.gov.labourerp.controller;

import in.gov.labourerp.dto.common.ApiResponse;
import in.gov.labourerp.dto.common.PageResponse;
import in.gov.labourerp.dto.transfer.LabourTransferDto;
import in.gov.labourerp.service.TransferService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transfers")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<LabourTransferDto>>> getTransfers(
            @RequestParam Long businessId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<LabourTransferDto> result = transferService.getTransfers(businessId, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.fromPage(result)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<LabourTransferDto>> executeTransfer(
            @Valid @RequestBody LabourTransferDto dto,
            Authentication auth,
            HttpServletRequest request) {
        String username = auth != null ? auth.getName() : "ADMIN";
        String ip = request.getRemoteAddr();
        LabourTransferDto saved = transferService.executeTransfer(dto, username, ip);
        return ResponseEntity.ok(ApiResponse.success("Labour site transfer executed successfully", saved));
    }
}
