package com.academic.service.config;

import com.academic.service.entity.AcademicUser;
import com.academic.service.repository.AcademicUserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements ApplicationRunner {

    private final AcademicUserRepository userRepository;
    private final PasswordEncoder        passwordEncoder;

    public DataInitializer(AcademicUserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository  = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        seedAdmin("academic_admin", "academic123", "ACADEMIC_ADMIN");
    }

    private void seedAdmin(String username, String rawPassword, String role) {
        if (!userRepository.existsByUsername(username)) {
            AcademicUser user = new AcademicUser();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(rawPassword));
            user.setRole(role);
            userRepository.save(user);
            System.out.println("Academic ERP: created admin account → " + username);
        }
    }
}
