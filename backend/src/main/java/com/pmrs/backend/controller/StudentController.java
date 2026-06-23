package com.pmrs.backend.controller;

import com.pmrs.backend.entity.Student;
import com.pmrs.backend.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Student APIs", description = "Operations related to students")
@RestController
@RequestMapping("/students")
@CrossOrigin("*")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @Operation(summary = "Get all students")
    @GetMapping
    public List<Student> getAllStudents() {
        return studentService.getAllStudents();
    }

    @Operation(summary = "Get student by ID")
    @GetMapping("/{id}")
    public Student getStudentById(@PathVariable Integer id) {
        return studentService.getStudentById(id);
    }

    @Operation(summary = "Create a new student")
    @PostMapping
    public Student createStudent(@Valid @RequestBody Student student) {
        return studentService.saveStudent(student);
    }

    @Operation(summary = "Update student by ID")
    @PutMapping("/{id}")
    public Student updateStudent(@PathVariable Integer id,
                                 @Valid @RequestBody Student student) {
        return studentService.updateStudent(id, student);
    }

    @Operation(summary = "Delete student by ID")
    @DeleteMapping("/{id}")
    public String deleteStudent(@PathVariable Integer id) {
        studentService.deleteStudent(id);
        return "Student deleted successfully";
    }

    @Operation(summary = "Get students with CGPA greater than or equal to value")
    @GetMapping("/cgpa/{cgpa}")
    public List<Student> getStudentsByCgpa(@PathVariable Double cgpa) {
        return studentService.getStudentsByCgpa(cgpa);
    }

    @Operation(summary = "Filter students by department, program, and batch year")
    @GetMapping("/filter")
    public List<Student> getFilteredStudents(
            @RequestParam String department,
            @RequestParam String program,
            @RequestParam Integer batchYear
    ) {
        return studentService.getFilteredStudents(department, program, batchYear);
    }
}
