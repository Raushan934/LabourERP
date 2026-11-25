package in.gov.labourerp.repository;

import in.gov.labourerp.entity.MasterCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface MasterCategoryRepository extends JpaRepository<MasterCategory, Long> {
    Optional<MasterCategory> findByCategoryCode(String categoryCode);
}
