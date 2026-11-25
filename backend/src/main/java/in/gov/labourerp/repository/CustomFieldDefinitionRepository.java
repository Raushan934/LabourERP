package in.gov.labourerp.repository;

import in.gov.labourerp.entity.CustomFieldDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomFieldDefinitionRepository extends JpaRepository<CustomFieldDefinition, Long> {
    List<CustomFieldDefinition> findByBusinessIdAndEntityTypeAndIsActiveTrueOrderByDisplayOrderAsc(Long businessId, String entityType);
    List<CustomFieldDefinition> findByBusinessIdAndEntityTypeOrderByDisplayOrderAsc(Long businessId, String entityType);
    List<CustomFieldDefinition> findByBusinessId(Long businessId);
    Optional<CustomFieldDefinition> findByBusinessIdAndEntityTypeAndFieldKey(Long businessId, String entityType, String fieldKey);
}
