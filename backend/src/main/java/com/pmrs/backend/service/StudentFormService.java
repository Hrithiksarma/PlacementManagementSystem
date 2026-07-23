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

    /** Imports every PENDING submission; one bad row never aborts the batch. */
    BulkImportResultDTO importAllPending();
}
