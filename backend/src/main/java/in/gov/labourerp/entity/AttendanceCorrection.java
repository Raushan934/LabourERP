package in.gov.labourerp.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance_corrections")
public class AttendanceCorrection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendance_record_id", nullable = false)
    private AttendanceRecord attendanceRecord;

    @Column(name = "previous_status", nullable = false, length = 30)
    private String previousStatus;

    @Column(name = "new_status", nullable = false, length = 30)
    private String newStatus;

    @Column(name = "previous_overtime_hours", nullable = false, precision = 4, scale = 2)
    private BigDecimal previousOvertimeHours;

    @Column(name = "new_overtime_hours", nullable = false, precision = 4, scale = 2)
    private BigDecimal newOvertimeHours;

    @Column(name = "previous_calculated_wage", nullable = false, precision = 10, scale = 2)
    private BigDecimal previousCalculatedWage;

    @Column(name = "new_calculated_wage", nullable = false, precision = 10, scale = 2)
    private BigDecimal newCalculatedWage;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(name = "modified_by", nullable = false, length = 50)
    private String modifiedBy;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public AttendanceCorrection() {}

    public AttendanceCorrection(AttendanceRecord attendanceRecord, String previousStatus, String newStatus,
                                BigDecimal previousOvertimeHours, BigDecimal newOvertimeHours,
                                BigDecimal previousCalculatedWage, BigDecimal newCalculatedWage,
                                String reason, String modifiedBy) {
        this.attendanceRecord = attendanceRecord;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.previousOvertimeHours = previousOvertimeHours;
        this.newOvertimeHours = newOvertimeHours;
        this.previousCalculatedWage = previousCalculatedWage;
        this.newCalculatedWage = newCalculatedWage;
        this.reason = reason;
        this.modifiedBy = modifiedBy;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public AttendanceRecord getAttendanceRecord() { return attendanceRecord; }
    public void setAttendanceRecord(AttendanceRecord attendanceRecord) { this.attendanceRecord = attendanceRecord; }

    public String getPreviousStatus() { return previousStatus; }
    public void setPreviousStatus(String previousStatus) { this.previousStatus = previousStatus; }

    public String getNewStatus() { return newStatus; }
    public void setNewStatus(String newStatus) { this.newStatus = newStatus; }

    public BigDecimal getPreviousOvertimeHours() { return previousOvertimeHours; }
    public void setPreviousOvertimeHours(BigDecimal previousOvertimeHours) { this.previousOvertimeHours = previousOvertimeHours; }

    public BigDecimal getNewOvertimeHours() { return newOvertimeHours; }
    public void setNewOvertimeHours(BigDecimal newOvertimeHours) { this.newOvertimeHours = newOvertimeHours; }

    public BigDecimal getPreviousCalculatedWage() { return previousCalculatedWage; }
    public void setPreviousCalculatedWage(BigDecimal previousCalculatedWage) { this.previousCalculatedWage = previousCalculatedWage; }

    public BigDecimal getNewCalculatedWage() { return newCalculatedWage; }
    public void setNewCalculatedWage(BigDecimal newCalculatedWage) { this.newCalculatedWage = newCalculatedWage; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getModifiedBy() { return modifiedBy; }
    public void setModifiedBy(String modifiedBy) { this.modifiedBy = modifiedBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
