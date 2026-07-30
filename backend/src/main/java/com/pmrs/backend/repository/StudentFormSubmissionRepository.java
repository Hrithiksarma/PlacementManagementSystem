package com.pmrs.backend.repository;

import com.pmrs.backend.entity.StudentFormSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentFormSubmissionRepository extends JpaRepository<StudentFormSubmission, Integer> {

    List<StudentFormSubmission> findByStatusOrderBySubmissionIdDesc(String status);

    boolean existsByResponseKey(String responseKey);

    Optional<StudentFormSubmission> findFirstByRollNoAndFlaggedTrueAndStatusOrderBySubmissionIdDesc(
            String rollNo, String status);

    Optional<StudentFormSubmission> findFirstByEmailAndFlaggedTrueAndStatusOrderBySubmissionIdDesc(
            String email, String status);
}
