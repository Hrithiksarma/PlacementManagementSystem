package com.pmrs.backend.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * An in-app alert for placement-cell staff, shown via the notification bell
 * on the Forms page. Currently only raised for one event: a student
 * resubmitting a form after their earlier submission was flagged.
 */
@Entity
@Table(name = "AdminNotifications")
public class AdminNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Integer notificationId;

    @Column(name = "message", nullable = false, length = 500)
    private String message;

    /** The StudentFormSubmission row this notification should link back to. */
    @Column(name = "submission_id")
    private Integer submissionId;

    @Column(name = "is_read", nullable = false)
    private boolean read = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Integer getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(Integer notificationId) {
        this.notificationId = notificationId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(Integer submissionId) {
        this.submissionId = submissionId;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
