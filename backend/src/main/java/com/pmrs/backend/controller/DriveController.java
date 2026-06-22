package com.pmrs.backend.controller;

import com.pmrs.backend.entity.Drive;
import com.pmrs.backend.service.DriveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Drive APIs", description = "Operations related to placement drives")
@RestController
@RequestMapping("/drives")
@CrossOrigin("*")
public class DriveController {

    private final DriveService driveService;

    public DriveController(DriveService driveService) {
        this.driveService = driveService;
    }

    @Operation(summary = "Get all drives")
    @GetMapping
    public List<Drive> getAllDrives() {
        return driveService.getAllDrives();
    }

    @Operation(summary = "Get drive by ID")
    @GetMapping("/{id}")
    public Drive getDriveById(@PathVariable Integer id) {
        return driveService.getDriveById(id);
    }

    @Operation(summary = "Get drives by status")
    @GetMapping("/status/{status}")
    public List<Drive> getDrivesByStatus(@PathVariable String status) {
        return driveService.getDrivesByStatus(status);
    }

    @Operation(summary = "Get drives by company ID")
    @GetMapping("/company/{companyId}")
    public List<Drive> getDrivesByCompany(@PathVariable Integer companyId) {
        return driveService.getDrivesByCompanyId(companyId);
    }

    @Operation(summary = "Create a new drive")
    @PostMapping
    public Drive createDrive(@Valid @RequestBody Drive drive) {
        return driveService.saveDrive(drive);
    }

    @Operation(summary = "Update drive by ID")
    @PutMapping("/{id}")
    public Drive updateDrive(@PathVariable Integer id,
                             @Valid @RequestBody Drive drive) {
        return driveService.updateDrive(id, drive);
    }

    @Operation(summary = "Delete drive by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteDrive(@PathVariable Integer id,
                                              Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            Drive drive = driveService.getDriveById(id);
            if (drive != null && "Completed".equalsIgnoreCase(drive.getStatus())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Placement Officers cannot delete completed drives. Contact an Admin.");
            }
        }

        driveService.deleteDrive(id);
        return ResponseEntity.ok("Drive deleted successfully");
    }
}
