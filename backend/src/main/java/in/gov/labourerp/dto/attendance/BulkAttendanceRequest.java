package in.gov.labourerp.dto.attendance;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class BulkAttendanceRequest {
    @NotNull(message = "Business ID is required")
    private Long businessId;

    @NotNull(message = "Site ID is required")
    private Long siteId;

    @NotNull(message = "Attendance date is required")
    private LocalDate attendanceDate;

    @NotEmpty(message = "Attendance items cannot be empty")
    private List<AttendanceItem> items;

    public BulkAttendanceRequest() {}

    public static class AttendanceItem {
        @NotNull(message = "Labourer ID is required")
        private Long labourerId;

        @NotNull(message = "Status is required")
        private String status;

        private BigDecimal overtimeHours = BigDecimal.ZERO;
        private String remarks;

        public AttendanceItem() {}

        public Long getLabourerId() { return labourerId; }
        public void setLabourerId(Long labourerId) { this.labourerId = labourerId; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public BigDecimal getOvertimeHours() { return overtimeHours; }
        public void setOvertimeHours(BigDecimal overtimeHours) { this.overtimeHours = overtimeHours; }
        public String getRemarks() { return remarks; }
        public void setRemarks(String remarks) { this.remarks = remarks; }
    }

    public Long getBusinessId() { return businessId; }
    public void setBusinessId(Long businessId) { this.businessId = businessId; }
    public Long getSiteId() { return siteId; }
    public void setSiteId(Long siteId) { this.siteId = siteId; }
    public LocalDate getAttendanceDate() { return attendanceDate; }
    public void setAttendanceDate(LocalDate attendanceDate) { this.attendanceDate = attendanceDate; }
    public List<AttendanceItem> getItems() { return items; }
    public void setItems(List<AttendanceItem> items) { this.items = items; }
}
