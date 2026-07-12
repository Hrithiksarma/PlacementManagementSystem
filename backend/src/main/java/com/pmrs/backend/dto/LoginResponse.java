package com.pmrs.backend.dto;

public class LoginResponse {
    private String token;
    private String role;
    private String username;
    private String email;
    private boolean mustChangePassword;

    public LoginResponse(String token, String role, String username, String email,
                          boolean mustChangePassword) {
        this.token = token;
        this.role = role;
        this.username = username;
        this.email = email;
        this.mustChangePassword = mustChangePassword;
    }

    public String getToken() { return token; }
    public String getRole() { return role; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public boolean isMustChangePassword() { return mustChangePassword; }
}
