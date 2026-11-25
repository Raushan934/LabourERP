package in.gov.labourerp.controller;

import in.gov.labourerp.dto.attendance.AttendanceCorrectionRequest;
import in.gov.labourerp.dto.attendance.AttendanceRecordDto;
import in.gov.labourerp.dto.attendance.BulkAttendanceRequest;
import in.gov.labourerp.dto.common.ApiResponse;
import in.gov.labourerp.service.AttendanceService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AttendanceRecordDto>>> getAttendance(
            @RequestParam Long businessId,
            @RequestParam Long siteId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<AttendanceRecordDto> list = attendanceService.getAttendanceForSiteAndDate(businessId, siteId, date);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @PostMapping("/bulk")
    public ResponseEntity<ApiResponse<List<AttendanceRecordDto>>> saveBulkAttendance(
            @Valid @RequestBody BulkAttendanceRequest request,
            Authentication auth,
            HttpServletRequest httpRequest) {
        String username = auth != null ? auth.getName() : "SUPERVISOR";
        String ip = httpRequest.getRemoteAddr();
        List<AttendanceRecordDto> saved = attendanceService.saveBulkAttendance(request, username, ip);
        return ResponseEntity.ok(ApiResponse.success("Daily muster attendance saved successfully", saved));
    }

    @PostMapping("/correct")
    public ResponseEntity<ApiResponse<AttendanceRecordDto>> correctAttendance(
            @Valid @RequestBody AttendanceCorrectionRequest request,
            Authentication auth,
            HttpServletRequest httpRequest) {
        String username = auth != null ? auth.getName() : "ADMIN";
        String ip = httpRequest.getRemoteAddr();
        AttendanceRecordDto updated = attendanceService.correctAttendance(request, username, ip);
        return ResponseEntity.ok(ApiResponse.success("Attendance correction recorded in audit trail", updated));
    }
}
