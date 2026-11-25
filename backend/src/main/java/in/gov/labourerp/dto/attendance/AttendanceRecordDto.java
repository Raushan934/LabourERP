package in.gov.labourerp.dto.attendance;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public class AttendanceRecordDto {
    private Long id;
    private Long businessId;
    private Long siteId;
    private String siteName;

    @NotNull(message = "Labourer ID is required")
    private Long labourerId;
    private String labourCode;
    private String labourName;
    private String designation;
    private String labourCategory;

    @NotNull(message = "Attendance date is required")
    private LocalDate attendanceDate;

    @NotNull(message = "Status is required")
    private String status; // PRESENT, HALF_DAY, ABSENT, PAID_LEAVE, WEEKLY_OFF, HOLIDAY

    private BigDecimal overtimeHours = BigDecimal.ZERO;
    private BigDecimal wageMultiplier = BigDecimal.ONE;
    private BigDecimal dailyWageApplied = BigDecimal.ZERO;
    private BigDecimal calculatedWage = BigDecimal.ZERO;
    private BigDecimal overtimeAmount = BigDecimal.ZERO;
    private String remarks;
    private String markedBy;

    public AttendanceRecordDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getBusinessId() { return businessId; }
    public void setBusinessId(Long businessId) { this.businessId = businessId; }
    public Long getSiteId() { return siteId; }
    public void setSiteId(Long siteId) { this.siteId = siteId; }
    public String getSiteName() { return siteName; }
    public void setSiteName(String siteName) { this.siteName = siteName; }
    public Long getLabourerId() { return labourerId; }
    public void setLabourerId(Long labourerId) { this.labourerId = labourerId; }
    public String getLabourCode() { return labourCode; }
    public void setLabourCode(String labourCode) { this.labourCode = labourCode; }
    public String getLabourName() { return labourName; }
    public void setLabourName(String labourName) { this.labourName = labourName; }
    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }
    public String getLabourCategory() { return labourCategory; }
    public void setLabourCategory(String labourCategory) { this.labourCategory = labourCategory; }
    public LocalDate getAttendanceDate() { return attendanceDate; }
    public void setAttendanceDate(LocalDate attendanceDate) { this.attendanceDate = attendanceDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BigDecimal getOvertimeHours() { return overtimeHours; }
    public void setOvertimeHours(BigDecimal overtimeHours) { this.overtimeHours = overtimeHours; }
    public BigDecimal getWageMultiplier() { return wageMultiplier; }
    public void setWageMultiplier(BigDecimal wageMultiplier) { this.wageMultiplier = wageMultiplier; }
    public BigDecimal getDailyWageApplied() { return dailyWageApplied; }
    public void setDailyWageApplied(BigDecimal dailyWageApplied) { this.dailyWageApplied = dailyWageApplied; }
    public BigDecimal getCalculatedWage() { return calculatedWage; }
    public void setCalculatedWage(BigDecimal calculatedWage) { this.calculatedWage = calculatedWage; }
    public BigDecimal getOvertimeAmount() { return overtimeAmount; }
    public void setOvertimeAmount(BigDecimal overtimeAmount) { this.overtimeAmount = overtimeAmount; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    public String getMarkedBy() { return markedBy; }
    public void setMarkedBy(String markedBy) { this.markedBy = markedBy; }
}
