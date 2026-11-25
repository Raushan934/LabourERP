package in.gov.labourerp.repository;

import in.gov.labourerp.entity.LabourTransfer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LabourTransferRepository extends JpaRepository<LabourTransfer, Long> {
    List<LabourTransfer> findByBusinessIdOrderByTransferDateDesc(Long businessId);
    Page<LabourTransfer> findByBusinessIdOrderByTransferDateDesc(Long businessId, Pageable pageable);
    List<LabourTransfer> findByLabourerIdOrderByTransferDateDesc(Long labourerId);
}
