package com.pmrs.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class FlagEmailServiceImpl implements FlagEmailService {

    private static final Logger log = LoggerFactory.getLogger(FlagEmailServiceImpl.class);

    private final JavaMailSender mailSender;

    @Value("${prms.mail.enabled:true}")
    private boolean mailEnabled;

    @Value("${prms.mail.from-name:PRMS Placement Cell}")
    private String fromName;

    @Value("${prms.mail.from-address:${spring.mail.username:}}")
    private String fromAddress;

    public FlagEmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    @Override
    public void sendFlagEmail(String toEmail, String studentName, String comment) {
        if (!mailEnabled) {
            log.info("prms.mail.enabled=false — skipping flag email to {}.", toEmail);
            return;
        }
        if (toEmail == null || toEmail.isBlank()) {
            log.warn("Submission flagged but no email on file — no notification sent.");
            return;
        }

        String name = (studentName != null && !studentName.isBlank()) ? studentName : "Student";
        String subject = "Action needed: your PRMS registration needs a correction";
        String body =
                "Dear " + name + ",\n\n"
              + "The placement cell reviewed your registration form and found something "
              + "that needs to be corrected before it can be processed:\n\n"
              + comment + "\n\n"
              + "Please resubmit the registration Google Form with the correction. "
              + "Once resubmitted, your registration will be reviewed again.\n\n"
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

            log.info("Flag email sent to {}.", toEmail);
        } catch (Exception e) {
            // Never rethrow — a mail hiccup must not surface as a failed flag action.
            log.error("Failed to send flag email to {}.", toEmail, e);
        }
    }
}
