package in.gov.labourerp.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payroll_items", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"payroll_run_id", "labourer_id"})
})
public class PayrollItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payroll_run_id", nullable = false)
    private PayrollRun payrollRun;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "labourer_id", nullable = false)
    private Labourer labourer;

    @Column(name = "daily_wage_rate", nullable = false, precision = 10, scale = 2)
    private BigDecimal dailyWageRate = BigDecimal.ZERO;

    @Column(name = "present_days", nullable = false, precision = 5, scale = 2)
    private BigDecimal presentDays = BigDecimal.ZERO;

    @Column(name = "half_days", nullable = false, precision = 5, scale = 2)
    private BigDecimal halfDays = BigDecimal.ZERO;

    @Column(name = "absent_days", nullable = false, precision = 5, scale = 2)
    private BigDecimal absentDays = BigDecimal.ZERO;

    @Column(name = "paid_leave_days", nullable = false, precision = 5, scale = 2)
    private BigDecimal paidLeaveDays = BigDecimal.ZERO;

    @Column(name = "weekly_off_days", nullable = false, precision = 5, scale = 2)
    private BigDecimal weeklyOffDays = BigDecimal.ZERO;

    @Column(name = "holiday_days", nullable = false, precision = 5, scale = 2)
    private BigDecimal holidayDays = BigDecimal.ZERO;

    @Column(name = "overtime_hours", nullable = false, precision = 6, scale = 2)
    private BigDecimal overtimeHours = BigDecimal.ZERO;

    @Column(name = "overtime_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal overtimeAmount = BigDecimal.ZERO;

    @Column(name = "base_earnings", nullable = false, precision = 10, scale = 2)
    private BigDecimal baseEarnings = BigDecimal.ZERO;

    @Column(name = "gross_earnings", nullable = false, precision = 10, scale = 2)
    private BigDecimal grossEarnings = BigDecimal.ZERO;

    @Column(name = "advance_recovery", nullable = false, precision = 10, scale = 2)
    private BigDecimal advanceRecovery = BigDecimal.ZERO;

    @Column(name = "other_deductions", nullable = false, precision = 10, scale = 2)
    private BigDecimal otherDeductions = BigDecimal.ZERO;

    @Column(name = "net_payable", nullable = false, precision = 10, scale = 2)
    private BigDecimal netPayable = BigDecimal.ZERO;

    @Column(name = "amount_paid", nullable = false, precision = 10, scale = 2)
    private BigDecimal amountPaid = BigDecimal.ZERO;

    @Column(name = "remaining_balance", nullable = false, precision = 10, scale = 2)
    private BigDecimal remainingBalance = BigDecimal.ZERO;

    @Column(nullable = false, length = 30)
    private String status = "UNPAID"; // UNPAID, PARTIALLY_PAID, PAID

    @Column(length = 255)
    private String remarks;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public PayrollItem() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public PayrollRun getPayrollRun() { return payrollRun; }
    public void setPayrollRun(PayrollRun payrollRun) { this.payrollRun = payrollRun; }

    public Labourer getLabourer() { return labourer; }
    public void setLabourer(Labourer labourer) { this.labourer = labourer; }

    public BigDecimal getDailyWageRate() { return dailyWageRate; }
    public void setDailyWageRate(BigDecimal dailyWageRate) { this.dailyWageRate = dailyWageRate; }

    public BigDecimal getPresentDays() { return presentDays; }
    public void setPresentDays(BigDecimal presentDays) { this.presentDays = presentDays; }

    public BigDecimal getHalfDays() { return halfDays; }
    public void setHalfDays(BigDecimal halfDays) { this.halfDays = halfDays; }

    public BigDecimal getAbsentDays() { return absentDays; }
    public void setAbsentDays(BigDecimal absentDays) { this.absentDays = absentDays; }

    public BigDecimal getPaidLeaveDays() { return paidLeaveDays; }
    public void setPaidLeaveDays(BigDecimal paidLeaveDays) { this.paidLeaveDays = paidLeaveDays; }

    public BigDecimal getWeeklyOffDays() { return weeklyOffDays; }
    public void setWeeklyOffDays(BigDecimal weeklyOffDays) { this.weeklyOffDays = weeklyOffDays; }

    public BigDecimal getHolidayDays() { return holidayDays; }
    public void setHolidayDays(BigDecimal holidayDays) { this.holidayDays = holidayDays; }

    public BigDecimal getOvertimeHours() { return overtimeHours; }
    public void setOvertimeHours(BigDecimal overtimeHours) { this.overtimeHours = overtimeHours; }

    public BigDecimal getOvertimeAmount() { return overtimeAmount; }
    public void setOvertimeAmount(BigDecimal overtimeAmount) { this.overtimeAmount = overtimeAmount; }

    public BigDecimal getBaseEarnings() { return baseEarnings; }
    public void setBaseEarnings(BigDecimal baseEarnings) { this.baseEarnings = baseEarnings; }

    public BigDecimal getGrossEarnings() { return grossEarnings; }
    public void setGrossEarnings(BigDecimal grossEarnings) { this.grossEarnings = grossEarnings; }

    public BigDecimal getAdvanceRecovery() { return advanceRecovery; }
    public void setAdvanceRecovery(BigDecimal advanceRecovery) { this.advanceRecovery = advanceRecovery; }

    public BigDecimal getOtherDeductions() { return otherDeductions; }
    public void setOtherDeductions(BigDecimal otherDeductions) { this.otherDeductions = otherDeductions; }

    public BigDecimal getNetPayable() { return netPayable; }
    public void setNetPayable(BigDecimal netPayable) { this.netPayable = netPayable; }

    public BigDecimal getAmountPaid() { return amountPaid; }
    public void setAmountPaid(BigDecimal amountPaid) { this.amountPaid = amountPaid; }

    public BigDecimal getRemainingBalance() { return remainingBalance; }
    public void setRemainingBalance(BigDecimal remainingBalance) { this.remainingBalance = remainingBalance; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
