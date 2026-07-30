package com.pmrs.backend.service;

import com.pmrs.backend.entity.Student;

import java.util.List;

/**
 * Reverse of {@link StudentPortalService#getEligibleDrives}: given a drive,
 * which students are eligible for it. Used by the Notify Students page and
 * the automatic reminder job.
 */
public interface DriveEligibilityService {

    List<Student> getEligibleStudents(Integer driveId);
}
