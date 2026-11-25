package in.gov.labourerp.service;

import in.gov.labourerp.dto.auth.LoginRequest;
import in.gov.labourerp.dto.auth.LoginResponse;
import in.gov.labourerp.entity.BusinessUser;
import in.gov.labourerp.entity.User;
import in.gov.labourerp.repository.BusinessUserRepository;
import in.gov.labourerp.repository.UserRepository;
import in.gov.labourerp.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final BusinessUserRepository businessUserRepository;
    private final AuditLogService auditLogService;

    public AuthService(AuthenticationManager authenticationManager,
                       JwtTokenProvider tokenProvider,
                       UserRepository userRepository,
                       BusinessUserRepository businessUserRepository,
                       AuditLogService auditLogService) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
        this.businessUserRepository = businessUserRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public LoginResponse login(LoginRequest loginRequest, String ipAddress) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userRepository.findByUsername(loginRequest.getUsername())
                .or(() -> userRepository.findByEmail(loginRequest.getUsername()))
                .orElseThrow(() -> new RuntimeException("User not found"));

        String jwt = tokenProvider.generateToken(authentication, user.getId());

        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        List<BusinessUser> businessUsers = businessUserRepository.findByUserId(user.getId());
        List<LoginResponse.AccessibleBusinessDto> accessibleBusinesses = businessUsers.stream()
                .map(bu -> new LoginResponse.AccessibleBusinessDto(
                        bu.getBusiness().getId(),
                        bu.getBusiness().getBusinessCode(),
                        bu.getBusiness().getBusinessName(),
                        bu.getBusiness().getBusinessType(),
                        bu.getRole().getRoleCode()
                ))
                .collect(Collectors.toList());

        Long primaryBusinessId = businessUsers.stream()
                .filter(BusinessUser::isPrimary)
                .map(bu -> bu.getBusiness().getId())
                .findFirst()
                .orElse(accessibleBusinesses.isEmpty() ? null : accessibleBusinesses.get(0).getId());

        auditLogService.logAction(
                primaryBusinessId,
                user.getId(),
                user.getUsername(),
                "LOGIN",
                "USER",
                user.getId().toString(),
                null,
                "User successfully logged in via administrative authentication portal",
                ipAddress
        );

        LoginResponse response = new LoginResponse();
        response.setToken(jwt);
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setFullName(user.getFullName());
        response.setEmail(user.getEmail());
        response.setRoles(roles);
        response.setAccessibleBusinesses(accessibleBusinesses);
        response.setPrimaryBusinessId(primaryBusinessId);
        if (user.getAssignedSite() != null) {
            response.setAssignedSiteId(user.getAssignedSite().getId());
            response.setAssignedSiteName(user.getAssignedSite().getSiteName());
            response.setAssignedSiteCode(user.getAssignedSite().getSiteCode());
        }

        return response;
    }
}
