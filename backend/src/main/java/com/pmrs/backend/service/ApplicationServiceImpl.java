package com.pmrs.backend.service;

import com.pmrs.backend.entity.Application;
import com.pmrs.backend.entity.Drive;
import com.pmrs.backend.entity.Student;
import com.pmrs.backend.exception.EligibilityException;
import com.pmrs.backend.exception.ResourceNotFoundException;
import com.pmrs.backend.repository.ApplicationRepository;
import com.pmrs.backend.repository.DriveRepository;
import com.pmrs.backend.repository.StudentRepository;
import com.pmrs.backend.util.ApplicationStatusValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final StudentRepository studentRepository;
    private final DriveRepository driveRepository;

    public ApplicationServiceImpl(ApplicationRepository applicationRepository,
                                  StudentRepository studentRepository,
                                  DriveRepository driveRepository) {
        this.applicationRepository = applicationRepository;
        this.studentRepository = studentRepository;
        this.driveRepository = driveRepository;
    }

    @Override
    public List<Application> getAllApplications() {
        return applicationRepository.findAll();
    }

    @Override
    public Application getApplicationById(Integer id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + id));
    }

    @Override
    public Application saveApplication(Application application) {
        Student student = studentRepository.findById(application.getStudent().getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        Drive drive = driveRepository.findById(application.getDrive().getDriveId())
                .orElseThrow(() -> new ResourceNotFoundException("Drive not found"));

        List<String> reasons = new ArrayList<>();
        if (drive.getMinCgpa() != null && student.getCgpa() != null
                && student.getCgpa() < drive.getMinCgpa()) {
            reasons.add("CGPA " + student.getCgpa() + " is below required " + drive.getMinCgpa());
        }
        if (drive.getMaxBacklogs() != null && student.getActiveBacklogs() != null
                && student.getActiveBacklogs() > drive.getMaxBacklogs()) {
            reasons.add("Active backlogs " + student.getActiveBacklogs()
                    + " exceeds allowed " + drive.getMaxBacklogs());
        }
        if (!reasons.isEmpty()) {
            throw new EligibilityException(
                    "Student does not meet eligibility criteria: " + String.join("; ", reasons));
        }

        application.setApplicationDate(LocalDate.now());
        application.setUpdatedAt(LocalDate.now());
        Application saved = applicationRepository.save(application);
        return applicationRepository.findById(saved.getApplicationId())
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));
    }

    @Override
    public Application updateApplication(Integer id, Application updated) {
        Application existing = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + id));
        existing.setStatus(updated.getStatus());
        existing.setRound(updated.getRound());
        existing.setUpdatedAt(LocalDate.now());
        return applicationRepository.save(existing);
    }

    @Override
    public void deleteApplication(Integer id) {
        applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + id));
        applicationRepository.deleteById(id);
    }

    @Override
    public List<Application> getApplicationsByStudentId(Integer studentId) {
        return applicationRepository.findByStudent_StudentId(studentId);
    }

    @Override
    public List<Application> getApplicationsByDriveId(Integer driveId) {
        return applicationRepository.findByDrive_DriveId(driveId);
    }

    @Override
    public List<Application> getApplicationsByStatus(String status) {
        return applicationRepository.findByStatus(status);
    }

    @Override
    public List<Application> getApplicationsByCohort(String branch, String program, Integer batchYear) {
        return applicationRepository
                .findByStudent_Department_BranchAndStudent_Department_ProgramAndStudent_BatchYear(
                        branch, program, batchYear);
    }

    @Override
    @Transactional
    public Application updateStatus(Integer id, String newStatus) {
        Application app = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + id));

        // Old records written before the status column existed have status = NULL.
        // The result column on those rows holds 'Pending' / 'Selected' / 'Rejected'.
        // Map: Pending → Applied, keep Selected / Rejected as-is.
        String rawStatus = app.getStatus();
        String current;
        if (rawStatus == null || rawStatus.isBlank() || "Pending".equalsIgnoreCase(rawStatus)) {
            current = "Applied";
        } else {
            current = rawStatus;
        }

        if (ApplicationStatusValidator.isFinalStatus(current)) {
            throw new IllegalStateException(
                    "Cannot update a " + current + " application — this is a final status.");
        }
        if (!ApplicationStatusValidator.isValidTransition(current, newStatus)) {
            throw new IllegalStateException(
                    "Invalid transition: " + current + " → " + newStatus
                    + ". Allowed: " + ApplicationStatusValidator.allowedTransitions(current));
        }

        // Offer decisions belong to the student, not to admin status updates.
        if ("Offer Accepted".equals(newStatus) || "Offer Rejected".equals(newStatus)) {
            throw new IllegalStateException(
                    "Offer decisions must be made by the student via the student portal.");
        }

        app.setStatus(newStatus);
        app.setUpdatedAt(LocalDate.now());
        boolean justSelected = "Selected".equals(newStatus);
        if (justSelected) {
            // "Selected" IS the job offer (no separate Offer Released stage) —
            // stamp both, so downstream offer-deadline logic still works.
            app.setSelectedAt(LocalDateTime.now());
            app.setOfferReleasedAt(LocalDateTime.now());
        }
        applicationRepository.save(app);

        return applicationRepository.findById(app.getApplicationId()).orElse(app);
    }
}
