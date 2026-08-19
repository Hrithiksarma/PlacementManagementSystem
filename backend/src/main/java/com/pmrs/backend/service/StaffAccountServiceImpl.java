package com.pmrs.backend.service;

import com.pmrs.backend.dto.CreateStaffRequest;
import com.pmrs.backend.dto.StaffAccountDTO;
import com.pmrs.backend.dto.StaffCreatedDTO;
import com.pmrs.backend.entity.Role;
import com.pmrs.backend.entity.User;
import com.pmrs.backend.exception.ResourceNotFoundException;
import com.pmrs.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StaffAccountServiceImpl implements StaffAccountService {

    private final UserRepository             userRepository;
    private final PasswordEncoder            passwordEncoder;
    private final StaffWelcomeEmailService   staffWelcomeEmailService;
    private final TemporaryPasswordGenerator temporaryPasswordGenerator;

    public StaffAccountServiceImpl(UserRepository             userRepository,
                                   PasswordEncoder            passwordEncoder,
                                   StaffWelcomeEmailService   staffWelcomeEmailService,
                                   TemporaryPasswordGenerator temporaryPasswordGenerator) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.staffWelcomeEmailService = staffWelcomeEmailService;
        this.temporaryPasswordGenerator = temporaryPasswordGenerator;
    }

    @Override
    public StaffCreatedDTO createStaff(CreateStaffRequest request) {
        List<User> existing = userRepository.findByRole(Role.STAFF);
        if (!existing.isEmpty()) {
            throw new IllegalStateException(
                "A staff portal account already exists — reset its password or enable/disable it instead of creating another.");
        }

        String username = request.getUsername().trim();
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already taken: " + username);
        }

        String temporaryPassword = temporaryPasswordGenerator.generate();

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(temporaryPassword));
        user.setEmail(request.getEmail().trim());
        // Role is always hardcoded here — never taken from client input — so
        // this endpoint can never be used to mint an ADMIN account.
        user.setRole(Role.STAFF);
        user.setMustChangePassword(true);
        user.setEnabled(true);
        User saved = userRepository.save(user);

        return new StaffCreatedDTO(saved.getUserId(), saved.getUsername(), saved.getEmail(), temporaryPassword);
    }

    @Override
    public Optional<StaffAccountDTO> getStaffAccount() {
        return userRepository.findByRole(Role.STAFF).stream()
            .findFirst()
            .map(u -> new StaffAccountDTO(u.getUserId(), u.getUsername(), u.getEmail(),
                                           u.isEnabled(), u.isMustChangePassword()));
    }

    @Override
    public StaffCreatedDTO resetPassword() {
        User user = requireStaffAccount();

        String temporaryPassword = temporaryPasswordGenerator.generate();
        user.setPassword(passwordEncoder.encode(temporaryPassword));
        user.setMustChangePassword(true);
        User saved = userRepository.save(user);

        return new StaffCreatedDTO(saved.getUserId(), saved.getUsername(), saved.getEmail(), temporaryPassword);
    }

    @Override
    public void setEnabled(Long userId, boolean enabled) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Staff account not found: " + userId));

        // Guards against ever disabling a non-staff account through this path,
        // even if a caller passes an admin's or student's userId.
        if (user.getRole() != Role.STAFF) {
            throw new IllegalArgumentException("User " + userId + " is not the staff portal account.");
        }

        user.setEnabled(enabled);
        userRepository.save(user);
    }

    @Override
    public void sendWelcomeEmail(Long userId, String temporaryPassword, String comment) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Staff account not found: " + userId));

        if (user.getRole() != Role.STAFF) {
            throw new IllegalArgumentException("User " + userId + " is not the staff portal account.");
        }

        staffWelcomeEmailService.sendWelcomeEmail(
                user.getEmail(), user.getUsername(), temporaryPassword, comment);
    }

    private User requireStaffAccount() {
        return userRepository.findByRole(Role.STAFF).stream()
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("No staff portal account exists yet."));
    }
}
