package com.pmrs.backend.service;

import com.pmrs.backend.entity.Drive;

import java.time.LocalDate;
import java.util.List;

public interface DriveReminderEmailService {

    /**
     * Sends an individual reminder email (never a shared To/CC) to each
     * address about the given drive event. Runs asynchronously and never
     * throws — a mail failure must never break the caller.
     */
    void sendReminderEmails(List<String> toEmails, Drive drive, String eventLabel, LocalDate eventDate);
}
