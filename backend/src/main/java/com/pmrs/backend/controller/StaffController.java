package com.pmrs.backend.controller;

import com.pmrs.backend.dto.CreateStaffRequest;
import com.pmrs.backend.dto.StaffAccountDTO;
import com.pmrs.backend.dto.StaffCreatedDTO;
import com.pmrs.backend.dto.StaffWelcomeEmailRequest;
import com.pmrs.backend.service.StaffAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin APIs", description = "Shared department-staff portal account management (ADMIN only)")
@RestController
@RequestMapping("/admin/staff")
@CrossOrigin("*")
public class StaffController {

    private final StaffAccountService staffAccountService;

    public StaffController(StaffAccountService staffAccountService) {
        this.staffAccountService = staffAccountService;
    }

    @Operation(summary = "Get the current shared staff portal account, if one has been created")
    @GetMapping
    public ResponseEntity<StaffAccountDTO> getStaffAccount() {
        return staffAccountService.getStaffAccount()
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @Operation(summary = "Create the shared staff portal account with an auto-generated temporary password (fails if one already exists)")
    @PostMapping
    public ResponseEntity<StaffCreatedDTO> createStaff(@Valid @RequestBody CreateStaffRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(staffAccountService.createStaff(request));
    }

    @Operation(summary = "Rotate the shared staff portal password")
    @PostMapping("/reset-password")
    public StaffCreatedDTO resetPassword() {
        return staffAccountService.resetPassword();
    }

    @Operation(summary = "Email the shared staff account's username, temporary password, and an optional admin note")
    @PostMapping("/{id}/send-welcome-email")
    public void sendWelcomeEmail(@PathVariable Long id, @Valid @RequestBody StaffWelcomeEmailRequest request) {
        staffAccountService.sendWelcomeEmail(id, request.getTemporaryPassword(), request.getComment());
    }

    @Operation(summary = "Disable the shared staff portal account")
    @PatchMapping("/{id}/disable")
    public ResponseEntity<Void> disable(@PathVariable Long id) {
        staffAccountService.setEnabled(id, false);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Re-enable the shared staff portal account")
    @PatchMapping("/{id}/enable")
    public ResponseEntity<Void> enable(@PathVariable Long id) {
        staffAccountService.setEnabled(id, true);
        return ResponseEntity.noContent().build();
    }
}
