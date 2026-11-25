package in.gov.labourerp.repository;

import in.gov.labourerp.entity.BusinessUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BusinessUserRepository extends JpaRepository<BusinessUser, Long> {
    List<BusinessUser> findByUserId(Long userId);
    List<BusinessUser> findByBusinessId(Long businessId);
    Optional<BusinessUser> findByBusinessIdAndUserId(Long businessId, Long userId);
}
