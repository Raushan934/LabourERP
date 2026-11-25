package in.gov.labourerp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class SupabaseAuthService {

    private static final Logger log = LoggerFactory.getLogger(SupabaseAuthService.class);

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public SupabaseAuthService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public static class SupabaseUserRecord {
        private String id;
        private String email;
        private String encryptedPassword;
        private String rawUserMetaData;

        public SupabaseUserRecord(String id, String email, String encryptedPassword, String rawUserMetaData) {
            this.id = id;
            this.email = email;
            this.encryptedPassword = encryptedPassword;
            this.rawUserMetaData = rawUserMetaData;
        }

        public String getId() { return id; }
        public String getEmail() { return email; }
        public String getEncryptedPassword() { return encryptedPassword; }
        public String getRawUserMetaData() { return rawUserMetaData; }
    }

    /**
     * Find user directly from Supabase auth.users table
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public Optional<SupabaseUserRecord> findAuthUserByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return Optional.empty();
        }
        try {
            String sql = "SELECT id::text, email, encrypted_password, raw_user_meta_data::text " +
                         "FROM auth.users WHERE LOWER(email) = LOWER(?) LIMIT 1";
            SupabaseUserRecord record = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> new SupabaseUserRecord(
                    rs.getString("id"),
                    rs.getString("email"),
                    rs.getString("encrypted_password"),
                    rs.getString("raw_user_meta_data")
            ), email.trim());
            return Optional.ofNullable(record);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        } catch (Exception e) {
            log.warn("Supabase auth.users query notice: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Synchronize a user to Supabase auth.users so they appear in Supabase Authentication Dashboard
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void syncUserToSupabaseAuth(String email, String rawPassword, String roleCode, String fullName) {
        if (email == null || email.trim().isEmpty()) return;
        try {
            Optional<SupabaseUserRecord> existing = findAuthUserByEmail(email);
            String encodedPassword = passwordEncoder.encode(rawPassword);
            String metadataJson = String.format("{\"role\": \"%s\", \"full_name\": \"%s\"}",
                    roleCode != null ? roleCode : "ROLE_SITE_SUPERVISOR",
                    fullName != null ? fullName.replace("\"", "\\\"") : "");

            if (existing.isPresent()) {
                String updateSql = "UPDATE auth.users SET encrypted_password = ?, raw_user_meta_data = ?::jsonb, updated_at = NOW() WHERE LOWER(email) = LOWER(?)";
                jdbcTemplate.update(updateSql, encodedPassword, metadataJson, email.trim());
                log.info("Updated credentials in Supabase auth.users for {}", email);
            } else {
                UUID userId = UUID.randomUUID();
                String insertSql = "INSERT INTO auth.users (" +
                        "instance_id, id, aud, role, email, encrypted_password, email_confirmed_at, " +
                        "raw_app_meta_data, raw_user_meta_data, created_at, updated_at" +
                        ") VALUES (" +
                        "'00000000-0000-0000-0000-000000000000'::uuid, ?::uuid, 'authenticated', 'authenticated', LOWER(?), ?, NOW(), " +
                        "'{\"provider\":\"email\",\"providers\":[\"email\"]}'::jsonb, ?::jsonb, NOW(), NOW())";
                jdbcTemplate.update(insertSql, userId.toString(), email.trim(), encodedPassword, metadataJson);
                log.info("Inserted user into Supabase auth.users for {}", email);

                // Optional identity insert with robust try/catch
                try {
                    String identitySql = "INSERT INTO auth.identities (" +
                            "id, user_id, identity_data, provider, provider_id, last_sign_in_at, created_at, updated_at" +
                            ") VALUES (" +
                            "?::uuid, ?::uuid, ?::jsonb, 'email', LOWER(?), NOW(), NOW(), NOW())";
                    String identityData = String.format("{\"sub\": \"%s\", \"email\": \"%s\"}", userId, email.trim().toLowerCase());
                    jdbcTemplate.update(identitySql, userId.toString(), userId.toString(), identityData, email.trim());
                } catch (Exception ex) {
                    log.debug("auth.identities insert notice: {}", ex.getMessage());
                }
            }
        } catch (Exception e) {
            log.warn("Supabase auth sync notice (non-fatal): {}", e.getMessage());
        }
    }

    /**
     * Update password in Supabase auth.users
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void updatePasswordInSupabaseAuth(String email, String newPassword) {
        if (email == null || email.trim().isEmpty()) return;
        try {
            String encodedPassword = passwordEncoder.encode(newPassword);
            String updateSql = "UPDATE auth.users SET encrypted_password = ?, updated_at = NOW() WHERE LOWER(email) = LOWER(?)";
            int rows = jdbcTemplate.update(updateSql, encodedPassword, email.trim());
            if (rows > 0) {
                log.info("Synchronized new password to Supabase auth.users for {}", email);
            }
        } catch (Exception e) {
            log.warn("Could not update password in Supabase auth.users: {}", e.getMessage());
        }
    }
}
