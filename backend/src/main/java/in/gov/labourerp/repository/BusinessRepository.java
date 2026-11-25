package in.gov.labourerp.repository;

import in.gov.labourerp.entity.Business;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BusinessRepository extends JpaRepository<Business, Long> {
    Optional<Business> findByBusinessCode(String businessCode);
    List<Business> findByStatus(String status);
    boolean existsByBusinessCode(String businessCode);
}
