package in.gov.labourerp.dto.dashboard;

import java.math.BigDecimal;
import java.util.List;

public class DashboardSummaryDto {
    private long totalBusinesses;
    private long activeBusinesses;
    private long totalActiveSites;
    private long totalActiveLabourers;
    private long totalSupervisors;

    // Today's Attendance breakdown
    private long todayPresent;
    private long todayHalfDay;
    private long todayAbsent;
    private long todayPaidLeave;
    private long todayNotSubmitted;

    // Financial Overview
    private BigDecimal periodTotalWages = BigDecimal.ZERO;
    private BigDecimal periodPaymentsMade = BigDecimal.ZERO;
    private BigDecimal periodOutstandingWages = BigDecimal.ZERO;
    private BigDecimal totalOutstandingAdvances = BigDecimal.ZERO;
    private BigDecimal pendingPaymentAmount = BigDecimal.ZERO;

    // Site overview table
    private List<SiteSummaryDto> siteSummaries;

    public DashboardSummaryDto() {}

    public long getTotalBusinesses() { return totalBusinesses; }
    public void setTotalBusinesses(long totalBusinesses) { this.totalBusinesses = totalBusinesses; }
    public long getActiveBusinesses() { return activeBusinesses; }
    public void setActiveBusinesses(long activeBusinesses) { this.activeBusinesses = activeBusinesses; }
    public long getTotalActiveSites() { return totalActiveSites; }
    public void setTotalActiveSites(long totalActiveSites) { this.totalActiveSites = totalActiveSites; }
    public long getTotalActiveLabourers() { return totalActiveLabourers; }
    public void setTotalActiveLabourers(long totalActiveLabourers) { this.totalActiveLabourers = totalActiveLabourers; }
    public long getTotalSupervisors() { return totalSupervisors; }
    public void setTotalSupervisors(long totalSupervisors) { this.totalSupervisors = totalSupervisors; }
    public long getTodayPresent() { return todayPresent; }
    public void setTodayPresent(long todayPresent) { this.todayPresent = todayPresent; }
    public long getTodayHalfDay() { return todayHalfDay; }
    public void setTodayHalfDay(long todayHalfDay) { this.todayHalfDay = todayHalfDay; }
    public long getTodayAbsent() { return todayAbsent; }
    public void setTodayAbsent(long todayAbsent) { this.todayAbsent = todayAbsent; }
    public long getTodayPaidLeave() { return todayPaidLeave; }
    public void setTodayPaidLeave(long todayPaidLeave) { this.todayPaidLeave = todayPaidLeave; }
    public long getTodayNotSubmitted() { return todayNotSubmitted; }
    public void setTodayNotSubmitted(long todayNotSubmitted) { this.todayNotSubmitted = todayNotSubmitted; }
    public BigDecimal getPeriodTotalWages() { return periodTotalWages; }
    public void setPeriodTotalWages(BigDecimal periodTotalWages) { this.periodTotalWages = periodTotalWages; }
    public BigDecimal getPeriodPaymentsMade() { return periodPaymentsMade; }
    public void setPeriodPaymentsMade(BigDecimal periodPaymentsMade) { this.periodPaymentsMade = periodPaymentsMade; }
    public BigDecimal getPeriodOutstandingWages() { return periodOutstandingWages; }
    public void setPeriodOutstandingWages(BigDecimal periodOutstandingWages) { this.periodOutstandingWages = periodOutstandingWages; }
    public BigDecimal getTotalOutstandingAdvances() { return totalOutstandingAdvances; }
    public void setTotalOutstandingAdvances(BigDecimal totalOutstandingAdvances) { this.totalOutstandingAdvances = totalOutstandingAdvances; }
    public BigDecimal getPendingPaymentAmount() { return pendingPaymentAmount; }
    public void setPendingPaymentAmount(BigDecimal pendingPaymentAmount) { this.pendingPaymentAmount = pendingPaymentAmount; }
    public List<SiteSummaryDto> getSiteSummaries() { return siteSummaries; }
    public void setSiteSummaries(List<SiteSummaryDto> siteSummaries) { this.siteSummaries = siteSummaries; }
}
