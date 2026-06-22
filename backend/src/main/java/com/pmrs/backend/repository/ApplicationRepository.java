package com.pmrs.backend.repository;

import com.pmrs.backend.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application, Integer> {

    List<Application> findByStudent_StudentId(Integer studentId);

    List<Application> findByDrive_DriveId(Integer driveId);

    List<Application> findByStatus(String status);

    List<Application> findByStudent_StudentIdAndDrive_DriveId(Integer studentId, Integer driveId);

    List<Application> findByStudent_Department_BranchAndStudent_Department_ProgramAndStudent_BatchYear(
            String branch, String program, Integer batchYear);
}
