package com.pmrs.backend.service;

import com.pmrs.backend.entity.AdminNotification;
import com.pmrs.backend.entity.StudentFormSubmission;
import com.pmrs.backend.exception.ResourceNotFoundException;
import com.pmrs.backend.repository.AdminNotificationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminNotificationServiceImpl implements AdminNotificationService {

    private final AdminNotificationRepository notificationRepository;

    public AdminNotificationServiceImpl(AdminNotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public List<AdminNotification> listRecent() {
        return notificationRepository.findTop50ByOrderByCreatedAtDesc();
    }

    @Override
    public long unreadCount() {
        return notificationRepository.countByReadFalse();
    }

    @Override
    public void markRead(Integer notificationId) {
        AdminNotification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Notification not found with id: " + notificationId));
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Override
    public void markAllRead() {
        List<AdminNotification> unread = notificationRepository.findByReadFalse();
        for (AdminNotification n : unread) {
            n.setRead(true);
        }
        notificationRepository.saveAll(unread);
    }

    @Override
    public void notifyResubmission(StudentFormSubmission newSubmission) {
        String who = (newSubmission.getFullName() != null && !newSubmission.getFullName().isBlank())
                ? newSubmission.getFullName() : "A student";
        String rollNo = newSubmission.getRollNo();
        String message = who + (rollNo != null ? " (" + rollNo + ")" : "") + " resubmitted their registration.";

        AdminNotification notification = new AdminNotification();
        notification.setMessage(message);
        notification.setSubmissionId(newSubmission.getSubmissionId());
        notificationRepository.save(notification);
    }
}
