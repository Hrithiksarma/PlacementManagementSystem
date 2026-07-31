package com.pmrs.backend.controller;

import com.pmrs.backend.dto.ApplicationEmailRequest;
import com.pmrs.backend.dto.StatusUpdateRequest;
import com.pmrs.backend.entity.Application;
import com.pmrs.backend.service.ApplicationService;
import com.pmrs.backend.service.ApplicationStatusEmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Tag(name = "Application APIs", description = "Operations related to placement applications")
@RestController
@RequestMapping("/applications")
@CrossOrigin("*")
public class ApplicationController {

    private static final Set<String> EMAILABLE_STATUSES =
            Set.of("First Round", "Interview Scheduled", "Selected", "Rejected");

    private final ApplicationService applicationService;
    private final ApplicationStatusEmailService applicationStatusEmailService;

    public ApplicationController(ApplicationService applicationService,
                                 ApplicationStatusEmailService applicationStatusEmailService) {
        this.applicationService = applicationService;
        this.applicationStatusEmailService = applicationStatusEmailService;
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

    @Operation(summary = "Send a stage-appropriate status update email, with an admin comment")
    @PostMapping("/{id}/send-status-email")
    public ResponseEntity<?> sendStatusEmail(@PathVariable Integer id,
                                              @RequestBody(required = false) ApplicationEmailRequest request) {
        Application app = applicationService.getApplicationById(id);
        if (!EMAILABLE_STATUSES.contains(app.getStatus())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "No email available for status: " + app.getStatus()));
        }
        if (app.getStatus().equals(app.getLastEmailStatus())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "An email has already been sent for this status."));
        }
        String comment = request != null ? request.getComment() : null;
        applicationStatusEmailService.sendStatusEmail(app, comment);
        return ResponseEntity.ok(Map.of("message", "Email queued for sending."));
    }

    @Operation(summary = "Delete application by ID")
    @DeleteMapping("/{id}")
    public String deleteApplication(@PathVariable Integer id) {
        applicationService.deleteApplication(id);
        return "Application deleted successfully";
    }
}
