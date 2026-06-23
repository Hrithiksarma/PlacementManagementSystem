package com.pmrs.backend.service;

import com.pmrs.backend.entity.Drive;

import java.util.List;

public interface DriveService {

    List<Drive> getAllDrives();

    Drive getDriveById(Integer id);

    Drive saveDrive(Drive drive);

    Drive updateDrive(Integer id, Drive drive);

    void deleteDrive(Integer id);

    List<Drive> getDrivesByStatus(String status);

    List<Drive> getDrivesByCompanyId(Integer companyId);
}
