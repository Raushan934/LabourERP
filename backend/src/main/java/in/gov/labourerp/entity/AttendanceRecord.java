package in.gov.labourerp.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance_records", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"site_id", "labourer_id", "attendance_date"})
})
public class AttendanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "labourer_id", nullable = false)
    private Labourer labourer;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    @Column(nullable = false, length = 30)
    private String status = "PRESENT"; // PRESENT, HALF_DAY, ABSENT, PAID_LEAVE, WEEKLY_OFF, HOLIDAY

    @Column(name = "overtime_hours", nullable = false, precision = 4, scale = 2)
    private BigDecimal overtimeHours = BigDecimal.ZERO;

    @Column(name = "wage_multiplier", nullable = false, precision = 5, scale = 2)
    private BigDecimal wageMultiplier = BigDecimal.ONE;

    @Column(name = "daily_wage_applied", nullable = false, precision = 10, scale = 2)
    private BigDecimal dailyWageApplied = BigDecimal.ZERO;

    @Column(name = "calculated_wage", nullable = false, precision = 10, scale = 2)
    private BigDecimal calculatedWage = BigDecimal.ZERO;

    @Column(name = "overtime_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal overtimeAmount = BigDecimal.ZERO;

    @Column(length = 255)
    private String remarks;

    @Column(name = "marked_by", length = 50)
    private String markedBy;

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

    public AttendanceRecord() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Business getBusiness() { return business; }
    public void setBusiness(Business business) { this.business = business; }

    public Site getSite() { return site; }
    public void setSite(Site site) { this.site = site; }

    public Labourer getLabourer() { return labourer; }
    public void setLabourer(Labourer labourer) { this.labourer = labourer; }

    public LocalDate getAttendanceDate() { return attendanceDate; }
    public void setAttendanceDate(LocalDate attendanceDate) { this.attendanceDate = attendanceDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public BigDecimal getOvertimeHours() { return overtimeHours; }
    public void setOvertimeHours(BigDecimal overtimeHours) { this.overtimeHours = overtimeHours; }

    public BigDecimal getWageMultiplier() { return wageMultiplier; }
    public void setWageMultiplier(BigDecimal wageMultiplier) { this.wageMultiplier = wageMultiplier; }

    public BigDecimal getDailyWageApplied() { return dailyWageApplied; }
    public void setDailyWageApplied(BigDecimal dailyWageApplied) { this.dailyWageApplied = dailyWageApplied; }

    public BigDecimal getCalculatedWage() { return calculatedWage; }
    public void setCalculatedWage(BigDecimal calculatedWage) { this.calculatedWage = calculatedWage; }

    public BigDecimal getOvertimeAmount() { return overtimeAmount; }
    public void setOvertimeAmount(BigDecimal overtimeAmount) { this.overtimeAmount = overtimeAmount; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public String getMarkedBy() { return markedBy; }
    public void setMarkedBy(String markedBy) { this.markedBy = markedBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
