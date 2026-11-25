package in.gov.labourerp.dto.labour;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public class LabourerDto {
    private Long id;
    private Long businessId;

    @NotBlank(message = "Labour ID / Code is required")
    private String labourCode;

    @NotBlank(message = "Full name is required")
    private String fullName;

    private String photoUrl;

    @NotBlank(message = "Mobile number is required")
    private String mobileNumber;

    private String altMobileNumber;
    private String aadharNumber;
    private String address;
    private String emergencyContactName;
    private String emergencyContactPhone;

    @NotNull(message = "Date of joining is required")
    private LocalDate dateOfJoining;

    @NotBlank(message = "Designation is required")
    private String designation;

    @NotBlank(message = "Labour category is required")
    private String labourCategory;

    private String skillLevel = "SKILLED";

    @NotNull(message = "Daily wage is required")
    private BigDecimal dailyWage;

    private BigDecimal overtimeHourlyRate = BigDecimal.ZERO;
    private Long currentSiteId;
    private String currentSiteName;
    private String status = "ACTIVE"; // ACTIVE, INACTIVE, TERMINATED
    private String upiId;
    private String bankAccountNumber;
    private String bankIfscCode;
    private String bankName;
    private String bankBeneficiaryName;
    private String qrCodeUrl;
    private String notes;
    private BigDecimal outstandingAdvance = BigDecimal.ZERO;
    private Map<String, String> customFields;

    public LabourerDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getBusinessId() { return businessId; }
    public void setBusinessId(Long businessId) { this.businessId = businessId; }
    public String getLabourCode() { return labourCode; }
    public void setLabourCode(String labourCode) { this.labourCode = labourCode; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }
    public String getAltMobileNumber() { return altMobileNumber; }
    public void setAltMobileNumber(String altMobileNumber) { this.altMobileNumber = altMobileNumber; }
    public String getAadharNumber() { return aadharNumber; }
    public void setAadharNumber(String aadharNumber) { this.aadharNumber = aadharNumber; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getEmergencyContactName() { return emergencyContactName; }
    public void setEmergencyContactName(String emergencyContactName) { this.emergencyContactName = emergencyContactName; }
    public String getEmergencyContactPhone() { return emergencyContactPhone; }
    public void setEmergencyContactPhone(String emergencyContactPhone) { this.emergencyContactPhone = emergencyContactPhone; }
    public LocalDate getDateOfJoining() { return dateOfJoining; }
    public void setDateOfJoining(LocalDate dateOfJoining) { this.dateOfJoining = dateOfJoining; }
    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }
    public String getLabourCategory() { return labourCategory; }
    public void setLabourCategory(String labourCategory) { this.labourCategory = labourCategory; }
    public String getSkillLevel() { return skillLevel; }
    public void setSkillLevel(String skillLevel) { this.skillLevel = skillLevel; }
    public BigDecimal getDailyWage() { return dailyWage; }
    public void setDailyWage(BigDecimal dailyWage) { this.dailyWage = dailyWage; }
    public BigDecimal getOvertimeHourlyRate() { return overtimeHourlyRate; }
    public void setOvertimeHourlyRate(BigDecimal overtimeHourlyRate) { this.overtimeHourlyRate = overtimeHourlyRate; }
    public Long getCurrentSiteId() { return currentSiteId; }
    public void setCurrentSiteId(Long currentSiteId) { this.currentSiteId = currentSiteId; }
    public String getCurrentSiteName() { return currentSiteName; }
    public void setCurrentSiteName(String currentSiteName) { this.currentSiteName = currentSiteName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
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
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public BigDecimal getOutstandingAdvance() { return outstandingAdvance; }
    public void setOutstandingAdvance(BigDecimal outstandingAdvance) { this.outstandingAdvance = outstandingAdvance; }
    public Map<String, String> getCustomFields() { return customFields; }
    public void setCustomFields(Map<String, String> customFields) { this.customFields = customFields; }
}
