package com.emailplatform.config;

import com.emailplatform.model.User;
import com.emailplatform.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Idempotent bootstrap seeder.
 *
 * When ADMIN_EMAIL and ADMIN_PASSWORD are set the first startup creates an
 * ADMIN user. Safe to leave enabled in production - it never overwrites an
 * existing account and never writes secrets to logs.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin-email:}")
    private String adminEmail;

    @Value("${app.admin-password:}")
    private String adminPassword;

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!StringUtils.hasText(adminEmail) || !StringUtils.hasText(adminPassword)) {
            return;
        }
        String email = adminEmail.trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            return;
        }
        User admin = new User();
        admin.setEmail(email);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setName("Administrator");
        admin.setRole("ADMIN");
        userRepository.save(admin);
        log.info("Seeded admin account for {}", email);
    }
}
