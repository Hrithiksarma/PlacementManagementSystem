package com.academic.service.controller;

import com.academic.service.dto.StudentRequestDTO;
import com.academic.service.dto.StudentResponseDTO;
import com.academic.service.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @PostMapping
    public ResponseEntity<StudentResponseDTO> create(@Valid @RequestBody StudentRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(studentService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<StudentResponseDTO>> findAll() {
        return ResponseEntity.ok(studentService.findAll());
    }

    @GetMapping("/{rollNo}")
    public ResponseEntity<StudentResponseDTO> findByRollNo(@PathVariable String rollNo) {
        return ResponseEntity.ok(studentService.findByRollNo(rollNo));
    }

    @PutMapping("/{rollNo}")
    public ResponseEntity<StudentResponseDTO> update(
            @PathVariable String rollNo,
            @Valid @RequestBody StudentRequestDTO request) {
        return ResponseEntity.ok(studentService.update(rollNo, request));
    }

    @DeleteMapping("/{rollNo}")
    public ResponseEntity<Void> delete(@PathVariable String rollNo) {
        studentService.delete(rollNo);
        return ResponseEntity.noContent().build();
    }
}
