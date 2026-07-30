package com.pmrs.backend.repository;

import com.pmrs.backend.entity.ReminderSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReminderSettingsRepository extends JpaRepository<ReminderSettings, Integer> {
}
