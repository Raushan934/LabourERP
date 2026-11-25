package in.gov.labourerp.repository;

import in.gov.labourerp.entity.PayrollRun;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PayrollRunRepository extends JpaRepository<PayrollRun, Long> {

    List<PayrollRun> findByBusinessId(Long businessId);

    Optional<PayrollRun> findByBusinessIdAndPeriodMonthAndPeriodYearAndSiteId(Long businessId, int periodMonth, int periodYear, Long siteId);

    Optional<PayrollRun> findByBusinessIdAndRunCode(Long businessId, String runCode);

    @Query("SELECT pr FROM PayrollRun pr WHERE pr.business.id = :businessId " +
           "AND (cast(:year as Integer) IS NULL OR pr.periodYear = :year) " +
           "AND (cast(:month as Integer) IS NULL OR pr.periodMonth = :month) " +
           "AND (cast(:siteId as Long) IS NULL OR pr.site.id = :siteId) " +
           "ORDER BY pr.periodYear DESC, pr.periodMonth DESC")
    Page<PayrollRun> findByFilters(@Param("businessId") Long businessId,
                                  @Param("year") Integer year,
                                  @Param("month") Integer month,
                                  @Param("siteId") Long siteId,
                                  Pageable pageable);
}
