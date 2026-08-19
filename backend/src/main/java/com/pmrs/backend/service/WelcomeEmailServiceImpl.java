package com.pmrs.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class WelcomeEmailServiceImpl implements WelcomeEmailService {

    private static final Logger log = LoggerFactory.getLogger(WelcomeEmailServiceImpl.class);

    private final JavaMailSender mailSender;

    @Value("${prms.mail.enabled:true}")
    private boolean mailEnabled;

    @Value("${prms.mail.from-name:PRMS Placement Cell}")
    private String fromName;

    @Value("${prms.mail.from-address:${spring.mail.username:}}")
    private String fromAddress;

    @Value("${prms.frontend.login-url:http://localhost:5173/login}")
    private String loginUrl;

    public WelcomeEmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    @Override
    public void sendWelcomeEmail(String toEmail, String studentName, String rollNo, String temporaryPassword) {
        if (!mailEnabled) {
            log.info("prms.mail.enabled=false — skipping welcome email for roll {}.", rollNo);
            return;
        }
        if (toEmail == null || toEmail.isBlank()) {
            log.warn("Account created for roll {} with no email — welcome email not sent.", rollNo);
            return;
        }

        String name = studentName != null && !studentName.isBlank() ? studentName : "Student";

        String subject = "Welcome to PRMS — Your Account Has Been Created";
        String body =
                "Dear " + name + ",\n\n"
              + "Welcome to PRMS (Placement Record Management System)! Your account has been "
              + "successfully created.\n\n"
              + "You can log in using:\n"
              + "    Login page          : " + loginUrl + "\n"
              + "    Username / Roll No   : " + rollNo + "\n"
              + "    Temporary Password   : " + temporaryPassword + "\n\n"
              + "You will be required to set a new password the first time you log in.\n\n"
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
            log.info("Welcome email sent to {} (roll {}).", toEmail, rollNo);
        } catch (Exception e) {
            // Never rethrow — a mail hiccup must not fail account creation / import.
            log.error("Failed to send welcome email to {} (roll {}).", toEmail, rollNo, e);
        }
    }
}
