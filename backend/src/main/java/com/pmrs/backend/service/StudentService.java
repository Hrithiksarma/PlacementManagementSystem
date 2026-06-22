package com.pmrs.backend.service;

import com.pmrs.backend.entity.Student;

import java.util.List;

public interface StudentService {

    List<Student> getAllStudents();

    Student getStudentById(Integer id);

    Student saveStudent(Student student);

    Student updateStudent(Integer id, Student student);

    void deleteStudent(Integer id);

    List<Student> getStudentsByCgpa(Double cgpa);

    List<Student> getFilteredStudents(String branch, String program, Integer batchYear);
}
