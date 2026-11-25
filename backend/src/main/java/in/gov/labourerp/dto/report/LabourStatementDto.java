package in.gov.labourerp.dto.report;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class LabourStatementDto {
    private Long labourerId;
    private String labourCode;
    private String fullName;
    private String designation;
    private String labourCategory;
    private BigDecimal dailyWage;
    private String businessName;
    private String currentSiteName;
    private LocalDate startDate;
    private LocalDate endDate;

    // Attendance summary
    private BigDecimal totalPresentDays = BigDecimal.ZERO;
    private BigDecimal totalHalfDays = BigDecimal.ZERO;
    private BigDecimal totalAbsentDays = BigDecimal.ZERO;
    private BigDecimal totalPaidLeaves = BigDecimal.ZERO;
    private BigDecimal totalOvertimeHours = BigDecimal.ZERO;

    // Financial totals
    private BigDecimal grossEarnings = BigDecimal.ZERO;
    private BigDecimal totalAdvancesReceived = BigDecimal.ZERO;
    private BigDecimal totalAdvancesRecovered = BigDecimal.ZERO;
    private BigDecimal outstandingAdvanceBalance = BigDecimal.ZERO;
    private BigDecimal totalPaymentsReceived = BigDecimal.ZERO;
    private BigDecimal netOutstandingPayable = BigDecimal.ZERO;

    private List<AttendanceItemReport> attendanceDetails;
    private List<AdvanceItemReport> advanceDetails;
    private List<PaymentItemReport> paymentDetails;

    public LabourStatementDto() {}

    public static class AttendanceItemReport {
        private LocalDate date;
        private String siteName;
        private String status;
        private BigDecimal overtimeHours;
        private BigDecimal wageEarned;

        public AttendanceItemReport() {}
        public AttendanceItemReport(LocalDate date, String siteName, String status, BigDecimal overtimeHours, BigDecimal wageEarned) {
            this.date = date;
            this.siteName = siteName;
            this.status = status;
            this.overtimeHours = overtimeHours;
            this.wageEarned = wageEarned;
        }
        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }
        public String getSiteName() { return siteName; }
        public void setSiteName(String siteName) { this.siteName = siteName; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public BigDecimal getOvertimeHours() { return overtimeHours; }
        public void setOvertimeHours(BigDecimal overtimeHours) { this.overtimeHours = overtimeHours; }
        public BigDecimal getWageEarned() { return wageEarned; }
        public void setWageEarned(BigDecimal wageEarned) { this.wageEarned = wageEarned; }
    }

    public static class AdvanceItemReport {
        private String advanceCode;
        private LocalDate date;
        private BigDecimal amount;
        private String paymentMethod;
        private String purpose;
        private String recoveryStatus;

        public AdvanceItemReport() {}
        public AdvanceItemReport(String advanceCode, LocalDate date, BigDecimal amount, String paymentMethod, String purpose, String recoveryStatus) {
            this.advanceCode = advanceCode;
            this.date = date;
            this.amount = amount;
            this.paymentMethod = paymentMethod;
            this.purpose = purpose;
            this.recoveryStatus = recoveryStatus;
        }
        public String getAdvanceCode() { return advanceCode; }
        public void setAdvanceCode(String advanceCode) { this.advanceCode = advanceCode; }
        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
        public String getPurpose() { return purpose; }
        public void setPurpose(String purpose) { this.purpose = purpose; }
        public String getRecoveryStatus() { return recoveryStatus; }
        public void setRecoveryStatus(String recoveryStatus) { this.recoveryStatus = recoveryStatus; }
    }

    public static class PaymentItemReport {
        private String paymentCode;
        private LocalDate date;
        private BigDecimal amount;
        private String paymentMethod;
        private String referenceNumber;

        public PaymentItemReport() {}
        public PaymentItemReport(String paymentCode, LocalDate date, BigDecimal amount, String paymentMethod, String referenceNumber) {
            this.paymentCode = paymentCode;
            this.date = date;
            this.amount = amount;
            this.paymentMethod = paymentMethod;
            this.referenceNumber = referenceNumber;
        }
        public String getPaymentCode() { return paymentCode; }
        public void setPaymentCode(String paymentCode) { this.paymentCode = paymentCode; }
        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
        public String getReferenceNumber() { return referenceNumber; }
        public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }
    }

    public Long getLabourerId() { return labourerId; }
    public void setLabourerId(Long labourerId) { this.labourerId = labourerId; }
    public String getLabourCode() { return labourCode; }
    public void setLabourCode(String labourCode) { this.labourCode = labourCode; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }
    public String getLabourCategory() { return labourCategory; }
    public void setLabourCategory(String labourCategory) { this.labourCategory = labourCategory; }
    public BigDecimal getDailyWage() { return dailyWage; }
    public void setDailyWage(BigDecimal dailyWage) { this.dailyWage = dailyWage; }
    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }
    public String getCurrentSiteName() { return currentSiteName; }
    public void setCurrentSiteName(String currentSiteName) { this.currentSiteName = currentSiteName; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public BigDecimal getTotalPresentDays() { return totalPresentDays; }
    public void setTotalPresentDays(BigDecimal totalPresentDays) { this.totalPresentDays = totalPresentDays; }
    public BigDecimal getTotalHalfDays() { return totalHalfDays; }
    public void setTotalHalfDays(BigDecimal totalHalfDays) { this.totalHalfDays = totalHalfDays; }
    public BigDecimal getTotalAbsentDays() { return totalAbsentDays; }
    public void setTotalAbsentDays(BigDecimal totalAbsentDays) { this.totalAbsentDays = totalAbsentDays; }
    public BigDecimal getTotalPaidLeaves() { return totalPaidLeaves; }
    public void setTotalPaidLeaves(BigDecimal totalPaidLeaves) { this.totalPaidLeaves = totalPaidLeaves; }
    public BigDecimal getTotalOvertimeHours() { return totalOvertimeHours; }
    public void setTotalOvertimeHours(BigDecimal totalOvertimeHours) { this.totalOvertimeHours = totalOvertimeHours; }
    public BigDecimal getGrossEarnings() { return grossEarnings; }
    public void setGrossEarnings(BigDecimal grossEarnings) { this.grossEarnings = grossEarnings; }
    public BigDecimal getTotalAdvancesReceived() { return totalAdvancesReceived; }
    public void setTotalAdvancesReceived(BigDecimal totalAdvancesReceived) { this.totalAdvancesReceived = totalAdvancesReceived; }
    public BigDecimal getTotalAdvancesRecovered() { return totalAdvancesRecovered; }
    public void setTotalAdvancesRecovered(BigDecimal totalAdvancesRecovered) { this.totalAdvancesRecovered = totalAdvancesRecovered; }
    public BigDecimal getOutstandingAdvanceBalance() { return outstandingAdvanceBalance; }
    public void setOutstandingAdvanceBalance(BigDecimal outstandingAdvanceBalance) { this.outstandingAdvanceBalance = outstandingAdvanceBalance; }
    public BigDecimal getTotalPaymentsReceived() { return totalPaymentsReceived; }
    public void setTotalPaymentsReceived(BigDecimal totalPaymentsReceived) { this.totalPaymentsReceived = totalPaymentsReceived; }
    public BigDecimal getNetOutstandingPayable() { return netOutstandingPayable; }
    public void setNetOutstandingPayable(BigDecimal netOutstandingPayable) { this.netOutstandingPayable = netOutstandingPayable; }
    public List<AttendanceItemReport> getAttendanceDetails() { return attendanceDetails; }
    public void setAttendanceDetails(List<AttendanceItemReport> attendanceDetails) { this.attendanceDetails = attendanceDetails; }
    public List<AdvanceItemReport> getAdvanceDetails() { return advanceDetails; }
    public void setAdvanceDetails(List<AdvanceItemReport> advanceDetails) { this.advanceDetails = advanceDetails; }
    public List<PaymentItemReport> getPaymentDetails() { return paymentDetails; }
    public void setPaymentDetails(List<PaymentItemReport> paymentDetails) { this.paymentDetails = paymentDetails; }
}
