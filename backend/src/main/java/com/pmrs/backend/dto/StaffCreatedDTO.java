package com.pmrs.backend.dto;

/**
 * Returned right after the shared staff account is created or its password is
 * reset — the only time the plaintext password is ever available. The admin
 * must relay it out-of-band to whichever departments should have access; it
 * cannot be retrieved again afterward.
 */
public class StaffCreatedDTO {
    private Long userId;
    private String username;
    private String email;
    private String temporaryPassword;

    public StaffCreatedDTO(Long userId, String username, String email, String temporaryPassword) {
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
