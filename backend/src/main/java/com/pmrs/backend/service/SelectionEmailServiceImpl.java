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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class SelectionEmailServiceImpl implements SelectionEmailService {

    private static final Logger log = LoggerFactory.getLogger(SelectionEmailServiceImpl.class);

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private final JavaMailSender        mailSender;
    private final ApplicationRepository applicationRepository;

    @Value("${prms.mail.enabled:true}")
    private boolean mailEnabled;

    @Value("${prms.mail.from-name:PRMS Placement Cell}")
    private String fromName;

    @Value("${prms.mail.from-address:${spring.mail.username:}}")
    private String fromAddress;

    public SelectionEmailServiceImpl(JavaMailSender mailSender,
                                     ApplicationRepository applicationRepository) {
        this.mailSender            = mailSender;
        this.applicationRepository = applicationRepository;
    }

    @Async
    @Override
    public void sendSelectionEmail(Application application) {
        if (!mailEnabled) {
            log.info("prms.mail.enabled=false — skipping selection email for application {}.",
                    application.getApplicationId());
            return;
        }

        Student student = application.getStudent();
        Drive   drive   = application.getDrive();

        if (student == null || student.getEmail() == null || student.getEmail().isBlank()) {
            log.warn("Application {} selected but the student has no email — no notification sent.",
                    application.getApplicationId());
            return;
        }

        String studentName = student.getName() != null ? student.getName() : "Student";
        String companyName = drive != null && drive.getCompany() != null
                ? drive.getCompany().getCompanyName() : "the company";
        String role = drive != null && drive.getRoleOffered() != null
                ? drive.getRoleOffered() : "the offered role";
        String pkg  = formatPackage(drive != null ? drive.getPackageLpa() : null);
        String date = drive != null && drive.getDriveDate() != null
                ? drive.getDriveDate().format(DATE_FMT) : null;

        String subject = "Congratulations on your selection at " + companyName + "!";
        String body =
                "Dear " + studentName + ",\n\n"
              + "Congratulations! You have been selected for the role of " + role
              + " at " + companyName
              + (date != null ? " through the placement drive on " + date : "")
              + ", with a package of " + pkg + ".\n\n"
              + "Wishing you all the best for your career ahead.\n\n"
              + "Regards,\n"
              + "Placement Cell";

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            if (fromAddress != null && !fromAddress.isBlank()) {
                message.setFrom(fromName + " <" + fromAddress + ">");
            }
            message.setTo(student.getEmail());
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);

            log.info("Selection email sent to {} for application {}.",
                    student.getEmail(), application.getApplicationId());

            application.setSelectionEmailSent(true);
            applicationRepository.save(application);
        } catch (Exception e) {
            // Never rethrow — a mail hiccup must not surface as a failed status update.
            log.error("Failed to send selection email to {} for application {}.",
                    student.getEmail(), application.getApplicationId(), e);
        }
    }

    private static String formatPackage(BigDecimal packageLpa) {
        if (packageLpa == null) {
            return "not disclosed";
        }
        return packageLpa.stripTrailingZeros().toPlainString() + " LPA";
    }
}
