package in.gov.labourerp.security;

import in.gov.labourerp.entity.Business;
import in.gov.labourerp.entity.BusinessUser;
import in.gov.labourerp.entity.Role;
import in.gov.labourerp.entity.User;
import in.gov.labourerp.repository.BusinessRepository;
import in.gov.labourerp.repository.BusinessUserRepository;
import in.gov.labourerp.repository.RoleRepository;
import in.gov.labourerp.repository.UserRepository;
import in.gov.labourerp.service.SupabaseAuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(CustomUserDetailsService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BusinessRepository businessRepository;
    private final BusinessUserRepository businessUserRepository;
    private final SupabaseAuthService supabaseAuthService;

    public CustomUserDetailsService(UserRepository userRepository,
                                    RoleRepository roleRepository,
                                    BusinessRepository businessRepository,
                                    BusinessUserRepository businessUserRepository,
                                    SupabaseAuthService supabaseAuthService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.businessRepository = businessRepository;
        this.businessUserRepository = businessUserRepository;
        this.supabaseAuthService = supabaseAuthService;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        // 1. Look up existing user in public.users
        Optional<User> existingUserOpt = userRepository.findByUsername(usernameOrEmail)
                .or(() -> userRepository.findByEmail(usernameOrEmail));

        User user;
        if (existingUserOpt.isPresent()) {
            user = existingUserOpt.get();

            // Check if Supabase auth.users has an updated password from Supabase dashboard
            try {
                Optional<SupabaseAuthService.SupabaseUserRecord> supaAuth = supabaseAuthService.findAuthUserByEmail(user.getEmail());
                if (supaAuth.isPresent()) {
                    String supaPassword = supaAuth.get().getEncryptedPassword();
                    if (supaPassword != null && !supaPassword.equals(user.getPasswordHash())) {
                        log.info("Synchronizing updated Supabase Auth password hash for {}", user.getEmail());
                        user.setPasswordHash(supaPassword);
                        user.setSupabaseUid(supaAuth.get().getId());
                        userRepository.save(user);
                    }
                }
            } catch (Exception e) {
                log.debug("Supabase auth sync check: {}", e.getMessage());
            }
        } else {
            // 2. User not in public.users. Check if created directly in Supabase Authentication Dashboard!
            Optional<SupabaseAuthService.SupabaseUserRecord> supaAuth = supabaseAuthService.findAuthUserByEmail(usernameOrEmail);
            if (supaAuth.isPresent()) {
                log.info("Auto-provisioning user created in Supabase Auth: {}", supaAuth.get().getEmail());
                user = autoProvisionFromSupabase(supaAuth.get(), usernameOrEmail);
            } else {
                throw new UsernameNotFoundException("User not found with username or email: " + usernameOrEmail);
            }
        }

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPasswordHash(),
                "ACTIVE".equalsIgnoreCase(user.getStatus()),
                true,
                true,
                true,
                user.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority(role.getRoleCode()))
                        .collect(Collectors.toSet())
        );
    }

    private User autoProvisionFromSupabase(SupabaseAuthService.SupabaseUserRecord supaRecord, String originalInput) {
        String email = supaRecord.getEmail().toLowerCase().trim();
        String derivedUsername = email.contains("@") ? email.substring(0, email.indexOf('@')) : email;

        // Ensure username is unique
        int suffix = 1;
        String finalUsername = derivedUsername;
        while (userRepository.findByUsername(finalUsername).isPresent()) {
            finalUsername = derivedUsername + suffix++;
        }

        // Determine role from metadata or default to SITE_SUPERVISOR
        String roleCode = "ROLE_SITE_SUPERVISOR";
        String fullName = derivedUsername;
        if (supaRecord.getRawUserMetaData() != null) {
            String meta = supaRecord.getRawUserMetaData().toUpperCase();
            if (meta.contains("ROLE_ADMIN") || meta.contains("\"ADMIN\"")) {
                roleCode = "ROLE_ADMIN";
            } else if (meta.contains("ROLE_MANAGER") || meta.contains("\"MANAGER\"")) {
                roleCode = "ROLE_MANAGER";
            } else if (meta.contains("ROLE_ACCOUNTANT") || meta.contains("\"ACCOUNTANT\"")) {
                roleCode = "ROLE_ACCOUNTANT";
            }
        }

        final String effectiveRoleCode = roleCode;
        Role role = roleRepository.findByRoleCode(effectiveRoleCode)
                .orElseGet(() -> roleRepository.save(new Role(effectiveRoleCode, effectiveRoleCode.replace("ROLE_", ""), "Auto-provisioned role")));

        User newUser = new User();
        newUser.setUsername(finalUsername);
        newUser.setEmail(email);
        newUser.setPasswordHash(supaRecord.getEncryptedPassword());
        newUser.setFullName(fullName.substring(0, 1).toUpperCase() + fullName.substring(1));
        newUser.setStatus("ACTIVE");
        newUser.setSupabaseUid(supaRecord.getId());
        newUser.getRoles().add(role);

        User savedUser = userRepository.save(newUser);

        // Assign to primary business if available
        businessRepository.findAll().stream().findFirst().ifPresent(business -> {
            businessUserRepository.save(new BusinessUser(business, savedUser, role, true));
        });

        return savedUser;
    }
}
