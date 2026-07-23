package com.pmrs.backend.repository;

import com.pmrs.backend.entity.DriveFormSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DriveFormSubmissionRepository extends JpaRepository<DriveFormSubmission, Integer> {

    List<DriveFormSubmission> findByStatusOrderBySubmissionIdDesc(String status);

    boolean existsByResponseKey(String responseKey);

    Optional<DriveFormSubmission> findByResponseKey(String responseKey);
}
