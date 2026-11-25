package in.gov.labourerp.controller;

import in.gov.labourerp.dto.common.ApiResponse;
import in.gov.labourerp.dto.dashboard.DashboardSummaryDto;
import in.gov.labourerp.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DashboardSummaryDto>> getDashboardSummary(@RequestParam Long businessId) {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getDashboardSummary(businessId)));
    }
}
