package in.gov.labourerp.repository;

import in.gov.labourerp.entity.Labourer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface LabourerRepository extends JpaRepository<Labourer, Long> {

    List<Labourer> findByBusinessId(Long businessId);

    List<Labourer> findByBusinessIdAndCurrentSiteId(Long businessId, Long currentSiteId);

    Optional<Labourer> findByBusinessIdAndLabourCode(Long businessId, String labourCode);

    boolean existsByBusinessIdAndLabourCode(Long businessId, String labourCode);

    @Query("SELECT l FROM Labourer l WHERE l.business.id = :businessId " +
           "AND (:siteId IS NULL OR l.currentSite.id = :siteId) " +
           "AND (:category IS NULL OR l.labourCategory = :category) " +
           "AND (:status IS NULL OR l.status = :status) " +
           "AND (:search IS NULL OR LOWER(l.fullName) LIKE :search " +
           "     OR LOWER(l.labourCode) LIKE :search " +
           "     OR l.mobileNumber LIKE :search)")
    Page<Labourer> findByFilters(@Param("businessId") Long businessId,
                                 @Param("siteId") Long siteId,
                                 @Param("category") String category,
                                 @Param("status") String status,
                                 @Param("search") String search,
                                 Pageable pageable);

    long countByBusinessIdAndStatus(Long businessId, String status);

    @Query("SELECT COUNT(l) FROM Labourer l WHERE l.business.id = :businessId AND l.currentSite.id = :siteId AND l.status = 'ACTIVE'")
    long countActiveBySite(@Param("businessId") Long businessId, @Param("siteId") Long siteId);
}
