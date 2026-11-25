package in.gov.labourerp.dto.site;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.Map;

public class SiteDto {
    private Long id;
    private Long businessId;

    @NotBlank(message = "Site code is required")
    private String siteCode;

    @NotBlank(message = "Site name is required")
    private String siteName;

    private String clientName;
    private String siteCategory;
    private String locationAddress;
    private String city;
    private String state;
    private Long siteManagerId;
    private String siteManagerName;
    private Long supervisorId;
    private String supervisorName;
    private LocalDate startDate;
    private LocalDate expectedEndDate;
    private String status = "ACTIVE";
    private String notes;
    private long assignedLabourCount;
    private Map<String, String> customFields;

    public SiteDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getBusinessId() { return businessId; }
    public void setBusinessId(Long businessId) { this.businessId = businessId; }
    public String getSiteCode() { return siteCode; }
    public void setSiteCode(String siteCode) { this.siteCode = siteCode; }
    public String getSiteName() { return siteName; }
    public void setSiteName(String siteName) { this.siteName = siteName; }
    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }
    public String getSiteCategory() { return siteCategory; }
    public void setSiteCategory(String siteCategory) { this.siteCategory = siteCategory; }
    public String getLocationAddress() { return locationAddress; }
    public void setLocationAddress(String locationAddress) { this.locationAddress = locationAddress; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public Long getSiteManagerId() { return siteManagerId; }
    public void setSiteManagerId(Long siteManagerId) { this.siteManagerId = siteManagerId; }
    public String getSiteManagerName() { return siteManagerName; }
    public void setSiteManagerName(String siteManagerName) { this.siteManagerName = siteManagerName; }
    public Long getSupervisorId() { return supervisorId; }
    public void setSupervisorId(Long supervisorId) { this.supervisorId = supervisorId; }
    public String getSupervisorName() { return supervisorName; }
    public void setSupervisorName(String supervisorName) { this.supervisorName = supervisorName; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getExpectedEndDate() { return expectedEndDate; }
    public void setExpectedEndDate(LocalDate expectedEndDate) { this.expectedEndDate = expectedEndDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public long getAssignedLabourCount() { return assignedLabourCount; }
    public void setAssignedLabourCount(long assignedLabourCount) { this.assignedLabourCount = assignedLabourCount; }
    public Map<String, String> getCustomFields() { return customFields; }
    public void setCustomFields(Map<String, String> customFields) { this.customFields = customFields; }
}
