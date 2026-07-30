package com.pmrs.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class OfficerWelcomeEmailServiceImpl implements OfficerWelcomeEmailService {

    private static final Logger log = LoggerFactory.getLogger(OfficerWelcomeEmailServiceImpl.class);

    private final JavaMailSender mailSender;

    @Value("${prms.mail.enabled:true}")
    private boolean mailEnabled;

    @Value("${prms.mail.from-name:PRMS Placement Cell}")
    private String fromName;

    @Value("${prms.mail.from-address:${spring.mail.username:}}")
    private String fromAddress;

    @Value("${prms.frontend.login-url:http://localhost:5173/login}")
    private String loginUrl;

    public OfficerWelcomeEmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    @Override
    public void sendWelcomeEmail(String toEmail, String username, String temporaryPassword, String comment) {
        if (!mailEnabled) {
            log.info("prms.mail.enabled=false — skipping officer welcome email for {}.", username);
            return;
        }
        if (toEmail == null || toEmail.isBlank()) {
            log.warn("Officer account {} has no email — welcome email not sent.", username);
            return;
        }

        String subject = "Welcome to PRMS — Your Placement Officer Account";
        String body =
                "Dear " + username + ",\n\n"
              + "An account has been created for you on PRMS (Placement Record Management System) "
              + "as a Placement Officer.\n\n"
              + "You can log in using:\n"
              + "    Login page          : " + loginUrl + "\n"
              + "    Username            : " + username + "\n"
              + "    Temporary Password  : " + temporaryPassword + "\n\n"
              + "For security, please change your password immediately after your first login.\n\n"
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
            log.info("Officer welcome email sent to {} ({}).", toEmail, username);
        } catch (Exception e) {
            // Never rethrow — a mail hiccup must not fail the send action.
            log.error("Failed to send officer welcome email to {} ({}).", toEmail, username, e);
        }
    }
}
