package com.pmrs.backend.service;

import com.pmrs.backend.entity.Application;
import com.pmrs.backend.entity.Drive;
import com.pmrs.backend.entity.Student;
import com.pmrs.backend.repository.ApplicationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Service
public class ApplicationStatusEmailServiceImpl implements ApplicationStatusEmailService {

    private static final Logger log = LoggerFactory.getLogger(ApplicationStatusEmailServiceImpl.class);

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private final JavaMailSender        mailSender;
    private final ApplicationRepository applicationRepository;

    @Value("${prms.mail.enabled:true}")
    private boolean mailEnabled;

    @Value("${prms.mail.from-name:PRMS Placement Cell}")
    private String fromName;

    @Value("${prms.mail.from-address:${spring.mail.username:}}")
    private String fromAddress;

    public ApplicationStatusEmailServiceImpl(JavaMailSender mailSender,
                                             ApplicationRepository applicationRepository) {
        this.mailSender            = mailSender;
        this.applicationRepository = applicationRepository;
    }

    @Async
    @Override
    public void sendStatusEmail(Application application, String comment) {
        if (!mailEnabled) {
            log.info("prms.mail.enabled=false — skipping status email for application {}.",
                    application.getApplicationId());
            return;
        }

        Student student = application.getStudent();
        Drive   drive   = application.getDrive();

        if (student == null || student.getEmail() == null || student.getEmail().isBlank()) {
            log.warn("Application {} status email requested but the student has no email — no notification sent.",
                    application.getApplicationId());
            return;
        }

        String studentName = student.getName() != null ? student.getName() : "Student";
        String companyName = drive != null && drive.getCompany() != null
                ? drive.getCompany().getCompanyName() : "the company";
        String role = drive != null && drive.getRoleOffered() != null
                ? drive.getRoleOffered() : "the offered role";
        String date = drive != null && drive.getDriveDate() != null
                ? drive.getDriveDate().format(DATE_FMT) : null;

        String status = application.getStatus();
        String subject;
        String body;

        switch (status) {
            case "First Round" -> {
                subject = "You've advanced to the First Round at " + companyName;
                body = "Dear " + studentName + ",\n\n"
                     + "Good news! You've advanced to the First Round for the role of " + role
                     + " at " + companyName + ".\n\n"
                     + optionalParagraph(comment)
                     + "We'll keep you posted on the next steps.\n\n"
                     + "Regards,\n"
                     + "Placement Cell";
            }
            case "Interview Scheduled" -> {
                subject = "Your interview at " + companyName + " has been scheduled";
                body = "Dear " + studentName + ",\n\n"
                     + "Your interview for the role of " + role + " at " + companyName
                     + (date != null ? " has been scheduled around the placement drive on " + date : " has been scheduled")
                     + ".\n\n"
                     + optionalParagraph(comment)
                     + "All the best for your interview.\n\n"
                     + "Regards,\n"
                     + "Placement Cell";
            }
            case "Selected" -> {
                String pkg = formatPackage(drive != null ? drive.getPackageLpa() : null);
                subject = "Congratulations on your selection at " + companyName + "!";
                body = "Dear " + studentName + ",\n\n"
                     + "Congratulations! You have been selected for the role of " + role
                     + " at " + companyName
                     + (date != null ? " through the placement drive on " + date : "")
                     + ", with a package of " + pkg + ".\n\n"
                     + optionalParagraph(comment)
                     + "Wishing you all the best for your career ahead.\n\n"
                     + "Regards,\n"
                     + "Placement Cell";
            }
            case "Rejected" -> {
                subject = "Update on your application at " + companyName;
                body = "Dear " + studentName + ",\n\n"
                     + "Thank you for participating in the selection process for " + role
                     + " at " + companyName + ".\n\n"
                     + "After careful consideration, we regret to inform you that you have not "
                     + "been selected to proceed further this time.\n\n"
                     + optionalParagraph(comment)
                     + "We encourage you to apply for future opportunities through the placement cell.\n\n"
                     + "Regards,\n"
                     + "Placement Cell";
            }
            default -> {
                log.info("No email template for status '{}' — skipping application {}.",
                        status, application.getApplicationId());
                return;
            }
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            if (fromAddress != null && !fromAddress.isBlank()) {
                message.setFrom(fromName + " <" + fromAddress + ">");
            }
            message.setTo(student.getEmail());
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);

            log.info("Status email ({}) sent to {} for application {}.",
                    status, student.getEmail(), application.getApplicationId());

            application.setLastEmailStatus(status);
            applicationRepository.save(application);
        } catch (Exception e) {
            // Never rethrow — a mail hiccup must not surface as a failed status update.
            log.error("Failed to send status email to {} for application {}.",
                    student.getEmail(), application.getApplicationId(), e);
        }
    }

    private static String optionalParagraph(String comment) {
        return comment != null && !comment.isBlank() ? comment + "\n\n" : "";
    }

    private static String formatPackage(BigDecimal packageLpa) {
        if (packageLpa == null) {
            return "not disclosed";
        }
        return packageLpa.stripTrailingZeros().toPlainString() + " LPA";
    }
}
