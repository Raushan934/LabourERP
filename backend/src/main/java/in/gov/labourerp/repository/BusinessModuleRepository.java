package in.gov.labourerp.repository;

import in.gov.labourerp.entity.BusinessModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BusinessModuleRepository extends JpaRepository<BusinessModule, Long> {
    List<BusinessModule> findByBusinessId(Long businessId);
    Optional<BusinessModule> findByBusinessIdAndModuleCode(Long businessId, String moduleCode);
}
