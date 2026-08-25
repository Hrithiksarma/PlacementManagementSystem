package com.pmrs.backend.service;

import com.pmrs.backend.entity.Drive;

import java.util.List;

public interface DriveService {

    List<Drive> getAllDrives();

    Drive getDriveById(Integer id);

    Drive saveDrive(Drive drive);

    Drive updateDrive(Integer id, Drive drive);

    Drive updateStatus(Integer id, String status);

    void deleteDrive(Integer id);

    List<Drive> getDrivesByStatus(String status);

    List<Drive> getDrivesByCompanyId(Integer companyId);

    /** Sets/updates a drive's JD URL and (re-)extracts its text from it. */
    Drive setJdUrl(Integer id, String jdUrl);

    /** Re-runs JD text extraction from the drive's already-stored jdUrl. */
    Drive extractJd(Integer id);
}
