package com.pmrs.backend.controller;

import com.pmrs.backend.dto.CreateOfficerRequest;
import com.pmrs.backend.dto.OfficerAccountDTO;
import com.pmrs.backend.dto.OfficerCreatedDTO;
import com.pmrs.backend.dto.OfficerWelcomeEmailRequest;
import com.pmrs.backend.service.OfficerAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Admin APIs", description = "Placement officer account management (ADMIN only)")
@RestController
@RequestMapping("/admin/officers")
@CrossOrigin("*")
public class AdminController {

    private final OfficerAccountService officerAccountService;

    public AdminController(OfficerAccountService officerAccountService) {
        this.officerAccountService = officerAccountService;
    }

    @Operation(summary = "Create a placement officer account with an auto-generated temporary password")
    @PostMapping
    public ResponseEntity<OfficerCreatedDTO> createOfficer(@Valid @RequestBody CreateOfficerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(officerAccountService.createOfficer(request));
    }

    @Operation(summary = "Email a newly-created officer their username, temporary password, and an optional admin note")
    @PostMapping("/{id}/send-welcome-email")
    public void sendWelcomeEmail(@PathVariable Long id, @Valid @RequestBody OfficerWelcomeEmailRequest request) {
        officerAccountService.sendWelcomeEmail(id, request.getTemporaryPassword(), request.getComment());
    }

    @Operation(summary = "List all placement officer accounts")
    @GetMapping
    public List<OfficerAccountDTO> listOfficers() {
        return officerAccountService.listOfficers();
    }

    @Operation(summary = "Disable a placement officer account")
    @PatchMapping("/{id}/disable")
    public ResponseEntity<Void> disable(@PathVariable Long id) {
        officerAccountService.setEnabled(id, false);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Re-enable a placement officer account")
    @PatchMapping("/{id}/enable")
    public ResponseEntity<Void> enable(@PathVariable Long id) {
        officerAccountService.setEnabled(id, true);
        return ResponseEntity.noContent().build();
    }
}
