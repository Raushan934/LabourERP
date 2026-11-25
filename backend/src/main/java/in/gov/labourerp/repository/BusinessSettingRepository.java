package in.gov.labourerp.repository;

import in.gov.labourerp.entity.BusinessSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BusinessSettingRepository extends JpaRepository<BusinessSetting, Long> {
    List<BusinessSetting> findByBusinessId(Long businessId);
    Optional<BusinessSetting> findByBusinessIdAndSettingKey(Long businessId, String settingKey);
}
