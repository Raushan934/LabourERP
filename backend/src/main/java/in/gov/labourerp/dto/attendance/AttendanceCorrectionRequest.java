package in.gov.labourerp.dto.attendance;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class AttendanceCorrectionRequest {
    @NotNull(message = "Attendance record ID is required")
    private Long attendanceRecordId;

    @NotBlank(message = "New status is required")
    private String newStatus;

    private BigDecimal newOvertimeHours = BigDecimal.ZERO;

    @NotBlank(message = "Reason for modification is required")
    private String reason;

    public AttendanceCorrectionRequest() {}

    public Long getAttendanceRecordId() { return attendanceRecordId; }
    public void setAttendanceRecordId(Long attendanceRecordId) { this.attendanceRecordId = attendanceRecordId; }
    public String getNewStatus() { return newStatus; }
    public void setNewStatus(String newStatus) { this.newStatus = newStatus; }
    public BigDecimal getNewOvertimeHours() { return newOvertimeHours; }
    public void setNewOvertimeHours(BigDecimal newOvertimeHours) { this.newOvertimeHours = newOvertimeHours; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
