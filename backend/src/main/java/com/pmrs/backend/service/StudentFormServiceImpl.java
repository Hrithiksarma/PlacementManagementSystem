package com.pmrs.backend.service;

import com.pmrs.backend.dto.BulkImportResultDTO;
import com.pmrs.backend.entity.Student;
import com.pmrs.backend.entity.StudentFormSubmission;
import com.pmrs.backend.exception.ResourceNotFoundException;
import com.pmrs.backend.repository.StudentFormSubmissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Promotes staged Google-Form student submissions into real PRMS students.
 *
 * <p>Mirrors {@code DriveFormServiceImpl}: the staging row is the source,
 * "import" is the human approval step, and everything created goes through
 * the normal Students table (+ login account) so the student appears in every
 * existing view. The bulk {@link #importAllPending()} is the "Import All
 * Students" button.
 */
@Service
public class StudentFormServiceImpl implements StudentFormService {

    private final StudentFormSubmissionRepository submissionRepository;
    private final StudentService                  studentService;

    public StudentFormServiceImpl(StudentFormSubmissionRepository submissionRepository,
                                  StudentService                  studentService) {
        this.submissionRepository = submissionRepository;
        this.studentService       = studentService;
    }

    @Override
    public List<StudentFormSubmission> getSubmissionsByStatus(String status) {
        return submissionRepository.findByStatusOrderBySubmissionIdDesc(status);
    }

    @Override
    @Transactional
    public StudentFormSubmission rejectSubmission(Integer submissionId) {
        StudentFormSubmission submission = getSubmission(submissionId);
        if (!StudentFormSubmission.STATUS_PENDING.equals(submission.getStatus())) {
            throw new IllegalArgumentException(
                    "Submission " + submissionId + " is already " + submission.getStatus() + ".");
        }
        submission.setStatus(StudentFormSubmission.STATUS_REJECTED);
        return submissionRepository.save(submission);
    }

    @Override
    @Transactional
    public StudentFormSubmission includeSubmission(Integer submissionId) {
        StudentFormSubmission submission = getSubmission(submissionId);
        if (!StudentFormSubmission.STATUS_PENDING.equals(submission.getStatus())) {
            throw new IllegalArgumentException(
                    "Submission " + submissionId + " is already " + submission.getStatus() + ".");
        }
        Student saved = studentService.importFromSubmission(submission);
        submission.setStatus(StudentFormSubmission.STATUS_INCLUDED);
        submission.setStudentId(saved.getStudentId());
        submission.setFailureReason(null);
        return submissionRepository.save(submission);
    }

    /**
     * Not {@code @Transactional} on purpose: each row is imported in its own
     * transaction (via {@code importFromSubmission}), so one bad row that
     * rolls back can't poison the whole batch or the follow-up status saves.
     */
    @Override
    public BulkImportResultDTO importAllPending() {
        List<StudentFormSubmission> pending =
                submissionRepository.findByStatusOrderBySubmissionIdDesc(
                        StudentFormSubmission.STATUS_PENDING);

        int created = 0;
        int failed = 0;
        List<BulkImportResultDTO.Failure> failures = new ArrayList<>();
        // Catch same-roll-number collisions between rows in THIS batch — the
        // per-row existsByRollNo check only sees already-imported students.
        Set<String> rollNosSeenThisRun = new HashSet<>();

        for (StudentFormSubmission submission : pending) {
            try {
                String rollNo = submission.getRollNo() == null ? "" : submission.getRollNo().trim();
                if (!rollNo.isEmpty() && !rollNosSeenThisRun.add(rollNo)) {
                    throw new IllegalArgumentException(
                        "Roll number " + rollNo + " appears more than once in this batch — only one was imported.");
                }
                Student saved = studentService.importFromSubmission(submission);
                submission.setStatus(StudentFormSubmission.STATUS_INCLUDED);
                submission.setStudentId(saved.getStudentId());
                submission.setFailureReason(null);
                submissionRepository.save(submission);
                created++;
            } catch (Exception e) {
                // Leave the row PENDING so the officer can fix the sheet and retry.
                submission.setFailureReason(e.getMessage());
                submissionRepository.save(submission);
                failures.add(new BulkImportResultDTO.Failure(
                        displayName(submission), e.getMessage()));
                failed++;
            }
        }

        return new BulkImportResultDTO(created, failed, failures);
    }

    private static String displayName(StudentFormSubmission s) {
        if (s.getFullName() != null && !s.getFullName().isBlank()) return s.getFullName();
        if (s.getEmail() != null && !s.getEmail().isBlank()) return s.getEmail();
        return "Submission #" + s.getSubmissionId();
    }

    private StudentFormSubmission getSubmission(Integer submissionId) {
        return submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Student form submission not found with id: " + submissionId));
    }
}
