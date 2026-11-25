package in.gov.labourerp.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "payroll_runs", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"business_id", "run_code"})
})
public class PayrollRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id")
    private Site site;

    @Column(name = "run_code", nullable = false, length = 30)
    private String runCode;

    @Column(name = "period_month", nullable = false)
    private int periodMonth;

    @Column(name = "period_year", nullable = false)
    private int periodYear;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(nullable = false, length = 30)
    private String status = "DRAFT"; // DRAFT, GENERATED, APPROVED, DISBURSED

    @Column(name = "total_workers", nullable = false)
    private int totalWorkers = 0;

    @Column(name = "total_gross_earnings", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalGrossEarnings = BigDecimal.ZERO;

    @Column(name = "total_advance_deductions", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAdvanceDeductions = BigDecimal.ZERO;

    @Column(name = "total_other_deductions", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalOtherDeductions = BigDecimal.ZERO;

    @Column(name = "total_net_payable", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalNetPayable = BigDecimal.ZERO;

    @Column(name = "total_paid_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalPaidAmount = BigDecimal.ZERO;

    @Column(name = "total_outstanding_balance", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalOutstandingBalance = BigDecimal.ZERO;

    @Column(name = "generated_by", length = 50)
    private String generatedBy;

    @Column(name = "approved_by", length = 50)
    private String approvedBy;

    @OneToMany(mappedBy = "payrollRun", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PayrollItem> items = new ArrayList<>();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public PayrollRun() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Business getBusiness() { return business; }
    public void setBusiness(Business business) { this.business = business; }

    public Site getSite() { return site; }
    public void setSite(Site site) { this.site = site; }

    public String getRunCode() { return runCode; }
    public void setRunCode(String runCode) { this.runCode = runCode; }

    public int getPeriodMonth() { return periodMonth; }
    public void setPeriodMonth(int periodMonth) { this.periodMonth = periodMonth; }

    public int getPeriodYear() { return periodYear; }
    public void setPeriodYear(int periodYear) { this.periodYear = periodYear; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getTotalWorkers() { return totalWorkers; }
    public void setTotalWorkers(int totalWorkers) { this.totalWorkers = totalWorkers; }

    public BigDecimal getTotalGrossEarnings() { return totalGrossEarnings; }
    public void setTotalGrossEarnings(BigDecimal totalGrossEarnings) { this.totalGrossEarnings = totalGrossEarnings; }

    public BigDecimal getTotalAdvanceDeductions() { return totalAdvanceDeductions; }
    public void setTotalAdvanceDeductions(BigDecimal totalAdvanceDeductions) { this.totalAdvanceDeductions = totalAdvanceDeductions; }

    public BigDecimal getTotalOtherDeductions() { return totalOtherDeductions; }
    public void setTotalOtherDeductions(BigDecimal totalOtherDeductions) { this.totalOtherDeductions = totalOtherDeductions; }

    public BigDecimal getTotalNetPayable() { return totalNetPayable; }
    public void setTotalNetPayable(BigDecimal totalNetPayable) { this.totalNetPayable = totalNetPayable; }

    public BigDecimal getTotalPaidAmount() { return totalPaidAmount; }
    public void setTotalPaidAmount(BigDecimal totalPaidAmount) { this.totalPaidAmount = totalPaidAmount; }

    public BigDecimal getTotalOutstandingBalance() { return totalOutstandingBalance; }
    public void setTotalOutstandingBalance(BigDecimal totalOutstandingBalance) { this.totalOutstandingBalance = totalOutstandingBalance; }

    public String getGeneratedBy() { return generatedBy; }
    public void setGeneratedBy(String generatedBy) { this.generatedBy = generatedBy; }

    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }

    public List<PayrollItem> getItems() { return items; }
    public void setItems(List<PayrollItem> items) { this.items = items; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
