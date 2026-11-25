package in.gov.labourerp.repository;

import in.gov.labourerp.entity.Site;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SiteRepository extends JpaRepository<Site, Long> {

    List<Site> findByBusinessId(Long businessId);

    List<Site> findByBusinessIdAndStatus(Long businessId, String status);

    Optional<Site> findByBusinessIdAndSiteCode(Long businessId, String siteCode);

    Optional<Site> findFirstBySiteCode(String siteCode);

    boolean existsByBusinessIdAndSiteCode(Long businessId, String siteCode);

    @Query("SELECT s FROM Site s WHERE s.business.id = :businessId " +
           "AND (:status IS NULL OR s.status = :status) " +
           "AND (:search IS NULL OR LOWER(s.siteName) LIKE :search OR LOWER(s.siteCode) LIKE :search OR LOWER(s.clientName) LIKE :search)")
    Page<Site> findByFilters(@Param("businessId") Long businessId,
                             @Param("status") String status,
                             @Param("search") String search,
                             Pageable pageable);

    long countByBusinessIdAndStatus(Long businessId, String status);
}
