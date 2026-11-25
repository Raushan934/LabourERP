package in.gov.labourerp.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "wage_rate_history")
public class WageRateHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "labourer_id", nullable = false)
    private Labourer labourer;

    @Column(name = "daily_wage", nullable = false, precision = 10, scale = 2)
    private BigDecimal dailyWage;

    @Column(name = "overtime_hourly_rate", nullable = false, precision = 10, scale = 2)
    private BigDecimal overtimeHourlyRate;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(length = 255)
    private String reason;

    @Column(name = "changed_by", length = 50)
    private String changedBy;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public WageRateHistory() {}

    public WageRateHistory(Labourer labourer, BigDecimal dailyWage, BigDecimal overtimeHourlyRate, LocalDate effectiveFrom, String reason, String changedBy) {
        this.labourer = labourer;
        this.dailyWage = dailyWage;
        this.overtimeHourlyRate = overtimeHourlyRate;
        this.effectiveFrom = effectiveFrom;
        this.reason = reason;
        this.changedBy = changedBy;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Labourer getLabourer() { return labourer; }
    public void setLabourer(Labourer labourer) { this.labourer = labourer; }

    public BigDecimal getDailyWage() { return dailyWage; }
    public void setDailyWage(BigDecimal dailyWage) { this.dailyWage = dailyWage; }

    public BigDecimal getOvertimeHourlyRate() { return overtimeHourlyRate; }
    public void setOvertimeHourlyRate(BigDecimal overtimeHourlyRate) { this.overtimeHourlyRate = overtimeHourlyRate; }

    public LocalDate getEffectiveFrom() { return effectiveFrom; }
    public void setEffectiveFrom(LocalDate effectiveFrom) { this.effectiveFrom = effectiveFrom; }

    public LocalDate getEffectiveTo() { return effectiveTo; }
    public void setEffectiveTo(LocalDate effectiveTo) { this.effectiveTo = effectiveTo; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getChangedBy() { return changedBy; }
    public void setChangedBy(String changedBy) { this.changedBy = changedBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
