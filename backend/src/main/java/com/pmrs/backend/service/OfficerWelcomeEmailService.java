package com.pmrs.backend.service;

public interface OfficerWelcomeEmailService {

    /**
     * Sends the "your officer account was created" email — username,
     * temporary password, and an optional admin note. Runs asynchronously
     * and never throws — a mail failure must never break the caller.
     */
    void sendWelcomeEmail(String toEmail, String username, String temporaryPassword, String comment);
}
