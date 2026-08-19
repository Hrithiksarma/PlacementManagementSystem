package com.pmrs.backend.dto;

/** Read-only view of the shared department-staff portal account — never carries the password. */
public class StaffAccountDTO {
    private Long userId;
    private String username;
    private String email;
    private boolean enabled;
    private boolean mustChangePassword;

    public StaffAccountDTO(Long userId, String username, String email,
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
