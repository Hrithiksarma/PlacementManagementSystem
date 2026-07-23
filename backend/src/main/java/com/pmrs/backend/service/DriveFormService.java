package com.pmrs.backend.service;

import com.pmrs.backend.entity.Company;
import com.pmrs.backend.entity.DriveFormSubmission;

import java.util.List;

public interface DriveFormService {

    List<DriveFormSubmission> getSubmissionsByStatus(String status);

    /** Companies already in PRMS that have no form submission yet. */
    List<Company> getCompaniesWithoutSubmission();

    DriveFormSubmission includeSubmission(Integer submissionId);

    DriveFormSubmission rejectSubmission(Integer submissionId);
}
