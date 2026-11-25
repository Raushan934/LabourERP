package in.gov.labourerp.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "advance_recoveries")
public class AdvanceRecovery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "advance_id", nullable = false)
    private Advance advance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payroll_item_id", nullable = false)
    private PayrollItem payrollItem;

    @Column(name = "recovered_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal recoveredAmount;

    @Column(name = "recovery_date", nullable = false)
    private LocalDate recoveryDate;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public AdvanceRecovery() {}

    public AdvanceRecovery(Advance advance, PayrollItem payrollItem, BigDecimal recoveredAmount, LocalDate recoveryDate) {
        this.advance = advance;
        this.payrollItem = payrollItem;
        this.recoveredAmount = recoveredAmount;
        this.recoveryDate = recoveryDate;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Advance getAdvance() { return advance; }
    public void setAdvance(Advance advance) { this.advance = advance; }

    public PayrollItem getPayrollItem() { return payrollItem; }
    public void setPayrollItem(PayrollItem payrollItem) { this.payrollItem = payrollItem; }

    public BigDecimal getRecoveredAmount() { return recoveredAmount; }
    public void setRecoveredAmount(BigDecimal recoveredAmount) { this.recoveredAmount = recoveredAmount; }

    public LocalDate getRecoveryDate() { return recoveryDate; }
    public void setRecoveryDate(LocalDate recoveryDate) { this.recoveryDate = recoveryDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
