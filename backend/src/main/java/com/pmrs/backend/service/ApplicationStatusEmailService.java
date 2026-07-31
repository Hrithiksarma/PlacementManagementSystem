package com.pmrs.backend.service;

import com.pmrs.backend.entity.Application;

public interface ApplicationStatusEmailService {

    /**
     * Sends a stage-appropriate notification email for the application's
     * current status (First Round, Interview Scheduled, Selected, Rejected)
     * to the student's institute email, with an optional admin comment
     * appended as an extra paragraph. Runs asynchronously and never throws —
     * a mail failure must never break the status-update transaction.
     */
    void sendStatusEmail(Application application, String comment);
}
