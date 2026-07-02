package com.scholarfund.backend.config;

import com.scholarfund.backend.common.exception.ErrorCode;
import com.scholarfund.backend.common.exception.ScholarFundException;
import com.scholarfund.backend.entity.Role;
import com.scholarfund.backend.entity.User;
import com.scholarfund.backend.repository.RoleRepository;
import com.scholarfund.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminDataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {

        String adminEmail = "admin@subha.gov.in";
        String adminPassword = "Admin#123!";

        if (userRepository.findByEmail(adminEmail).isEmpty()) {
            log.info("Admin user not found. Creating default Government Admin account...");

            Role adminRole = roleRepository.findByRoleName("GOVT")
                    .orElseThrow(() -> new ScholarFundException("Role not found", ErrorCode.NOT_FOUND, null));

            User admin = new User();
            admin.setEmail(adminEmail);
            admin.setFullName("Government Administrator");
            admin.setRole(adminRole);

            admin.setPasswordHash(passwordEncoder.encode(adminPassword));

            userRepository.save(admin);
            log.info("Default Admin account created successfully! You can now log in.");
        } else {
            log.info("Default Admin account already exists in the database.");
        }
    }
}