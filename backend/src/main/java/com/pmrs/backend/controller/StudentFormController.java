package com.pmrs.backend.controller;

import com.pmrs.backend.dto.BulkImportResultDTO;
import com.pmrs.backend.entity.StudentFormSubmission;
import com.pmrs.backend.service.StudentFormService;
import com.pmrs.backend.service.StudentFormSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Student Form APIs", description = "Google Form student registrations — review and import")
@RestController
@RequestMapping("/student-form-submissions")
@CrossOrigin("*")
public class StudentFormController {

    private final StudentFormService     studentFormService;
    private final StudentFormSyncService studentFormSyncService;

    public StudentFormController(StudentFormService     studentFormService,
                                 StudentFormSyncService studentFormSyncService) {
        this.studentFormService     = studentFormService;
        this.studentFormSyncService = studentFormSyncService;
    }

    @Operation(summary = "List student submissions by status (PENDING, INCLUDED, REJECTED)")
    @GetMapping
    public List<StudentFormSubmission> getSubmissions(
            @RequestParam(defaultValue = "PENDING") String status) {
        return studentFormService.getSubmissionsByStatus(status.toUpperCase());
    }

    @Operation(summary = "Pull new responses from the student Google Form's linked sheet now")
    @PostMapping("/sync")
    public ResponseEntity<?> sync() {
        try {
            int created = studentFormSyncService.syncResponses();
            return ResponseEntity.ok(Map.of("newSubmissions", created));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "Student form sync failed: " + e.getMessage()));
        }
    }

    @Operation(summary = "Import all pending submissions into PRMS as students")
    @PostMapping("/import-all")
    public BulkImportResultDTO importAll() {
        return studentFormService.importAllPending();
    }

    @Operation(summary = "Import a single pending submission into PRMS")
    @PostMapping("/{id}/include")
    public StudentFormSubmission include(@PathVariable Integer id) {
        return studentFormService.includeSubmission(id);
    }

    @Operation(summary = "Reject a pending submission")
    @PostMapping("/{id}/reject")
    public StudentFormSubmission reject(@PathVariable Integer id) {
        return studentFormService.rejectSubmission(id);
    }
}
