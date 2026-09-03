package com.pmrs.backend.repository;

import com.pmrs.backend.entity.ResumeJdMatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ResumeJdMatchRepository extends JpaRepository<ResumeJdMatch, Integer> {

    Optional<ResumeJdMatch> findTopByStudent_StudentIdAndDrive_DriveIdOrderByCreatedAtDesc(
            Integer studentId, Integer driveId);

    List<ResumeJdMatch> findByDrive_DriveId(Integer driveId);
}
