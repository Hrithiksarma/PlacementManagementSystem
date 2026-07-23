package com.pmrs.backend.controller;

import com.pmrs.backend.dto.StatusUpdateRequest;
import com.pmrs.backend.entity.Application;
import com.pmrs.backend.service.ApplicationService;
import com.pmrs.backend.service.SelectionEmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Application APIs", description = "Operations related to placement applications")
@RestController
@RequestMapping("/applications")
@CrossOrigin("*")
public class ApplicationController {

    private final ApplicationService applicationService;
    private final SelectionEmailService selectionEmailService;

    public ApplicationController(ApplicationService applicationService,
                                 SelectionEmailService selectionEmailService) {
        this.applicationService = applicationService;
        this.selectionEmailService = selectionEmailService;
    }

    @Operation(summary = "Get all applications")
    @GetMapping
    public List<Application> getAllApplications() {
        return applicationService.getAllApplications();
    }

    @Operation(summary = "Get application by ID")
    @GetMapping("/{id}")
    public Application getApplicationById(@PathVariable Integer id) {
        return applicationService.getApplicationById(id);
    }

    @Operation(summary = "Get applications by student ID")
    @GetMapping("/student/{studentId}")
    public List<Application> getApplicationsByStudent(@PathVariable Integer studentId) {
        return applicationService.getApplicationsByStudentId(studentId);
    }

    @Operation(summary = "Get applications by drive ID")
    @GetMapping("/drive/{driveId}")
    public List<Application> getApplicationsByDrive(@PathVariable Integer driveId) {
        return applicationService.getApplicationsByDriveId(driveId);
    }

    @Operation(summary = "Get applications by status")
    @GetMapping("/status/{status}")
    public List<Application> getApplicationsByStatus(@PathVariable String status) {
        return applicationService.getApplicationsByStatus(status);
    }

    @Operation(summary = "Filter applications by student cohort (branch + program + batch year)")
    @GetMapping("/filter")
    public List<Application> getApplicationsByCohort(
            @RequestParam String branch,
            @RequestParam String program,
            @RequestParam Integer batchYear) {
        return applicationService.getApplicationsByCohort(branch, program, batchYear);
    }

    @Operation(summary = "Update application status by ID")
    @PutMapping("/{id}")
    public Application updateApplication(@PathVariable Integer id,
                                         @RequestBody Application application) {
        return applicationService.updateApplication(id, application);
    }

    @Operation(summary = "Update only the status of an application")
    @PutMapping("/{id}/status")
    public ResponseEntity<Application> updateApplicationStatus(
            @PathVariable Integer id,
            @RequestBody StatusUpdateRequest req) {
        if (req.getStatus() == null || req.getStatus().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(applicationService.updateStatus(id, req.getStatus()));
    }

    @Operation(summary = "Resend the selection congratulations email")
    @PostMapping("/{id}/resend-selection-email")
    public ResponseEntity<?> resendSelectionEmail(@PathVariable Integer id) {
        Application app = applicationService.getApplicationById(id);
        if (!"Selected".equals(app.getStatus())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Application is not Selected."));
        }
        selectionEmailService.sendSelectionEmail(app);
        return ResponseEntity.ok(Map.of("message", "Email queued for resend."));
    }

    @Operation(summary = "Delete application by ID")
    @DeleteMapping("/{id}")
    public String deleteApplication(@PathVariable Integer id) {
        applicationService.deleteApplication(id);
        return "Application deleted successfully";
    }
}
