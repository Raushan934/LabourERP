package in.gov.labourerp.dto.auth;

import java.util.List;

public class UserDto {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String phone;
    private String status;
    private List<String> roles;
    private Long assignedSiteId;
    private String assignedSiteName;
    private String assignedSiteCode;
    private String upiId;
    private String bankAccountNumber;
    private String bankIfscCode;
    private String bankName;
    private String bankBeneficiaryName;
    private String qrCodeUrl;

    public UserDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }

    public Long getAssignedSiteId() { return assignedSiteId; }
    public void setAssignedSiteId(Long assignedSiteId) { this.assignedSiteId = assignedSiteId; }
    public String getAssignedSiteName() { return assignedSiteName; }
    public void setAssignedSiteName(String assignedSiteName) { this.assignedSiteName = assignedSiteName; }
    public String getAssignedSiteCode() { return assignedSiteCode; }
    public void setAssignedSiteCode(String assignedSiteCode) { this.assignedSiteCode = assignedSiteCode; }

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
