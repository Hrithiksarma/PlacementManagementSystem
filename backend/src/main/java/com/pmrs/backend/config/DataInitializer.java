package com.pmrs.backend.config;

import com.pmrs.backend.entity.Role;
import com.pmrs.backend.entity.Student;
import com.pmrs.backend.entity.User;
import com.pmrs.backend.repository.DepartmentRepository;
import com.pmrs.backend.repository.StudentRepository;
import com.pmrs.backend.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements ApplicationRunner {

    private static final String SEED_STUDENT_EMAIL    = "student@pmrs.edu";
    private static final String DEFAULT_STUDENT_PASS  = "student123";

    private final UserRepository       userRepository;
    private final StudentRepository    studentRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder      passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           StudentRepository studentRepository,
                           DepartmentRepository departmentRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository       = userRepository;
        this.studentRepository    = studentRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder      = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        seedUser("admin",   "admin123",   "admin@pmrs.com",   Role.ADMIN,              null);
        seedUser("officer", "officer123", "officer@pmrs.com", Role.PLACEMENT_OFFICER,  null);
        seedStudentUser();
        seedAllStudentAccounts();
    }

    // ── Generic user seeder (no student link) ────────────────────────────────

    private void seedUser(String username, String rawPassword, String email,
                          Role role, Integer studentId) {
        if (!userRepository.existsByUsername(username)) {
            User u = new User();
            u.setUsername(username);
            u.setPassword(passwordEncoder.encode(rawPassword));
            u.setEmail(email);
            u.setRole(role);
            u.setStudentId(studentId);
            userRepository.save(u);
        }
    }

    // ── Student user seeder with Student record linking ───────────────────────

    private void seedStudentUser() {
        userRepository.findByUsername("student").ifPresentOrElse(
            existing -> {
                // Back-fill studentId if missing (upgrading from previous schema)
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
                userRepository.save(u);
            }
        );
    }

    // ── Bulk student account generation ──────────────────────────────────────

    private void seedAllStudentAccounts() {
        String encodedPass = passwordEncoder.encode(DEFAULT_STUDENT_PASS);

        for (Student student : studentRepository.findAll()) {
            if (userRepository.existsByStudentId(student.getStudentId())) {
                System.out.println("Student account already exists: "
                        + toUsername(student.getName()));
                continue;
            }

            String username = toUsername(student.getName());
            // Append studentId suffix if the derived username is already taken
            // by a different account (e.g., two students named "Raj Singh")
            if (userRepository.existsByUsername(username)) {
                username = username + student.getStudentId();
            }

            User u = new User();
            u.setUsername(username);
            u.setPassword(encodedPass);
            u.setEmail(username + "@pmrs.com");
            u.setRole(Role.STUDENT);
            u.setStudentId(student.getStudentId());
            userRepository.save(u);

            System.out.println("Created student account: " + username);
        }
    }

    private static String toUsername(String name) {
        if (name == null) return "student";
        return name.trim().toLowerCase().replace(" ", "");
    }

    // ── Seed student helper ───────────────────────────────────────────────────

    private static final String SEED_STUDENT_PHONE = "9876543210";

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
            // Link to first available CSE B.Tech department, if any
            departmentRepository.findAll().stream()
                .filter(d -> "CSE".equalsIgnoreCase(d.getBranch())
                          && "B.Tech".equalsIgnoreCase(d.getProgram()))
                .findFirst()
                .ifPresent(s::setDepartment);
            return studentRepository.save(s);
        });
    }
}
