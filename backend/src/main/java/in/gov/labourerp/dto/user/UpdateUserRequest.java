package in.gov.labourerp.dto.user;

import jakarta.validation.constraints.NotBlank;

public class UpdateUserRequest {

    private String fullName;
    private String phone;
    private String status; // ACTIVE, INACTIVE, SUSPENDED
    private String role;
    private Long assignedSiteId;
    private String upiId;
    private String bankAccountNumber;
    private String bankIfscCode;
    private String bankName;
    private String bankBeneficiaryName;
    private String qrCodeUrl;

    public UpdateUserRequest() {}

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Long getAssignedSiteId() { return assignedSiteId; }
    public void setAssignedSiteId(Long assignedSiteId) { this.assignedSiteId = assignedSiteId; }

    public String getUpiId() { return upiId; }
    public void setUpiId(String upiId) { this.upiId = upiId; }

    public String getBankAccountNumber() { return bankAccountNumber; }
    public void setBankAccountNumber(String bankAccountNumber) { this.bankAccountNumber = bankAccountNumber; }

    public String getBankIfscCode() { return bankIfscCode; }
    public void setBankIfscCode(String bankIfscCode) { this.bankIfscCode = bankIfscCode; }

    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }

    public String getBankBeneficiaryName() { return bankBeneficiaryName; }
    public void setBankBeneficiaryName(String bankBeneficiaryName) { this.bankBeneficiaryName = bankBeneficiaryName; }

    public String getQrCodeUrl() { return qrCodeUrl; }
    public void setQrCodeUrl(String qrCodeUrl) { this.qrCodeUrl = qrCodeUrl; }
}
