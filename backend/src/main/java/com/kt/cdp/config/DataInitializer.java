package com.kt.cdp.config;

import com.kt.cdp.entity.Role;
import com.kt.cdp.entity.User;
import com.kt.cdp.repository.RoleRepository;
import com.kt.cdp.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Seeds demo user accounts at startup with BCrypt-hashed passwords, so the demo
 * is reproducible without hardcoding password hashes in SQL migrations.
 * Idempotent: skips if any user already exists. Roles/permissions and all
 * domain demo data come from Flyway (V2). Demo credentials are documented in README.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Users already present ({}); skipping demo user seeding.", userRepository.count());
            return;
        }
        createUser("admin", "admin@cdp.local", "Admin@123", "System Administrator", "ADMINISTRATOR");
        createUser("officer", "officer@cdp.local", "Officer@123", "Prison Officer", "PRISON_OFFICER");
        createUser("police", "police@cdp.local", "Police@123", "Police Official", "POLICE_OFFICIAL");
        createUser("govt", "govt@cdp.local", "Govt@123", "Government Official", "GOVERNMENT_OFFICIAL");
        createUser("auditor", "auditor@cdp.local", "Auditor@123", "System Auditor", "AUDITOR");
        log.info("Seeded 5 demo users (admin/officer/police/govt/auditor).");
    }

    private void createUser(String username, String email, String rawPassword, String fullName, String roleName) {
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalStateException("Role not seeded: " + roleName
                        + " (ensure Flyway V2 ran)"));
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setFullName(fullName);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setActive(true);
        user.setRoles(Set.of(role));
        userRepository.save(user);
    }
}
