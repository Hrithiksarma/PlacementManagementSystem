package com.pmrs.backend.service;

import com.pmrs.backend.dto.BulkImportResultDTO;
import com.pmrs.backend.entity.StudentFormSubmission;

import java.util.List;

/**
 * Review and promotion of staged Google-Form student submissions into real
 * PRMS students. Mirrors {@code DriveFormService} but batches the promotion
 * step ("Import All Students").
 */
public interface StudentFormService {

    List<StudentFormSubmission> getSubmissionsByStatus(String status);

    StudentFormSubmission rejectSubmission(Integer submissionId);

    /** Imports one pending submission and flips it to INCLUDED. */
    StudentFormSubmission includeSubmission(Integer submissionId);

    /**
     * Flags a pending submission for correction: records the officer's
     * comment and emails the student. The row stays PENDING so it remains
     * actionable until the student resubmits.
     */
    StudentFormSubmission flagSubmission(Integer submissionId, String comment);

    /** Imports every PENDING submission; one bad row never aborts the batch. */
    BulkImportResultDTO importAllPending();

    /**
     * Imports only the given submission IDs. IDs that don't exist or aren't
     * PENDING are recorded as failures rather than thrown, same as a bad row
     * in {@link #importAllPending()}.
     */
    BulkImportResultDTO importSelected(List<Integer> submissionIds);
}
