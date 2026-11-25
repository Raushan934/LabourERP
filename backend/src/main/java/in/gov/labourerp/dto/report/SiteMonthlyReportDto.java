package in.gov.labourerp.dto.report;

import java.math.BigDecimal;
import java.util.List;

public class SiteMonthlyReportDto {
    private Long siteId;
    private String siteCode;
    private String siteName;
    private String clientName;
    private int month;
    private int year;
    private long totalAssignedLabour;
    private BigDecimal totalPresentDays;
    private BigDecimal totalOvertimeHours;
    private BigDecimal totalGrossWages;
    private BigDecimal totalAdvancesDisbursed;
    private BigDecimal totalPaymentsMade;
    private BigDecimal totalOutstandingBalance;
    private List<LabourRowSummary> workerRows;

    public SiteMonthlyReportDto() {}

    public static class LabourRowSummary {
        private Long labourerId;
        private String labourCode;
        private String fullName;
        private String designation;
        private BigDecimal dailyWage;
        private BigDecimal presentDays;
        private BigDecimal halfDays;
        private BigDecimal absentDays;
        private BigDecimal overtimeHours;
        private BigDecimal grossEarnings;
        private BigDecimal advancesDisbursed;
        private BigDecimal paymentsMade;
        private BigDecimal balanceDue;

        public LabourRowSummary() {}

        public Long getLabourerId() { return labourerId; }
        public void setLabourerId(Long labourerId) { this.labourerId = labourerId; }
        public String getLabourCode() { return labourCode; }
        public void setLabourCode(String labourCode) { this.labourCode = labourCode; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getDesignation() { return designation; }
        public void setDesignation(String designation) { this.designation = designation; }
        public BigDecimal getDailyWage() { return dailyWage; }
        public void setDailyWage(BigDecimal dailyWage) { this.dailyWage = dailyWage; }
        public BigDecimal getPresentDays() { return presentDays; }
        public void setPresentDays(BigDecimal presentDays) { this.presentDays = presentDays; }
        public BigDecimal getHalfDays() { return halfDays; }
        public void setHalfDays(BigDecimal halfDays) { this.halfDays = halfDays; }
        public BigDecimal getAbsentDays() { return absentDays; }
        public void setAbsentDays(BigDecimal absentDays) { this.absentDays = absentDays; }
        public BigDecimal getOvertimeHours() { return overtimeHours; }
        public void setOvertimeHours(BigDecimal overtimeHours) { this.overtimeHours = overtimeHours; }
        public BigDecimal getGrossEarnings() { return grossEarnings; }
        public void setGrossEarnings(BigDecimal grossEarnings) { this.grossEarnings = grossEarnings; }
        public BigDecimal getAdvancesDisbursed() { return advancesDisbursed; }
        public void setAdvancesDisbursed(BigDecimal advancesDisbursed) { this.advancesDisbursed = advancesDisbursed; }
        public BigDecimal getPaymentsMade() { return paymentsMade; }
        public void setPaymentsMade(BigDecimal paymentsMade) { this.paymentsMade = paymentsMade; }
        public BigDecimal getBalanceDue() { return balanceDue; }
        public void setBalanceDue(BigDecimal balanceDue) { this.balanceDue = balanceDue; }
    }

    public Long getSiteId() { return siteId; }
    public void setSiteId(Long siteId) { this.siteId = siteId; }
    public String getSiteCode() { return siteCode; }
    public void setSiteCode(String siteCode) { this.siteCode = siteCode; }
    public String getSiteName() { return siteName; }
    public void setSiteName(String siteName) { this.siteName = siteName; }
    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }
    public int getMonth() { return month; }
    public void setMonth(int month) { this.month = month; }
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
    public long getTotalAssignedLabour() { return totalAssignedLabour; }
    public void setTotalAssignedLabour(long totalAssignedLabour) { this.totalAssignedLabour = totalAssignedLabour; }
    public BigDecimal getTotalPresentDays() { return totalPresentDays; }
    public void setTotalPresentDays(BigDecimal totalPresentDays) { this.totalPresentDays = totalPresentDays; }
    public BigDecimal getTotalOvertimeHours() { return totalOvertimeHours; }
    public void setTotalOvertimeHours(BigDecimal totalOvertimeHours) { this.totalOvertimeHours = totalOvertimeHours; }
    public BigDecimal getTotalGrossWages() { return totalGrossWages; }
    public void setTotalGrossWages(BigDecimal totalGrossWages) { this.totalGrossWages = totalGrossWages; }
    public BigDecimal getTotalAdvancesDisbursed() { return totalAdvancesDisbursed; }
    public void setTotalAdvancesDisbursed(BigDecimal totalAdvancesDisbursed) { this.totalAdvancesDisbursed = totalAdvancesDisbursed; }
    public BigDecimal getTotalPaymentsMade() { return totalPaymentsMade; }
    public void setTotalPaymentsMade(BigDecimal totalPaymentsMade) { this.totalPaymentsMade = totalPaymentsMade; }
    public BigDecimal getTotalOutstandingBalance() { return totalOutstandingBalance; }
    public void setTotalOutstandingBalance(BigDecimal totalOutstandingBalance) { this.totalOutstandingBalance = totalOutstandingBalance; }
    public List<LabourRowSummary> getWorkerRows() { return workerRows; }
    public void setWorkerRows(List<LabourRowSummary> workerRows) { this.workerRows = workerRows; }
}
