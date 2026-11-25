package in.gov.labourerp.controller;

import in.gov.labourerp.dto.common.ApiResponse;
import in.gov.labourerp.dto.report.LabourStatementDto;
import in.gov.labourerp.dto.report.SiteMonthlyReportDto;
import in.gov.labourerp.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/labour-statement/{labourId}")
    public ResponseEntity<ApiResponse<LabourStatementDto>> getLabourStatement(
            @PathVariable Long labourId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        LabourStatementDto statement = reportService.getLabourStatement(labourId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(statement));
    }

    @GetMapping("/labour-statement/{labourId}/csv")
    public ResponseEntity<String> exportLabourStatementCsv(
            @PathVariable Long labourId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        LabourStatementDto statement = reportService.getLabourStatement(labourId, startDate, endDate);
        String csv = reportService.generateCsvStatement(statement);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"statement_" + statement.getLabourCode() + ".csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csv);
    }

    @GetMapping("/site-monthly")
    public ResponseEntity<ApiResponse<SiteMonthlyReportDto>> getSiteMonthlyReport(
            @RequestParam Long siteId,
            @RequestParam int month,
            @RequestParam int year) {
        SiteMonthlyReportDto report = reportService.getSiteMonthlyReport(siteId, month, year);
        return ResponseEntity.ok(ApiResponse.success(report));
    }
}
