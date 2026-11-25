package in.gov.labourerp.dto.business;

public class BusinessModuleDto {
    private Long id;
    private String moduleCode;
    private String moduleName;
    private boolean enabled;

    public BusinessModuleDto() {}

    public BusinessModuleDto(Long id, String moduleCode, String moduleName, boolean enabled) {
        this.id = id;
        this.moduleCode = moduleCode;
        this.moduleName = moduleName;
        this.enabled = enabled;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getModuleCode() { return moduleCode; }
    public void setModuleCode(String moduleCode) { this.moduleCode = moduleCode; }
    public String getModuleName() { return moduleName; }
    public void setModuleName(String moduleName) { this.moduleName = moduleName; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
