package com.pmrs.backend.controller;

import com.pmrs.backend.dto.ChangePasswordRequest;
import com.pmrs.backend.dto.LoginRequest;
import com.pmrs.backend.dto.LoginResponse;
import com.pmrs.backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@Tag(name = "Auth API", description = "Login and authentication")
@RestController
@RequestMapping("/auth")
@CrossOrigin("*")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Login and receive a JWT token")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401)
                    .body(Map.of("status", 401, "error", e.getMessage()));
        }
    }

    @Operation(summary = "Change the current user's password")
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request,
                                            Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("status", 401, "error", "Not authenticated"));
        }
        try {
            authService.changePassword(principal.getName(), request);
            return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401)
                    .body(Map.of("status", 401, "error", e.getMessage()));
        }
    }
}
