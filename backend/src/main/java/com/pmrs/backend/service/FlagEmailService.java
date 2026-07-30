package com.pmrs.backend.service;

public interface FlagEmailService {

    /**
     * Sends the "please correct your registration" email to a flagged
     * student's form email. Runs asynchronously and never throws — a mail
     * failure must never break the flagging transaction.
     */
    void sendFlagEmail(String toEmail, String studentName, String comment);
}
