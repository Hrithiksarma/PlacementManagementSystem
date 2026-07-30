package com.pmrs.backend.service;

import com.pmrs.backend.entity.Drive;
import com.pmrs.backend.entity.DriveReminderLog;
import com.pmrs.backend.entity.ReminderSettings;
import com.pmrs.backend.entity.Student;
import com.pmrs.backend.exception.ResourceNotFoundException;
import com.pmrs.backend.repository.DriveReminderLogRepository;
import com.pmrs.backend.repository.DriveRepository;
import com.pmrs.backend.repository.ReminderSettingsRepository;
import com.pmrs.backend.repository.StudentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DriveReminderServiceImpl implements DriveReminderService {

    private static final Logger log = LoggerFactory.getLogger(DriveReminderServiceImpl.class);

    /** Single row this table ever has — see {@link ReminderSettings}. */
    private static final Integer SETTINGS_ID = 1;

    private final DriveRepository             driveRepository;
    private final StudentRepository           studentRepository;
    private final DriveReminderEmailService   reminderEmailService;
    private final DriveReminderLogRepository  reminderLogRepository;
    private final ReminderSettingsRepository  reminderSettingsRepository;
    private final DriveEligibilityService     driveEligibilityService;

    @Value("${prms.reminder.days-before:7}")
    private int daysBefore;

    public DriveReminderServiceImpl(DriveRepository             driveRepository,
                                    StudentRepository            studentRepository,
                                    DriveReminderEmailService    reminderEmailService,
                                    DriveReminderLogRepository   reminderLogRepository,
                                    ReminderSettingsRepository   reminderSettingsRepository,
                                    DriveEligibilityService      driveEligibilityService) {
        this.driveRepository            = driveRepository;
        this.studentRepository          = studentRepository;
        this.reminderEmailService       = reminderEmailService;
        this.reminderLogRepository      = reminderLogRepository;
        this.reminderSettingsRepository = reminderSettingsRepository;
        this.driveEligibilityService    = driveEligibilityService;
    }

    @Override
    public void sendReminder(Integer driveId, String eventType, List<Integer> studentIds) {
        Drive drive = driveRepository.findById(driveId)
                .orElseThrow(() -> new ResourceNotFoundException("Drive not found with id: " + driveId));

        LocalDate eventDate = resolveEventDate(drive, eventType);
        String eventLabel = resolveEventLabel(eventType);

        List<String> emails = studentRepository.findAllById(studentIds).stream()
                .map(Student::getEmail)
                .filter(e -> e != null && !e.isBlank())
                .toList();

        reminderEmailService.sendReminderEmails(emails, drive, eventLabel, eventDate);
        recordSent(driveId, eventType);
    }

    /**
     * Not {@code @Scheduled} itself — {@link #scheduledDailyReminders()}
     * wraps this in a try/catch, same shape as GoogleFormSyncService's and
     * StudentFormSyncService's scheduled methods, so a failure here never
     * kills the scheduler thread.
     */
    @Override
    public void runDailyReminders() {
        if (!isAutoReminderEnabled()) {
            log.info("Automatic drive reminders are turned off — skipping today's run.");
            return;
        }

        LocalDate targetDate = LocalDate.now().plusDays(daysBefore);
        for (Drive drive : driveRepository.findAll()) {
            checkAndSend(drive, EVENT_PPT, drive.getPptDate(), targetDate);
            checkAndSend(drive, EVENT_RESUME_SELECTION, drive.getResumeSelectionDate(), targetDate);
            checkAndSend(drive, EVENT_EXAM, drive.getDriveDate(), targetDate);
            checkAndSend(drive, EVENT_FINAL_SELECTION, drive.getFinalSelectionDate(), targetDate);
        }
    }

    @Scheduled(cron = "${prms.reminder.cron:0 0 8 * * *}")
    public void scheduledDailyReminders() {
        try {
            runDailyReminders();
        } catch (Exception e) {
            log.warn("Daily drive reminder run failed: {}", e.getMessage());
        }
    }

    @Override
    public boolean isAutoReminderEnabled() {
        return getOrCreateSettings().isEnabled();
    }

    @Override
    public void setAutoReminderEnabled(boolean enabled) {
        ReminderSettings settings = getOrCreateSettings();
        settings.setEnabled(enabled);
        reminderSettingsRepository.save(settings);
    }

    private ReminderSettings getOrCreateSettings() {
        return reminderSettingsRepository.findById(SETTINGS_ID)
                .orElseGet(() -> reminderSettingsRepository.save(new ReminderSettings()));
    }

    private void checkAndSend(Drive drive, String eventType, LocalDate eventDate, LocalDate targetDate) {
        if (eventDate == null || !eventDate.isEqual(targetDate)) {
            return;
        }
        if (reminderLogRepository.existsByDriveIdAndEventType(drive.getDriveId(), eventType)) {
            return;
        }

        List<Student> eligible = driveEligibilityService.getEligibleStudents(drive.getDriveId());
        List<String> emails = eligible.stream()
                .map(Student::getEmail)
                .filter(e -> e != null && !e.isBlank())
                .toList();

        reminderEmailService.sendReminderEmails(emails, drive, resolveEventLabel(eventType), eventDate);
        recordSent(drive.getDriveId(), eventType);
    }

    private void recordSent(Integer driveId, String eventType) {
        DriveReminderLog entry = reminderLogRepository.findByDriveIdAndEventType(driveId, eventType)
                .orElseGet(() -> {
                    DriveReminderLog l = new DriveReminderLog();
                    l.setDriveId(driveId);
                    l.setEventType(eventType);
                    return l;
                });
        entry.setSentAt(LocalDateTime.now());
        reminderLogRepository.save(entry);
    }

    private static LocalDate resolveEventDate(Drive drive, String eventType) {
        return switch (eventType) {
            case EVENT_PPT              -> drive.getPptDate();
            case EVENT_RESUME_SELECTION -> drive.getResumeSelectionDate();
            case EVENT_EXAM             -> drive.getDriveDate();
            case EVENT_FINAL_SELECTION  -> drive.getFinalSelectionDate();
            default -> throw new IllegalArgumentException("Unknown event type: " + eventType);
        };
    }

    private static String resolveEventLabel(String eventType) {
        return switch (eventType) {
            case EVENT_PPT              -> "Pre-Placement Talk";
            case EVENT_RESUME_SELECTION -> "Resume Selection";
            case EVENT_EXAM             -> "Exam";
            case EVENT_FINAL_SELECTION  -> "Final Selection";
            default -> throw new IllegalArgumentException("Unknown event type: " + eventType);
        };
    }
}
