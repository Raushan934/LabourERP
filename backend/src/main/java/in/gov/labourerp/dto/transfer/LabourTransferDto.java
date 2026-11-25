package in.gov.labourerp.dto.transfer;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class LabourTransferDto {
    private Long id;
    private Long businessId;

    @NotNull(message = "Labourer ID is required")
    private Long labourerId;
    private String labourCode;
    private String labourName;
    private String designation;

    private Long fromSiteId;
    private String fromSiteName;

    @NotNull(message = "To Site ID is required")
    private Long toSiteId;
    private String toSiteName;

    @NotNull(message = "Transfer date is required")
    private LocalDate transferDate;

    @NotNull(message = "Effective date is required")
    private LocalDate effectiveDate;

    private String reason;
    private String remarks;
    private String transferredBy;

    public LabourTransferDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getBusinessId() { return businessId; }
    public void setBusinessId(Long businessId) { this.businessId = businessId; }
    public Long getLabourerId() { return labourerId; }
    public void setLabourerId(Long labourerId) { this.labourerId = labourerId; }
    public String getLabourCode() { return labourCode; }
    public void setLabourCode(String labourCode) { this.labourCode = labourCode; }
    public String getLabourName() { return labourName; }
    public void setLabourName(String labourName) { this.labourName = labourName; }
    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }
    public Long getFromSiteId() { return fromSiteId; }
    public void setFromSiteId(Long fromSiteId) { this.fromSiteId = fromSiteId; }
    public String getFromSiteName() { return fromSiteName; }
    public void setFromSiteName(String fromSiteName) { this.fromSiteName = fromSiteName; }
    public Long getToSiteId() { return toSiteId; }
    public void setToSiteId(Long toSiteId) { this.toSiteId = toSiteId; }
    public String getToSiteName() { return toSiteName; }
    public void setToSiteName(String toSiteName) { this.toSiteName = toSiteName; }
    public LocalDate getTransferDate() { return transferDate; }
    public void setTransferDate(LocalDate transferDate) { this.transferDate = transferDate; }
    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    public String getTransferredBy() { return transferredBy; }
    public void setTransferredBy(String transferredBy) { this.transferredBy = transferredBy; }
}
