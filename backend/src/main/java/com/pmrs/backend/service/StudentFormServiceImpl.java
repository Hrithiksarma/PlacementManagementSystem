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
    private final FlagEmailService                flagEmailService;

    public StudentFormServiceImpl(StudentFormSubmissionRepository submissionRepository,
                                  StudentService                  studentService,
                                  FlagEmailService                flagEmailService) {
        this.submissionRepository = submissionRepository;
        this.studentService       = studentService;
        this.flagEmailService     = flagEmailService;
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

    @Override
    @Transactional
    public StudentFormSubmission flagSubmission(Integer submissionId, String comment) {
        StudentFormSubmission submission = getSubmission(submissionId);
        if (!StudentFormSubmission.STATUS_PENDING.equals(submission.getStatus())) {
            throw new IllegalArgumentException(
                    "Submission " + submissionId + " is already " + submission.getStatus() + ".");
        }
        if (submission.isFlagged()) {
            throw new IllegalArgumentException(
                    "Submission " + submissionId + " is already flagged.");
        }
        submission.setFlagged(true);
        submission.setFlagComment(comment);
        submission.setFlaggedAt(java.time.LocalDateTime.now());
        StudentFormSubmission saved = submissionRepository.save(submission);
        // Fired after the save so a mail hiccup can't roll back the flag itself.
        flagEmailService.sendFlagEmail(saved.getEmail(), saved.getFullName(), comment);
        return saved;
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
        return importBatch(pending);
    }

    /**
     * Imports only the given submission IDs. Not {@code @Transactional} for
     * the same reason as {@link #importAllPending()}.
     */
    @Override
    public BulkImportResultDTO importSelected(List<Integer> submissionIds) {
        List<StudentFormSubmission> toImport = new ArrayList<>();
        List<BulkImportResultDTO.Failure> upfrontFailures = new ArrayList<>();

        for (Integer id : submissionIds) {
            StudentFormSubmission submission = submissionRepository.findById(id).orElse(null);
            if (submission == null) {
                upfrontFailures.add(new BulkImportResultDTO.Failure(
                        "Submission #" + id, "Submission not found."));
            } else if (!StudentFormSubmission.STATUS_PENDING.equals(submission.getStatus())) {
                upfrontFailures.add(new BulkImportResultDTO.Failure(
                        displayName(submission), "Already " + submission.getStatus() + "."));
            } else {
                toImport.add(submission);
            }
        }

        BulkImportResultDTO result = importBatch(toImport);
        if (upfrontFailures.isEmpty()) {
            return result;
        }
        List<BulkImportResultDTO.Failure> combinedFailures = new ArrayList<>(upfrontFailures);
        combinedFailures.addAll(result.getFailures());
        return new BulkImportResultDTO(
                result.getCreated(), result.getFailed() + upfrontFailures.size(), combinedFailures);
    }

    /**
     * Shared per-row import loop used by both {@link #importAllPending()} and
     * {@link #importSelected(List)}. One failing row is recorded and skipped
     * rather than aborting the rest of the batch.
     */
    private BulkImportResultDTO importBatch(List<StudentFormSubmission> submissions) {
        int created = 0;
        int failed = 0;
        List<BulkImportResultDTO.Failure> failures = new ArrayList<>();
        // Catch same-roll-number collisions between rows in THIS batch — the
        // per-row existsByRollNo check only sees already-imported students.
        Set<String> rollNosSeenThisRun = new HashSet<>();

        for (StudentFormSubmission submission : submissions) {
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
