package in.gov.labourerp.service;

import in.gov.labourerp.entity.User;
import in.gov.labourerp.entity.UserTablePreference;
import in.gov.labourerp.repository.UserRepository;
import in.gov.labourerp.repository.UserTablePreferenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserPreferenceService {

    private final UserTablePreferenceRepository repository;
    private final UserRepository userRepository;

    public UserPreferenceService(UserTablePreferenceRepository repository, UserRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public String getTablePreference(Long userId, String tableKey) {
        return repository.findByUserIdAndTableKey(userId, tableKey)
                .map(UserTablePreference::getPreferencesJson)
                .orElse(null);
    }

    @Transactional
    public void saveTablePreference(Long userId, String tableKey, String preferencesJson) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        UserTablePreference pref = repository.findByUserIdAndTableKey(userId, tableKey)
                .orElse(new UserTablePreference(user, tableKey, preferencesJson));

        pref.setPreferencesJson(preferencesJson);
        repository.save(pref);
    }
}
