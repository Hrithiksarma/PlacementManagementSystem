package com.pmrs.backend.service;

import com.pmrs.backend.dto.BackfillResultDTO;
import com.pmrs.backend.entity.Student;
import com.pmrs.backend.entity.StudentFormSubmission;

import java.util.List;

public interface StudentService {

    List<Student> getAllStudents();

    Student getStudentById(Integer id);

    Student saveStudent(Student student);

    Student updateStudent(Integer id, Student student);

    void deleteStudent(Integer id);

    List<Student> getStudentsByCgpa(Double cgpa);

    List<Student> getFilteredStudents(String branch, String program, Integer batchYear);

    /**
     * Promotes a staged Google-Form student submission into a real Student
     * (roll number auto-assigned, login account created). Does not change the
     * submission's status — the caller owns that transition.
     */
    Student importFromSubmission(StudentFormSubmission submission);

    BackfillResultDTO backfillStudentAccounts();
}
