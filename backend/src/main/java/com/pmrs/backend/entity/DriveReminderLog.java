package com.pmrs.backend.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Records that a reminder email for a given drive event has already gone
 * out — checked by the daily automatic job so it never re-sends for the
 * same (drive, event) pair, whether the earlier send was automatic or a
 * manual "Send Reminder" from the Notify Students page.
 */
@Entity
@Table(name = "DriveReminderLogs", uniqueConstraints = {
        @UniqueConstraint(name = "uq_drive_reminder_log", columnNames = { "drive_id", "event_type" })
})
public class DriveReminderLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Integer logId;

    @Column(name = "drive_id", nullable = false)
    private Integer driveId;

    /** PPT / RESUME_SELECTION / EXAM / FINAL_SELECTION — see DriveReminderService. */
    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt = LocalDateTime.now();

    public Integer getLogId() {
        return logId;
    }

    public void setLogId(Integer logId) {
        this.logId = logId;
    }

    public Integer getDriveId() {
        return driveId;
    }

    public void setDriveId(Integer driveId) {
        this.driveId = driveId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }
}
