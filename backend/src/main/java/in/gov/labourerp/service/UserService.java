package in.gov.labourerp.service;

import in.gov.labourerp.dto.auth.UserDto;
import in.gov.labourerp.dto.user.CreateUserRequest;
import in.gov.labourerp.dto.user.UpdateUserRequest;
import in.gov.labourerp.entity.Business;
import in.gov.labourerp.entity.BusinessUser;
import in.gov.labourerp.entity.Role;
import in.gov.labourerp.entity.Site;
import in.gov.labourerp.entity.User;
import in.gov.labourerp.repository.BusinessRepository;
import in.gov.labourerp.repository.BusinessUserRepository;
import in.gov.labourerp.repository.RoleRepository;
import in.gov.labourerp.repository.SiteRepository;
import in.gov.labourerp.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final SiteRepository siteRepository;
    private final BusinessRepository businessRepository;
    private final BusinessUserRepository businessUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final SupabaseAuthService supabaseAuthService;

    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       SiteRepository siteRepository,
                       BusinessRepository businessRepository,
                       BusinessUserRepository businessUserRepository,
                       PasswordEncoder passwordEncoder,
                       SupabaseAuthService supabaseAuthService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.siteRepository = siteRepository;
        this.businessRepository = businessRepository;
        this.businessUserRepository = businessUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.supabaseAuthService = supabaseAuthService;
    }

    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        return convertToDto(user);
    }

    @Transactional
    public UserDto createUser(CreateUserRequest request, String createdBy) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username '" + request.getUsername() + "' is already taken");
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email '" + request.getEmail() + "' is already registered");
        }

        Role role = roleRepository.findByRoleCode(request.getRole())
                .orElseThrow(() -> new RuntimeException("Role '" + request.getRole() + "' not found"));

        User user = new User();
        user.setUsername(request.getUsername().trim());
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName().trim());
        user.setPhone(request.getPhone());
        user.setStatus("ACTIVE");
        user.setCreatedBy(createdBy);
        user.getRoles().add(role);

        user.setUpiId(request.getUpiId());
        user.setBankAccountNumber(request.getBankAccountNumber());
        user.setBankIfscCode(request.getBankIfscCode());
        user.setBankName(request.getBankName());
        user.setBankBeneficiaryName(request.getBankBeneficiaryName());
        user.setQrCodeUrl(request.getQrCodeUrl());

        ensureSpecificUpiAndQr(user);

        if (request.getAssignedSiteId() != null) {
            Site site = siteRepository.findById(request.getAssignedSiteId())
                    .orElseThrow(() -> new RuntimeException("Assigned site not found with id: " + request.getAssignedSiteId()));
            user.setAssignedSite(site);
        }

        User savedUser = userRepository.save(user);

        // Assign to Business
        Business business = null;
        if (request.getBusinessId() != null) {
            business = businessRepository.findById(request.getBusinessId()).orElse(null);
        }
        if (business == null) {
            business = businessRepository.findAll().stream().findFirst().orElse(null);
        }
        if (business != null) {
            businessUserRepository.save(new BusinessUser(business, savedUser, role, true));
        }

        // Two-way sync to Supabase Authentication (auth.users)
        try {
            supabaseAuthService.syncUserToSupabaseAuth(
                    savedUser.getEmail(),
                    request.getPassword(),
                    role.getRoleCode(),
                    savedUser.getFullName()
            );
        } catch (Exception e) {
            log.warn("Supabase auth sync notice on user create: {}", e.getMessage());
        }

        return convertToDto(savedUser);
    }

    @Transactional
    public UserDto updateUser(Long id, UpdateUserRequest request, String updatedBy) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName().trim());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone().trim());
        }
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            user.setStatus(request.getStatus().trim().toUpperCase());
        }

        if (request.getRole() != null && !request.getRole().isBlank()) {
            Role role = roleRepository.findByRoleCode(request.getRole())
                    .orElseThrow(() -> new RuntimeException("Role '" + request.getRole() + "' not found"));
            user.setRoles(new HashSet<>(List.of(role)));
        }

        if (request.getAssignedSiteId() != null) {
            if (request.getAssignedSiteId() == -1L) {
                user.setAssignedSite(null); // Clear deputation
            } else {
                Site site = siteRepository.findById(request.getAssignedSiteId())
                        .orElseThrow(() -> new RuntimeException("Site not found with id: " + request.getAssignedSiteId()));
                user.setAssignedSite(site);
            }
        }

        if (request.getUpiId() != null) user.setUpiId(request.getUpiId().trim());
        if (request.getBankAccountNumber() != null) user.setBankAccountNumber(request.getBankAccountNumber().trim());
        if (request.getBankIfscCode() != null) user.setBankIfscCode(request.getBankIfscCode().trim().toUpperCase());
        if (request.getBankName() != null) user.setBankName(request.getBankName().trim());
        if (request.getBankBeneficiaryName() != null) user.setBankBeneficiaryName(request.getBankBeneficiaryName().trim());
        if (request.getQrCodeUrl() != null) user.setQrCodeUrl(request.getQrCodeUrl().trim());

        ensureSpecificUpiAndQr(user);

        user.setUpdatedBy(updatedBy);
        User savedUser = userRepository.save(user);
        return convertToDto(savedUser);
    }

    @Transactional
    public void resetPassword(Long id, String newPassword, String adminUsername) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setUpdatedBy(adminUsername);
        userRepository.save(user);

        // Synchronize new password to Supabase Auth auth.users
        try {
            supabaseAuthService.updatePasswordInSupabaseAuth(user.getEmail(), newPassword);
        } catch (Exception e) {
            log.warn("Supabase auth sync notice on password reset: {}", e.getMessage());
        }
    }

    @Transactional
    public void changePassword(String username, String currentPassword, String newPassword) {
        User user = userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new RuntimeException("Current password does not match");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setUpdatedBy(username);
        userRepository.save(user);

        // Synchronize new password to Supabase Auth auth.users
        try {
            supabaseAuthService.updatePasswordInSupabaseAuth(user.getEmail(), newPassword);
        } catch (Exception e) {
            log.warn("Supabase auth sync notice on change password: {}", e.getMessage());
        }
    }

    @Transactional
    public UserDto updateProfile(String username, String fullName, String phone) {
        User user = userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        if (fullName != null && !fullName.isBlank()) {
            user.setFullName(fullName.trim());
        }
        if (phone != null) {
            user.setPhone(phone.trim());
        }
        user.setUpdatedBy(username);
        return convertToDto(userRepository.save(user));
    }

    private void ensureSpecificUpiAndQr(User user) {
        if (user.getUpiId() == null || user.getUpiId().isBlank()) {
            String phoneDigits = user.getPhone() != null ? user.getPhone().replaceAll("[^0-9]", "") : "";
            String upi = (phoneDigits.length() >= 10 ? phoneDigits.substring(phoneDigits.length() - 10) : user.getUsername().toLowerCase()) + "@upi";
            user.setUpiId(upi);
        }
        if (user.getBankBeneficiaryName() == null || user.getBankBeneficiaryName().isBlank()) {
            user.setBankBeneficiaryName(user.getFullName());
        }
        if (user.getQrCodeUrl() == null || user.getQrCodeUrl().isBlank()) {
            try {
                String upiUri = "upi://pay?pa=" + user.getUpiId() + "&pn=" + java.net.URLEncoder.encode(user.getBankBeneficiaryName(), java.nio.charset.StandardCharsets.UTF_8) + "&cu=INR";
                String qr = "https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=" + java.net.URLEncoder.encode(upiUri, java.nio.charset.StandardCharsets.UTF_8);
                user.setQrCodeUrl(qr);
            } catch (Exception ignored) {}
        }
    }

    public UserDto convertToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setPhone(user.getPhone());
        dto.setStatus(user.getStatus());
        dto.setRoles(user.getRoles().stream().map(Role::getRoleCode).collect(Collectors.toList()));
        if (user.getAssignedSite() != null) {
            dto.setAssignedSiteId(user.getAssignedSite().getId());
            dto.setAssignedSiteName(user.getAssignedSite().getSiteName());
            dto.setAssignedSiteCode(user.getAssignedSite().getSiteCode());
        }
        dto.setUpiId(user.getUpiId());
        dto.setBankAccountNumber(user.getBankAccountNumber());
        dto.setBankIfscCode(user.getBankIfscCode());
        dto.setBankName(user.getBankName());
        dto.setBankBeneficiaryName(user.getBankBeneficiaryName());
        dto.setQrCodeUrl(user.getQrCodeUrl());
        return dto;
    }
}
