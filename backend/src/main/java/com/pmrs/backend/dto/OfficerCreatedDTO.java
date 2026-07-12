package com.pmrs.backend.dto;

/**
 * Returned exactly once, right after an officer account is created — the only
 * time the plaintext temporary password is ever available. The admin must
 * relay it to the officer out-of-band; it cannot be retrieved again afterward.
 */
public class OfficerCreatedDTO {
    private Long userId;
    private String username;
    private String email;
    private String temporaryPassword;

    public OfficerCreatedDTO(Long userId, String username, String email, String temporaryPassword) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.temporaryPassword = temporaryPassword;
    }

    public Long getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getTemporaryPassword() { return temporaryPassword; }
}
