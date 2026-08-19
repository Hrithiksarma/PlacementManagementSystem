package com.pmrs.backend.service;
import com.pmrs.backend.dto.CreateStaffRequest;
import com.pmrs.backend.dto.StaffAccountDTO;
import com.pmrs.backend.dto.StaffCreatedDTO;

import java.util.Optional;

/**
 * Manages the single, shared "department staff" portal account. Unlike
 * placement officers, staff is a singleton role — one set of credentials
 * handed out to any department that needs read-only dashboard visibility.
 */
public interface StaffAccountService {

    /** Fails if a staff account already exists — use resetPassword() to rotate it instead. */
    StaffCreatedDTO createStaff(CreateStaffRequest request);

    Optional<StaffAccountDTO> getStaffAccount();

    /** Regenerates the shared password and forces a change on next login. Fails if no account exists. */
    StaffCreatedDTO resetPassword();

    void setEnabled(Long userId, boolean enabled);

    /**
     * Emails the shared staff account's username, temporary password, and an
     * optional admin note. The plaintext password is never stored, so the
     * caller must supply the one it was shown at creation/reset time.
     */
    void sendWelcomeEmail(Long userId, String temporaryPassword, String comment);
}
