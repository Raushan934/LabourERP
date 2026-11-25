package in.gov.labourerp.repository;

import in.gov.labourerp.entity.PayrollItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PayrollItemRepository extends JpaRepository<PayrollItem, Long> {
    List<PayrollItem> findByPayrollRunId(Long payrollRunId);
    Optional<PayrollItem> findByPayrollRunIdAndLabourerId(Long payrollRunId, Long labourerId);
    List<PayrollItem> findByLabourerId(Long labourerId);
}
