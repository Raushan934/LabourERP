package in.gov.labourerp.dto.auth;

import java.util.List;

public class LoginResponse {
    private String token;
    private String tokenType = "Bearer";
    private Long userId;
    private String username;
    private String fullName;
    private String email;
    private List<String> roles;
    private List<AccessibleBusinessDto> accessibleBusinesses;
    private Long primaryBusinessId;
    private Long assignedSiteId;
    private String assignedSiteName;
    private String assignedSiteCode;

    public LoginResponse() {}

    public static class AccessibleBusinessDto {
        private Long id;
        private String businessCode;
        private String businessName;
        private String businessType;
        private String role;

        public AccessibleBusinessDto() {}

        public AccessibleBusinessDto(Long id, String businessCode, String businessName, String businessType, String role) {
            this.id = id;
            this.businessCode = businessCode;
            this.businessName = businessName;
            this.businessType = businessType;
            this.role = role;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getBusinessCode() { return businessCode; }
        public void setBusinessCode(String businessCode) { this.businessCode = businessCode; }
        public String getBusinessName() { return businessName; }
        public void setBusinessName(String businessName) { this.businessName = businessName; }
        public String getBusinessType() { return businessType; }
        public void setBusinessType(String businessType) { this.businessType = businessType; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }

    public List<AccessibleBusinessDto> getAccessibleBusinesses() { return accessibleBusinesses; }
    public void setAccessibleBusinesses(List<AccessibleBusinessDto> accessibleBusinesses) { this.accessibleBusinesses = accessibleBusinesses; }

    public Long getPrimaryBusinessId() { return primaryBusinessId; }
    public void setPrimaryBusinessId(Long primaryBusinessId) { this.primaryBusinessId = primaryBusinessId; }

    public Long getAssignedSiteId() { return assignedSiteId; }
    public void setAssignedSiteId(Long assignedSiteId) { this.assignedSiteId = assignedSiteId; }

    public String getAssignedSiteName() { return assignedSiteName; }
    public void setAssignedSiteName(String assignedSiteName) { this.assignedSiteName = assignedSiteName; }

    public String getAssignedSiteCode() { return assignedSiteCode; }
    public void setAssignedSiteCode(String assignedSiteCode) { this.assignedSiteCode = assignedSiteCode; }
}
