package com.pmrs.backend.service;

import java.util.List;

public interface DriveReminderService {

    /**
     * Event type identifiers shared with the frontend Calendar/Notify
     * Students pages.
     */
    String EVENT_PPT               = "PPT";
    String EVENT_RESUME_SELECTION  = "RESUME_SELECTION";
    String EVENT_EXAM              = "EXAM";
    String EVENT_FINAL_SELECTION   = "FINAL_SELECTION";

    /** Officer-initiated send — always sends, regardless of any prior send. */
    void sendReminder(Integer driveId, String eventType, List<Integer> studentIds);

    /**
     * Checks every drive's four event dates and emails eligible students for
     * any (drive, event) exactly {@code prms.reminder.days-before} days out
     * that hasn't already been reminded about. No-ops entirely when
     * {@link #isAutoReminderEnabled()} is false.
     */
    void runDailyReminders();

    boolean isAutoReminderEnabled();

    void setAutoReminderEnabled(boolean enabled);
}
