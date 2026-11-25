package in.gov.labourerp.controller;

import in.gov.labourerp.dto.common.ApiResponse;
import in.gov.labourerp.entity.User;
import in.gov.labourerp.repository.UserRepository;
import in.gov.labourerp.service.UserPreferenceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/preferences")
public class UserPreferenceController {

    private final UserPreferenceService preferenceService;
    private final UserRepository userRepository;

    public UserPreferenceController(UserPreferenceService preferenceService, UserRepository userRepository) {
        this.preferenceService = preferenceService;
        this.userRepository = userRepository;
    }

    @GetMapping("/{tableKey}")
    public ResponseEntity<ApiResponse<String>> getPreference(@PathVariable String tableKey, Authentication auth) {
        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        String json = preferenceService.getTablePreference(user.getId(), tableKey);
        return ResponseEntity.ok(ApiResponse.success(json));
    }

    @PostMapping("/{tableKey}")
    public ResponseEntity<ApiResponse<Void>> savePreference(@PathVariable String tableKey,
                                                           @RequestBody Map<String, String> body,
                                                           Authentication auth) {
        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        String json = body.getOrDefault("preferencesJson", "{}");
        preferenceService.saveTablePreference(user.getId(), tableKey, json);
        return ResponseEntity.ok(ApiResponse.success("Preference saved", null));
    }
}
