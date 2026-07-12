package com.pmrs.backend.controller;

import com.pmrs.backend.dto.BackfillResultDTO;
import com.pmrs.backend.entity.Student;
import com.pmrs.backend.service.AcademicErpClient;
import com.pmrs.backend.service.StudentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    private final StudentService    studentService;
    private final AcademicErpClient academicErpClient;

    public StudentController(StudentService    studentService,
                             AcademicErpClient academicErpClient) {
        this.studentService    = studentService;
        this.academicErpClient = academicErpClient;
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

    // Preview endpoint — proxies the Academic ERP lookup so the
    // frontend never calls Academic ERP directly.
    @GetMapping("/preview/{rollNo}")
    public ResponseEntity<?> preview(@PathVariable String rollNo) {
        // Delegates straight to Academic ERP client — no PRMS DB write.
        return ResponseEntity.ok(academicErpClient.fetchByRollNo(rollNo));
    }

    // Import endpoint — fetches from Academic ERP, maps, saves to PRMS.
    @PostMapping("/import/{rollNo}")
    public ResponseEntity<Student> importFromAcademicErp(@PathVariable String rollNo) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(studentService.importStudent(rollNo));
    }

    // Admin-triggered bulk backfill — creates login accounts (username/temp
    // password = roll number) for any existing student that doesn't have one yet.
    @Operation(summary = "Backfill login accounts for students that don't have one")
    @PostMapping("/backfill-accounts")
    public ResponseEntity<BackfillResultDTO> backfillAccounts() {
        return ResponseEntity.ok(studentService.backfillStudentAccounts());
    }
}
