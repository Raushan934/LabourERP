package in.gov.labourerp.dto.master;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public class MasterValueDto {
    private Long id;
    private Long categoryId;
    private String categoryCode;
    private Long businessId;

    @NotBlank(message = "Code is required")
    private String code;

    @NotBlank(message = "Name is required")
    private String name;

    private String description;
    private String colorCode = "#1B3557";
    private boolean isPayable = true;
    private BigDecimal wagePercentage = new BigDecimal("100.00");
    private int sortOrder = 0;
    private boolean isActive = true;

    public MasterValueDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getCategoryCode() { return categoryCode; }
    public void setCategoryCode(String categoryCode) { this.categoryCode = categoryCode; }
    public Long getBusinessId() { return businessId; }
    public void setBusinessId(Long businessId) { this.businessId = businessId; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getColorCode() { return colorCode; }
    public void setColorCode(String colorCode) { this.colorCode = colorCode; }
    public boolean isPayable() { return isPayable; }
    public void setPayable(boolean payable) { isPayable = payable; }
    public BigDecimal getWagePercentage() { return wagePercentage; }
    public void setWagePercentage(BigDecimal wagePercentage) { this.wagePercentage = wagePercentage; }
    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}
