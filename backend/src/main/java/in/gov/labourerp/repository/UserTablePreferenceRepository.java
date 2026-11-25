package in.gov.labourerp.repository;

import in.gov.labourerp.entity.UserTablePreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserTablePreferenceRepository extends JpaRepository<UserTablePreference, Long> {
    Optional<UserTablePreference> findByUserIdAndTableKey(Long userId, String tableKey);
}
