package com.pmrs.backend.service;

public interface StaffWelcomeEmailService {
    void sendWelcomeEmail(String toEmail, String username, String temporaryPassword, String comment);
}
