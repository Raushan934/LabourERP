package in.gov.labourerp.dto.finance;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public class AdvanceDto {
    private Long id;
    private Long businessId;
    private Long siteId;
    private String siteName;

    @NotNull(message = "Labourer ID is required")
    private Long labourerId;
    private String labourCode;
    private String labourName;

    private String advanceCode;

    @NotNull(message = "Advance date is required")
    private LocalDate advanceDate;

    @NotNull(message = "Amount is required")
    private BigDecimal amount;

    private String paymentMethod = "CASH";
    private String advanceType = "PERSONAL_ADVANCE";
    private String referenceNumber;
    private String purpose;
    private String approvedBy;
    private BigDecimal recoveredAmount = BigDecimal.ZERO;
    private BigDecimal remainingBalance = BigDecimal.ZERO;
    private String recoveryStatus = "OPEN";
    private String remarks;

    public AdvanceDto() {}

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
    public String getAdvanceCode() { return advanceCode; }
    public void setAdvanceCode(String advanceCode) { this.advanceCode = advanceCode; }
    public LocalDate getAdvanceDate() { return advanceDate; }
    public void setAdvanceDate(LocalDate advanceDate) { this.advanceDate = advanceDate; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getAdvanceType() { return advanceType; }
    public void setAdvanceType(String advanceType) { this.advanceType = advanceType; }
    public String getReferenceNumber() { return referenceNumber; }
    public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }
    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }
    public BigDecimal getRecoveredAmount() { return recoveredAmount; }
    public void setRecoveredAmount(BigDecimal recoveredAmount) { this.recoveredAmount = recoveredAmount; }
    public BigDecimal getRemainingBalance() { return remainingBalance; }
    public void setRemainingBalance(BigDecimal remainingBalance) { this.remainingBalance = remainingBalance; }
    public String getRecoveryStatus() { return recoveryStatus; }
    public void setRecoveryStatus(String recoveryStatus) { this.recoveryStatus = recoveryStatus; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
