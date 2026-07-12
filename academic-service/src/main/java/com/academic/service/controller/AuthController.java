package com.academic.service.controller;

import com.academic.service.dto.LoginRequest;
import com.academic.service.dto.LoginResponse;
import com.academic.service.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtUtil               jwtUtil;

    public AuthController(AuthenticationManager authManager,
                          JwtUtil jwtUtil) {
        this.authManager = authManager;
        this.jwtUtil     = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getUsername(), request.getPassword()));

            UserDetails user  = (UserDetails) auth.getPrincipal();
            String      token = jwtUtil.generateToken(user.getUsername());

            return ResponseEntity.ok(
                new LoginResponse(token, user.getUsername(),
                    "Login successful"));

        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(401)
                .body(new LoginResponse(null, null,
                    "Invalid username or password"));
        }
    }
}
