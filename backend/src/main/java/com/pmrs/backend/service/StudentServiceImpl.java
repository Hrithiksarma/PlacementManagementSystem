package com.pmrs.backend.service;

import com.pmrs.backend.entity.Student;
import com.pmrs.backend.exception.ResourceNotFoundException;
import com.pmrs.backend.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;

    public StudentServiceImpl(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Override
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    @Override
    public Student getStudentById(Integer id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));
    }

    @Override
    public Student saveStudent(Student student) {
        Student saved = studentRepository.save(student);
        return studentRepository.findById(saved.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
    }

    @Override
    public Student updateStudent(Integer id, Student updated) {
        Student existing = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));
        existing.setName(updated.getName());
        existing.setDepartment(updated.getDepartment());
        existing.setBatchYear(updated.getBatchYear());
        existing.setCgpa(updated.getCgpa());
        existing.setEmail(updated.getEmail());
        existing.setActiveBacklogs(updated.getActiveBacklogs());
        existing.setPhone(updated.getPhone());
        existing.setPlacementTier(updated.getPlacementTier());
        return studentRepository.save(existing);
    }

    @Override
    public void deleteStudent(Integer id) {
        studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));
        studentRepository.deleteById(id);
    }

    @Override
    public List<Student> getStudentsByCgpa(Double cgpa) {
        return studentRepository.findByCgpaGreaterThanEqual(cgpa);
    }

    @Override
    public List<Student> getFilteredStudents(String branch, String program, Integer batchYear) {
        return studentRepository.findByDepartment_BranchAndDepartment_ProgramAndBatchYear(
                branch, program, batchYear
        );
    }
}
