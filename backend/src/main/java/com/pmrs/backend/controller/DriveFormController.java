package com.pmrs.backend.controller;

import com.pmrs.backend.entity.Company;
import com.pmrs.backend.entity.DriveFormSubmission;
import com.pmrs.backend.service.DriveFormService;
import com.pmrs.backend.service.GoogleFormSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Drive Form APIs", description = "Google Form drive submissions — review and include")
@RestController
@RequestMapping("/form-submissions")
@CrossOrigin("*")
public class DriveFormController {

    private final DriveFormService      driveFormService;
    private final GoogleFormSyncService googleFormSyncService;

    public DriveFormController(DriveFormService      driveFormService,
                               GoogleFormSyncService googleFormSyncService) {
        this.driveFormService      = driveFormService;
        this.googleFormSyncService = googleFormSyncService;
    }

    @Operation(summary = "List form submissions by status (PENDING, INCLUDED, REJECTED)")
    @GetMapping
    public List<DriveFormSubmission> getSubmissions(
            @RequestParam(defaultValue = "PENDING") String status) {
        return driveFormService.getSubmissionsByStatus(status.toUpperCase());
    }

    @Operation(summary = "Companies in PRMS that haven't submitted the form yet")
    @GetMapping("/not-submitted")
    public List<Company> getCompaniesWithoutSubmission() {
        return driveFormService.getCompaniesWithoutSubmission();
    }

    @Operation(summary = "Pull new responses from the Google Form's linked sheet now")
    @PostMapping("/sync")
    public ResponseEntity<?> sync() {
        try {
            int created = googleFormSyncService.syncResponses();
            return ResponseEntity.ok(Map.of("newSubmissions", created));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "Google Form sync failed: " + e.getMessage()));
        }
    }

    @Operation(summary = "Include a submission — creates the company (if new), HR contact and drive")
    @PostMapping("/{id}/include")
    public DriveFormSubmission include(@PathVariable Integer id) {
        return driveFormService.includeSubmission(id);
    }

    @Operation(summary = "Reject a pending submission")
    @PostMapping("/{id}/reject")
    public DriveFormSubmission reject(@PathVariable Integer id) {
        return driveFormService.rejectSubmission(id);
    }
}
