package in.gov.labourerp.repository;

import in.gov.labourerp.entity.MasterValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MasterValueRepository extends JpaRepository<MasterValue, Long> {

    @Query("SELECT mv FROM MasterValue mv WHERE mv.category.categoryCode = :categoryCode AND (mv.business IS NULL OR mv.business.id = :businessId) AND mv.isActive = true ORDER BY mv.sortOrder ASC, mv.name ASC")
    List<MasterValue> findActiveByCategoryCodeAndBusinessId(@Param("categoryCode") String categoryCode, @Param("businessId") Long businessId);

    @Query("SELECT mv FROM MasterValue mv WHERE mv.category.categoryCode = :categoryCode AND (mv.business IS NULL OR mv.business.id = :businessId) ORDER BY mv.sortOrder ASC, mv.name ASC")
    List<MasterValue> findAllByCategoryCodeAndBusinessId(@Param("categoryCode") String categoryCode, @Param("businessId") Long businessId);

    List<MasterValue> findByCategoryId(Long categoryId);

    Optional<MasterValue> findByCategoryIdAndCode(Long categoryId, String code);
}
