package com.pmrs.backend.repository;

import com.pmrs.backend.entity.DriveReminderLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DriveReminderLogRepository extends JpaRepository<DriveReminderLog, Integer> {

    Optional<DriveReminderLog> findByDriveIdAndEventType(Integer driveId, String eventType);

    boolean existsByDriveIdAndEventType(Integer driveId, String eventType);
}
