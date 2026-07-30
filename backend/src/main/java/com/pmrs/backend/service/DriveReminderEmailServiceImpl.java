package com.pmrs.backend.service;

import com.pmrs.backend.entity.Drive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class DriveReminderEmailServiceImpl implements DriveReminderEmailService {

    private static final Logger log = LoggerFactory.getLogger(DriveReminderEmailServiceImpl.class);

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private final JavaMailSender mailSender;

    @Value("${prms.mail.enabled:true}")
    private boolean mailEnabled;

    @Value("${prms.mail.from-name:PRMS Placement Cell}")
    private String fromName;

    @Value("${prms.mail.from-address:${spring.mail.username:}}")
    private String fromAddress;

    public DriveReminderEmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    @Override
    public void sendReminderEmails(List<String> toEmails, Drive drive, String eventLabel, LocalDate eventDate) {
        if (!mailEnabled) {
            log.info("prms.mail.enabled=false — skipping {} reminder emails for drive {}.",
                    eventLabel, drive.getDriveId());
            return;
        }
        if (toEmails == null || toEmails.isEmpty()) {
            log.warn("No recipient emails for {} reminder on drive {}.", eventLabel, drive.getDriveId());
            return;
        }

        String companyName = drive.getCompany() != null ? drive.getCompany().getCompanyName() : "the company";
        String role = drive.getRoleOffered() != null ? drive.getRoleOffered() : "the offered role";
        String dateStr = eventDate != null ? eventDate.format(DATE_FMT) : "TBA";

        String subject = companyName + " — " + eventLabel + " reminder";
        String body =
                "Dear Student,\n\n"
              + "This is a reminder that the " + eventLabel + " for " + companyName
              + " (" + role + ") is scheduled on " + dateStr + ".\n\n"
              + "Please check PRMS for full details and make sure you're prepared.\n\n"
              + "Regards,\n"
              + "Placement Cell";

        int sent = 0;
        for (String toEmail : toEmails) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                if (fromAddress != null && !fromAddress.isBlank()) {
                    message.setFrom(fromName + " <" + fromAddress + ">");
                }
                message.setTo(toEmail);
                message.setSubject(subject);
                message.setText(body);
                mailSender.send(message);
                sent++;
            } catch (Exception e) {
                // Never rethrow — one bad address must not stop the rest of the batch.
                log.error("Failed to send {} reminder email to {}.", eventLabel, toEmail, e);
            }
        }
        log.info("{} reminder emails sent for drive {} ({}/{}).",
                eventLabel, drive.getDriveId(), sent, toEmails.size());
    }
}
