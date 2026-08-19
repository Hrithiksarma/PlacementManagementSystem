package com.pmrs.backend.service;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * Generates the one-time temporary passwords used whenever an account is
 * created or reset (staff portal, students) — never persisted in plaintext,
 * only ever handed to the caller to email or display once.
 */
@Component
public class TemporaryPasswordGenerator {

    private static final String PASSWORD_CHARS =
        "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#$%";
    private static final int PASSWORD_LENGTH = 12;
    private static final SecureRandom RANDOM = new SecureRandom();

    public String generate() {
        StringBuilder sb = new StringBuilder(PASSWORD_LENGTH);
        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            sb.append(PASSWORD_CHARS.charAt(RANDOM.nextInt(PASSWORD_CHARS.length())));
        }
        return sb.toString();
    }
}
