package in.gov.labourerp.repository;

import in.gov.labourerp.entity.Payment;
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
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByLabourerId(Long labourerId);

    Optional<Payment> findByBusinessIdAndPaymentCode(Long businessId, String paymentCode);

    boolean existsByBusinessIdAndPaymentCode(Long businessId, String paymentCode);

    @Query("SELECT p FROM Payment p WHERE p.business.id = :businessId " +
           "AND (cast(:labourerId as Long) IS NULL OR p.labourer.id = :labourerId) " +
           "AND (cast(:siteId as Long) IS NULL OR p.site.id = :siteId) " +
           "AND (cast(:paymentMethod as String) IS NULL OR p.paymentMethod = :paymentMethod) " +
           "AND (cast(:startDate as LocalDate) IS NULL OR p.paymentDate >= :startDate) " +
           "AND (cast(:endDate as LocalDate) IS NULL OR p.paymentDate <= :endDate) " +
           "ORDER BY p.paymentDate DESC")
    Page<Payment> findByFilters(@Param("businessId") Long businessId,
                                @Param("labourerId") Long labourerId,
                                @Param("siteId") Long siteId,
                                @Param("paymentMethod") String paymentMethod,
                                @Param("startDate") LocalDate startDate,
                                @Param("endDate") LocalDate endDate,
                                Pageable pageable);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.business.id = :businessId " +
           "AND p.paymentDate >= :startDate AND p.paymentDate <= :endDate")
    BigDecimal calculateTotalPaymentsByBusiness(@Param("businessId") Long businessId,
                                                @Param("startDate") LocalDate startDate,
                                                @Param("endDate") LocalDate endDate);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.labourer.id = :labourerId " +
           "AND p.paymentDate >= :startDate AND p.paymentDate <= :endDate")
    BigDecimal calculateTotalPaymentsByLabourer(@Param("labourerId") Long labourerId,
                                                @Param("startDate") LocalDate startDate,
                                                @Param("endDate") LocalDate endDate);
}
