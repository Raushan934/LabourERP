package in.gov.labourerp.dto.dashboard;

public class SiteSummaryDto {
    private Long siteId;
    private String siteCode;
    private String siteName;
    private long assignedLabour;
    private long presentCount;
    private long absentCount;
    private long halfDayCount;
    private String status;

    public SiteSummaryDto() {}

    public SiteSummaryDto(Long siteId, String siteCode, String siteName, long assignedLabour, long presentCount, long absentCount, long halfDayCount, String status) {
        this.siteId = siteId;
        this.siteCode = siteCode;
        this.siteName = siteName;
        this.assignedLabour = assignedLabour;
        this.presentCount = presentCount;
        this.absentCount = absentCount;
        this.halfDayCount = halfDayCount;
        this.status = status;
    }

    public Long getSiteId() { return siteId; }
    public void setSiteId(Long siteId) { this.siteId = siteId; }
    public String getSiteCode() { return siteCode; }
    public void setSiteCode(String siteCode) { this.siteCode = siteCode; }
    public String getSiteName() { return siteName; }
    public void setSiteName(String siteName) { this.siteName = siteName; }
    public long getAssignedLabour() { return assignedLabour; }
    public void setAssignedLabour(long assignedLabour) { this.assignedLabour = assignedLabour; }
    public long getPresentCount() { return presentCount; }
    public void setPresentCount(long presentCount) { this.presentCount = presentCount; }
    public long getAbsentCount() { return absentCount; }
    public void setAbsentCount(long absentCount) { this.absentCount = absentCount; }
    public long getHalfDayCount() { return halfDayCount; }
    public void setHalfDayCount(long halfDayCount) { this.halfDayCount = halfDayCount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
