package in.gov.labourerp.controller;

import in.gov.labourerp.dto.auth.LoginRequest;
import in.gov.labourerp.dto.auth.LoginResponse;
import in.gov.labourerp.dto.auth.UserDto;
import in.gov.labourerp.dto.common.ApiResponse;
import in.gov.labourerp.entity.User;
import in.gov.labourerp.repository.UserRepository;
import in.gov.labourerp.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    public AuthController(AuthService authService, UserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        LoginResponse response = authService.login(loginRequest, ip);
        return ResponseEntity.ok(ApiResponse.success("Authentication successful", response));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDto>> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthorized"));
        }

        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setPhone(user.getPhone());
        dto.setStatus(user.getStatus());
        dto.setRoles(auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()));
        if (user.getAssignedSite() != null) {
            dto.setAssignedSiteId(user.getAssignedSite().getId());
            dto.setAssignedSiteName(user.getAssignedSite().getSiteName());
            dto.setAssignedSiteCode(user.getAssignedSite().getSiteCode());
        }

        return ResponseEntity.ok(ApiResponse.success(dto));
    }
}
