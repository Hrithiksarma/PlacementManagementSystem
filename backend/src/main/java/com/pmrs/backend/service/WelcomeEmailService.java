package com.pmrs.backend.service;

public interface WelcomeEmailService {

    /**
     * Sends the "account created" welcome email to a newly-created student
     * account. Runs asynchronously and never throws — a mail failure must
     * never break account creation or the import it's part of.
     *
     * <p>Takes plain Strings (not the Student entity) so the async method,
     * running on a different thread, can't trip over Hibernate lazy-loading.
     */
    void sendWelcomeEmail(String toEmail, String studentName, String rollNo);
}
