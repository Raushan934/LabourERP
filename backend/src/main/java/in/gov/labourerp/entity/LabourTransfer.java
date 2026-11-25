package in.gov.labourerp.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "labour_transfers")
public class LabourTransfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "labourer_id", nullable = false)
    private Labourer labourer;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "from_site_id")
    private Site fromSite;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "to_site_id", nullable = false)
    private Site toSite;

    @Column(name = "transfer_date", nullable = false)
    private LocalDate transferDate;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(length = 255)
    private String remarks;

    @Column(name = "transferred_by", length = 50)
    private String transferredBy;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public LabourTransfer() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Business getBusiness() { return business; }
    public void setBusiness(Business business) { this.business = business; }

    public Labourer getLabourer() { return labourer; }
    public void setLabourer(Labourer labourer) { this.labourer = labourer; }

    public Site getFromSite() { return fromSite; }
    public void setFromSite(Site fromSite) { this.fromSite = fromSite; }

    public Site getToSite() { return toSite; }
    public void setToSite(Site toSite) { this.toSite = toSite; }

    public LocalDate getTransferDate() { return transferDate; }
    public void setTransferDate(LocalDate transferDate) { this.transferDate = transferDate; }

    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public String getTransferredBy() { return transferredBy; }
    public void setTransferredBy(String transferredBy) { this.transferredBy = transferredBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
