package in.gov.labourerp.repository;

import in.gov.labourerp.entity.Advance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AdvanceRepository extends JpaRepository<Advance, Long> {

    List<Advance> findByLabourerId(Long labourerId);

    List<Advance> findByLabourerIdAndRecoveryStatusNot(Long labourerId, String recoveryStatus);

    Optional<Advance> findByBusinessIdAndAdvanceCode(Long businessId, String advanceCode);

    boolean existsByBusinessIdAndAdvanceCode(Long businessId, String advanceCode);

    @Query("SELECT a FROM Advance a WHERE a.business.id = :businessId " +
           "AND (cast(:labourerId as Long) IS NULL OR a.labourer.id = :labourerId) " +
           "AND (cast(:siteId as Long) IS NULL OR a.site.id = :siteId) " +
           "AND (cast(:status as String) IS NULL OR a.recoveryStatus = :status) " +
           "AND (cast(:startDate as LocalDate) IS NULL OR a.advanceDate >= :startDate) " +
           "AND (cast(:endDate as LocalDate) IS NULL OR a.advanceDate <= :endDate) " +
           "ORDER BY a.advanceDate DESC")
    Page<Advance> findByFilters(@Param("businessId") Long businessId,
                                @Param("labourerId") Long labourerId,
                                @Param("siteId") Long siteId,
                                @Param("status") String status,
                                @Param("startDate") LocalDate startDate,
                                @Param("endDate") LocalDate endDate,
                                Pageable pageable);

    @Query("SELECT COALESCE(SUM(a.amount - a.recoveredAmount), 0) FROM Advance a WHERE a.business.id = :businessId AND a.recoveryStatus != 'SETTLED'")
    BigDecimal calculateTotalOutstandingAdvanceByBusiness(@Param("businessId") Long businessId);

    @Query("SELECT COALESCE(SUM(a.amount - a.recoveredAmount), 0) FROM Advance a WHERE a.labourer.id = :labourerId AND a.recoveryStatus != 'SETTLED'")
    BigDecimal calculateOutstandingAdvanceByLabourer(@Param("labourerId") Long labourerId);
}
