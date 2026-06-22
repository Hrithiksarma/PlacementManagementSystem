package com.pmrs.backend.service;

import com.pmrs.backend.entity.Application;

import java.util.List;

public interface ApplicationService {

    List<Application> getAllApplications();

    Application getApplicationById(Integer id);

    Application saveApplication(Application application);

    Application updateApplication(Integer id, Application application);

    void deleteApplication(Integer id);

    List<Application> getApplicationsByStudentId(Integer studentId);

    List<Application> getApplicationsByDriveId(Integer driveId);

    List<Application> getApplicationsByStatus(String status);

    List<Application> getApplicationsByCohort(String branch, String program, Integer batchYear);

    Application updateStatus(Integer id, String status);
}
