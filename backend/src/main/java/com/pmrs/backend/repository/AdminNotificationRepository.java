package com.pmrs.backend.repository;

import com.pmrs.backend.entity.AdminNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdminNotificationRepository extends JpaRepository<AdminNotification, Integer> {

    List<AdminNotification> findTop50ByOrderByCreatedAtDesc();

    List<AdminNotification> findByReadFalse();

    int countByReadFalse();
}
