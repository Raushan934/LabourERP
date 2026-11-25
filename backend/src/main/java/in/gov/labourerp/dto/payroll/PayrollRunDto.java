package in.gov.labourerp.dto.payroll;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class PayrollRunDto {
    private Long id;
    private Long businessId;
    private Long siteId;
    private String siteName;
    private String runCode;
    private int periodMonth;
    private int periodYear;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private int totalWorkers;
    private BigDecimal totalGrossEarnings;
    private BigDecimal totalAdvanceDeductions;
    private BigDecimal totalOtherDeductions;
    private BigDecimal totalNetPayable;
    private BigDecimal totalPaidAmount;
    private BigDecimal totalOutstandingBalance;
    private String generatedBy;
    private String approvedBy;
    private LocalDateTime createdAt;
    private List<PayrollItemDto> items;

    public PayrollRunDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getBusinessId() { return businessId; }
    public void setBusinessId(Long businessId) { this.businessId = businessId; }
    public Long getSiteId() { return siteId; }
    public void setSiteId(Long siteId) { this.siteId = siteId; }
    public String getSiteName() { return siteName; }
    public void setSiteName(String siteName) { this.siteName = siteName; }
    public String getRunCode() { return runCode; }
    public void setRunCode(String runCode) { this.runCode = runCode; }
    public int getPeriodMonth() { return periodMonth; }
    public void setPeriodMonth(int periodMonth) { this.periodMonth = periodMonth; }
    public int getPeriodYear() { return periodYear; }
    public void setPeriodYear(int periodYear) { this.periodYear = periodYear; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getTotalWorkers() { return totalWorkers; }
    public void setTotalWorkers(int totalWorkers) { this.totalWorkers = totalWorkers; }
    public BigDecimal getTotalGrossEarnings() { return totalGrossEarnings; }
    public void setTotalGrossEarnings(BigDecimal totalGrossEarnings) { this.totalGrossEarnings = totalGrossEarnings; }
    public BigDecimal getTotalAdvanceDeductions() { return totalAdvanceDeductions; }
    public void setTotalAdvanceDeductions(BigDecimal totalAdvanceDeductions) { this.totalAdvanceDeductions = totalAdvanceDeductions; }
    public BigDecimal getTotalOtherDeductions() { return totalOtherDeductions; }
    public void setTotalOtherDeductions(BigDecimal totalOtherDeductions) { this.totalOtherDeductions = totalOtherDeductions; }
    public BigDecimal getTotalNetPayable() { return totalNetPayable; }
    public void setTotalNetPayable(BigDecimal totalNetPayable) { this.totalNetPayable = totalNetPayable; }
    public BigDecimal getTotalPaidAmount() { return totalPaidAmount; }
    public void setTotalPaidAmount(BigDecimal totalPaidAmount) { this.totalPaidAmount = totalPaidAmount; }
    public BigDecimal getTotalOutstandingBalance() { return totalOutstandingBalance; }
    public void setTotalOutstandingBalance(BigDecimal totalOutstandingBalance) { this.totalOutstandingBalance = totalOutstandingBalance; }
    public String getGeneratedBy() { return generatedBy; }
    public void setGeneratedBy(String generatedBy) { this.generatedBy = generatedBy; }
    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public List<PayrollItemDto> getItems() { return items; }
    public void setItems(List<PayrollItemDto> items) { this.items = items; }
}
