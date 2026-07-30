package com.pmrs.backend.service;

import com.pmrs.backend.dto.CreateOfficerRequest;
import com.pmrs.backend.dto.OfficerAccountDTO;
import com.pmrs.backend.dto.OfficerCreatedDTO;
import com.pmrs.backend.entity.Role;
import com.pmrs.backend.entity.User;
import com.pmrs.backend.exception.ResourceNotFoundException;
import com.pmrs.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;

@Service
public class OfficerAccountServiceImpl implements OfficerAccountService {

    private static final String PASSWORD_CHARS =
        "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#$%";
    private static final int PASSWORD_LENGTH = 12;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository             userRepository;
    private final PasswordEncoder            passwordEncoder;
    private final OfficerWelcomeEmailService officerWelcomeEmailService;

    public OfficerAccountServiceImpl(UserRepository             userRepository,
                                     PasswordEncoder            passwordEncoder,
                                     OfficerWelcomeEmailService officerWelcomeEmailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.officerWelcomeEmailService = officerWelcomeEmailService;
    }

    @Override
    public OfficerCreatedDTO createOfficer(CreateOfficerRequest request) {
        String username = request.getUsername().trim();
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already taken: " + username);
        }

        String temporaryPassword = generateTemporaryPassword();

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(temporaryPassword));
        user.setEmail(request.getEmail().trim());
        // Role is always hardcoded here — never taken from client input — so
        // this endpoint can never be used to mint an ADMIN account.
        user.setRole(Role.PLACEMENT_OFFICER);
        user.setMustChangePassword(true);
        user.setEnabled(true);
        User saved = userRepository.save(user);

        return new OfficerCreatedDTO(saved.getUserId(), saved.getUsername(), saved.getEmail(), temporaryPassword);
    }

    @Override
    public List<OfficerAccountDTO> listOfficers() {
        return userRepository.findByRole(Role.PLACEMENT_OFFICER).stream()
            .map(u -> new OfficerAccountDTO(u.getUserId(), u.getUsername(), u.getEmail(),
                                             u.isEnabled(), u.isMustChangePassword()))
            .toList();
    }

    @Override
    public void setEnabled(Long userId, boolean enabled) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Officer account not found: " + userId));

        // Guards against ever disabling a non-officer account through this path,
        // even if a caller passes an admin's or student's userId.
        if (user.getRole() != Role.PLACEMENT_OFFICER) {
            throw new IllegalArgumentException("User " + userId + " is not a placement officer account.");
        }

        user.setEnabled(enabled);
        userRepository.save(user);
    }

    @Override
    public void sendWelcomeEmail(Long userId, String temporaryPassword, String comment) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Officer account not found: " + userId));

        if (user.getRole() != Role.PLACEMENT_OFFICER) {
            throw new IllegalArgumentException("User " + userId + " is not a placement officer account.");
        }

        officerWelcomeEmailService.sendWelcomeEmail(
                user.getEmail(), user.getUsername(), temporaryPassword, comment);
    }

    private String generateTemporaryPassword() {
        StringBuilder sb = new StringBuilder(PASSWORD_LENGTH);
        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            sb.append(PASSWORD_CHARS.charAt(RANDOM.nextInt(PASSWORD_CHARS.length())));
        }
        return sb.toString();
    }
}
