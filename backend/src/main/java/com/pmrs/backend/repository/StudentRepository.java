package com.pmrs.backend.repository;

import com.pmrs.backend.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Integer> {

    List<Student> findByCgpaGreaterThanEqual(Double cgpa);

    List<Student> findByDepartment_BranchAndDepartment_ProgramAndBatchYear(
            String branch,
            String program,
            Integer batchYear
    );

    Optional<Student> findByEmail(String email);

    Optional<Student> findByPhone(String phone);

    Optional<Student> findByRollNo(String rollNo);

    boolean existsByRollNo(String rollNo);
}