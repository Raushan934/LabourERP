package in.gov.labourerp.dto.payroll;

import java.math.BigDecimal;

public class PayrollItemDto {
    private Long id;
    private Long payrollRunId;
    private Long labourerId;
    private String labourCode;
    private String labourName;
    private String designation;
    private String labourCategory;
    private BigDecimal dailyWageRate;
    private BigDecimal presentDays;
    private BigDecimal halfDays;
    private BigDecimal absentDays;
    private BigDecimal paidLeaveDays;
    private BigDecimal weeklyOffDays;
    private BigDecimal holidayDays;
    private BigDecimal overtimeHours;
    private BigDecimal overtimeAmount;
    private BigDecimal baseEarnings;
    private BigDecimal grossEarnings;
    private BigDecimal advanceRecovery;
    private BigDecimal otherDeductions;
    private BigDecimal netPayable;
    private BigDecimal amountPaid;
    private BigDecimal remainingBalance;
    private String status;
    private String remarks;

    public PayrollItemDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPayrollRunId() { return payrollRunId; }
    public void setPayrollRunId(Long payrollRunId) { this.payrollRunId = payrollRunId; }
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
    public BigDecimal getDailyWageRate() { return dailyWageRate; }
    public void setDailyWageRate(BigDecimal dailyWageRate) { this.dailyWageRate = dailyWageRate; }
    public BigDecimal getPresentDays() { return presentDays; }
    public void setPresentDays(BigDecimal presentDays) { this.presentDays = presentDays; }
    public BigDecimal getHalfDays() { return halfDays; }
    public void setHalfDays(BigDecimal halfDays) { this.halfDays = halfDays; }
    public BigDecimal getAbsentDays() { return absentDays; }
    public void setAbsentDays(BigDecimal absentDays) { this.absentDays = absentDays; }
    public BigDecimal getPaidLeaveDays() { return paidLeaveDays; }
    public void setPaidLeaveDays(BigDecimal paidLeaveDays) { this.paidLeaveDays = paidLeaveDays; }
    public BigDecimal getWeeklyOffDays() { return weeklyOffDays; }
    public void setWeeklyOffDays(BigDecimal weeklyOffDays) { this.weeklyOffDays = weeklyOffDays; }
    public BigDecimal getHolidayDays() { return holidayDays; }
    public void setHolidayDays(BigDecimal holidayDays) { this.holidayDays = holidayDays; }
    public BigDecimal getOvertimeHours() { return overtimeHours; }
    public void setOvertimeHours(BigDecimal overtimeHours) { this.overtimeHours = overtimeHours; }
    public BigDecimal getOvertimeAmount() { return overtimeAmount; }
    public void setOvertimeAmount(BigDecimal overtimeAmount) { this.overtimeAmount = overtimeAmount; }
    public BigDecimal getBaseEarnings() { return baseEarnings; }
    public void setBaseEarnings(BigDecimal baseEarnings) { this.baseEarnings = baseEarnings; }
    public BigDecimal getGrossEarnings() { return grossEarnings; }
    public void setGrossEarnings(BigDecimal grossEarnings) { this.grossEarnings = grossEarnings; }
    public BigDecimal getAdvanceRecovery() { return advanceRecovery; }
    public void setAdvanceRecovery(BigDecimal advanceRecovery) { this.advanceRecovery = advanceRecovery; }
    public BigDecimal getOtherDeductions() { return otherDeductions; }
    public void setOtherDeductions(BigDecimal otherDeductions) { this.otherDeductions = otherDeductions; }
    public BigDecimal getNetPayable() { return netPayable; }
    public void setNetPayable(BigDecimal netPayable) { this.netPayable = netPayable; }
    public BigDecimal getAmountPaid() { return amountPaid; }
    public void setAmountPaid(BigDecimal amountPaid) { this.amountPaid = amountPaid; }
    public BigDecimal getRemainingBalance() { return remainingBalance; }
    public void setRemainingBalance(BigDecimal remainingBalance) { this.remainingBalance = remainingBalance; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
