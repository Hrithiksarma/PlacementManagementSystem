package com.pmrs.backend.service;

import com.pmrs.backend.entity.AdminNotification;
import com.pmrs.backend.entity.StudentFormSubmission;

import java.util.List;

/**
 * In-app notifications for placement-cell staff. Currently raised only when
 * a student resubmits a form after their earlier submission was flagged.
 */
public interface AdminNotificationService {

    List<AdminNotification> listRecent();

    long unreadCount();

    void markRead(Integer notificationId);

    void markAllRead();

    /** Raises a notification pointing at the newly-synced resubmission row. */
    void notifyResubmission(StudentFormSubmission newSubmission);
}
