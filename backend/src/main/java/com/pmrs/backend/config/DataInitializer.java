package com.pmrs.backend.config;

import com.pmrs.backend.entity.Role;
import com.pmrs.backend.entity.Student;
import com.pmrs.backend.entity.User;
import com.pmrs.backend.repository.DepartmentRepository;
import com.pmrs.backend.repository.StudentRepository;
import com.pmrs.backend.repository.UserRepository;
import com.pmrs.backend.service.RollNumberService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Seeds demo admin/officer/student accounts for local development. Runs by
 * default (no active profile), but is skipped entirely with
 * {@code spring.profiles.active=prod} — a real deployment should have exactly
 * one admin account, bootstrapped directly in MySQL, and zero seeded demo
 * accounts with known passwords.
 */
@Profile("!prod")
@Component
public class DataInitializer implements ApplicationRunner {

    private static final String SEED_STUDENT_EMAIL   = "student@pmrs.edu";
    private static final String SEED_STUDENT_PHONE    = "9876543210";

    private final UserRepository       userRepository;
    private final StudentRepository    studentRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder      passwordEncoder;
    private final RollNumberService    rollNumberService;

    public DataInitializer(UserRepository userRepository,
                           StudentRepository studentRepository,
                           DepartmentRepository departmentRepository,
                           PasswordEncoder passwordEncoder,
                           RollNumberService rollNumberService) {
        this.userRepository       = userRepository;
        this.studentRepository    = studentRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder      = passwordEncoder;
        this.rollNumberService    = rollNumberService;
    }

    @Override
    public void run(ApplicationArguments args) {
        seedUser("admin",   "admin123",   "admin@pmrs.com",   Role.ADMIN,             null);
        seedUser("officer", "officer123", "officer@pmrs.com", Role.PLACEMENT_OFFICER, null);
        seedStudentUser();
    }

    // Demo/fixed accounts have a known password up front, so they're never
    // subject to the forced first-login password change.
    private void seedUser(String username, String rawPassword, String email,
                          Role role, Integer studentId) {
        if (!userRepository.existsByUsername(username)) {
            User u = new User();
            u.setUsername(username);
            u.setPassword(passwordEncoder.encode(rawPassword));
            u.setEmail(email);
            u.setRole(role);
            u.setStudentId(studentId);
            u.setMustChangePassword(false);
            userRepository.save(u);
        }
    }

    private void seedStudentUser() {
        userRepository.findByUsername("student").ifPresentOrElse(
            existing -> {
                if (existing.getStudentId() == null) {
                    Student s = findOrCreateSeedStudent();
                    existing.setStudentId(s.getStudentId());
                    userRepository.save(existing);
                }
            },
            () -> {
                Student s = findOrCreateSeedStudent();
                User u = new User();
                u.setUsername("student");
                u.setPassword(passwordEncoder.encode("student123"));
                u.setEmail(SEED_STUDENT_EMAIL);
                u.setRole(Role.STUDENT);
                u.setStudentId(s.getStudentId());
                u.setMustChangePassword(false);
                userRepository.save(u);
            }
        );
    }

    /**
     * Creates the demo seed student if one does not already exist.
     *
     * Roll number generation now goes through {@link RollNumberService} —
     * the same path as the REST API — so the seed student receives a proper
     * roll number and the counter table stays consistent.
     *
     * The method is {@code @Transactional} so the counter increment and the
     * student INSERT commit together, matching the behaviour of
     * {@link com.pmrs.backend.service.StudentServiceImpl#saveStudent}.
     */
    @Transactional
    private Student findOrCreateSeedStudent() {
        return studentRepository.findByPhone(SEED_STUDENT_PHONE).orElseGet(() -> {
            Student s = new Student();
            s.setName("Rahul Sharma");
            s.setEmail(SEED_STUDENT_EMAIL);
            s.setPhone(SEED_STUDENT_PHONE);
            s.setBatchYear(2025);
            s.setCgpa(8.2);
            s.setActiveBacklogs(0);
            s.setPlacementTier("Dream");

            departmentRepository.findAll().stream()
                .filter(d -> "CSE".equalsIgnoreCase(d.getBranch())
                          && "B.Tech".equalsIgnoreCase(d.getProgram()))
                .findFirst()
                .ifPresent(dept -> {
                    s.setDepartment(dept);
                    // Generate roll number through the service — not directly.
                    String rollNo = rollNumberService.generateRollNumber(
                        dept.getDeptId(), s.getBatchYear()
                    );
                    s.setRollNo(rollNo);
                });

            return studentRepository.save(s);
        });
    }
}