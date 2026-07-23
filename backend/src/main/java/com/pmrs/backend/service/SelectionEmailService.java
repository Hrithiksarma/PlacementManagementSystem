package com.pmrs.backend.service;

import com.pmrs.backend.entity.Application;

public interface SelectionEmailService {

    /**
     * Sends the congratulations email for a Selected application to the
     * student's institute email. Runs asynchronously and never throws —
     * a mail failure must never break the status-update transaction.
     */
    void sendSelectionEmail(Application application);
}
