package com.pmrs.backend.service;

import com.pmrs.backend.dto.CreateOfficerRequest;
import com.pmrs.backend.dto.OfficerAccountDTO;
import com.pmrs.backend.dto.OfficerCreatedDTO;

import java.util.List;

public interface OfficerAccountService {

    OfficerCreatedDTO createOfficer(CreateOfficerRequest request);

    List<OfficerAccountDTO> listOfficers();

    void setEnabled(Long userId, boolean enabled);

    /**
     * Emails a placement officer their username, temporary password, and an
     * optional admin note. The plaintext password is never stored, so the
     * caller must supply the one it was shown at creation time.
     */
    void sendWelcomeEmail(Long userId, String temporaryPassword, String comment);
}
