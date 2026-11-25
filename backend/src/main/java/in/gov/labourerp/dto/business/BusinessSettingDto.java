package in.gov.labourerp.dto.business;

public class BusinessSettingDto {
    private Long id;
    private String settingKey;
    private String settingValue;
    private String dataType;
    private String description;

    public BusinessSettingDto() {}

    public BusinessSettingDto(Long id, String settingKey, String settingValue, String dataType, String description) {
        this.id = id;
        this.settingKey = settingKey;
        this.settingValue = settingValue;
        this.dataType = dataType;
        this.description = description;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSettingKey() { return settingKey; }
    public void setSettingKey(String settingKey) { this.settingKey = settingKey; }
    public String getSettingValue() { return settingValue; }
    public void setSettingValue(String settingValue) { this.settingValue = settingValue; }
    public String getDataType() { return dataType; }
    public void setDataType(String dataType) { this.dataType = dataType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
