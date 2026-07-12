package com.pmrs.backend.dto;

/** Read-only view of a placement officer account — never carries the password. */
public class OfficerAccountDTO {
    private Long userId;
    private String username;
    private String email;
    private boolean enabled;
    private boolean mustChangePassword;

    public OfficerAccountDTO(Long userId, String username, String email,
                              boolean enabled, boolean mustChangePassword) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.enabled = enabled;
        this.mustChangePassword = mustChangePassword;
    }

    public Long getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public boolean isEnabled() { return enabled; }
    public boolean isMustChangePassword() { return mustChangePassword; }
}
