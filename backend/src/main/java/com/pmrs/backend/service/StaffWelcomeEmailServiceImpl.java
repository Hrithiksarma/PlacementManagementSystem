package com.pmrs.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class StaffWelcomeEmailServiceImpl implements StaffWelcomeEmailService {

    private static final Logger log = LoggerFactory.getLogger(StaffWelcomeEmailServiceImpl.class);

    private final JavaMailSender mailSender;

    @Value("${prms.mail.enabled:true}")
    private boolean mailEnabled;

    @Value("${prms.mail.from-name:PRMS Placement Cell}")
    private String fromName;

    @Value("${prms.mail.from-address:${spring.mail.username:}}")
    private String fromAddress;

    @Value("${prms.frontend.login-url:http://localhost:5173/login}")
    private String loginUrl;

    public StaffWelcomeEmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    @Override
    public void sendWelcomeEmail(String toEmail, String username, String temporaryPassword, String comment) {
        if (!mailEnabled) {
            log.info("prms.mail.enabled=false — skipping staff welcome email for {}.", username);
            return;
        }
        if (toEmail == null || toEmail.isBlank()) {
            log.warn("Staff account {} has no email — welcome email not sent.", username);
            return;
        }

        String subject = "PRMS — Department Staff Portal Access (Read-Only)";
        String body =
                "Hello,\n\n"
              + "This is the shared, read-only Department Staff Portal login for PRMS "
              + "(Placement Record Management System). Please forward these details to "
              + "the relevant staff in your department — they are shared across all "
              + "departments with dashboard access.\n\n"
              + "You can log in using:\n"
              + "    Login page          : " + loginUrl + "\n"
              + "    Portal              : Department Staff\n"
              + "    Username            : " + username + "\n"
              + "    Temporary Password  : " + temporaryPassword + "\n\n"
              + "The first person to log in will be prompted to set a new shared password — "
              + "please coordinate within your department before doing so.\n\n"
              + (comment != null && !comment.isBlank()
                    ? "Note from the admin:\n" + comment + "\n\n"
                    : "")
              + "Regards,\n"
              + "Placement Cell";

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            if (fromAddress != null && !fromAddress.isBlank()) {
                message.setFrom(fromName + " <" + fromAddress + ">");
            }
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Staff welcome email sent to {} ({}).", toEmail, username);
        } catch (Exception e) {
            // Never rethrow — a mail hiccup must not fail the send action.
            log.error("Failed to send staff welcome email to {} ({}).", toEmail, username, e);
        }
    }
}
