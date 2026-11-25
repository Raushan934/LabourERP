package in.gov.labourerp.repository;

import in.gov.labourerp.entity.WageRateHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface WageRateHistoryRepository extends JpaRepository<WageRateHistory, Long> {
    List<WageRateHistory> findByLabourerIdOrderByEffectiveFromDesc(Long labourerId);
}
