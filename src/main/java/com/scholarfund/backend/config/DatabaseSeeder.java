package com.scholarfund.backend.config;

import com.scholarfund.backend.entity.Role;
import com.scholarfund.backend.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        // Check if the roles table is empty
        if (roleRepository.count() == 0) {
            log.info("Seeding database with initial roles...");

            Role student = new Role();
            student.setRoleName("STUDENT");

            Role college = new Role();
            college.setRoleName("COLLEGE");

            Role bank = new Role();
            bank.setRoleName("BANK");

            Role govt = new Role();
            govt.setRoleName("GOVT");

            // Save all 4 roles to MySQL
            roleRepository.saveAll(List.of(student, college, bank, govt));

            log.info("Roles seeded successfully.");
        }
    }
}