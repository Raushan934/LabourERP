package in.gov.labourerp.repository;

import in.gov.labourerp.entity.AdvanceRecovery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AdvanceRecoveryRepository extends JpaRepository<AdvanceRecovery, Long> {
    List<AdvanceRecovery> findByAdvanceId(Long advanceId);
    List<AdvanceRecovery> findByPayrollItemId(Long payrollItemId);
}
