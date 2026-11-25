package in.gov.labourerp.repository;

import in.gov.labourerp.entity.CustomFieldValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomFieldValueRepository extends JpaRepository<CustomFieldValue, Long> {
    List<CustomFieldValue> findByEntityId(Long entityId);
    Optional<CustomFieldValue> findByDefinitionIdAndEntityId(Long definitionId, Long entityId);
    void deleteByEntityId(Long entityId);
}
