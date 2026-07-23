package com.pmrs.backend.controller;

import com.pmrs.backend.dto.EligibleDriveDTO;
import com.pmrs.backend.dto.PlacementAchievementDTO;
import com.pmrs.backend.dto.PlacementOfferDTO;
import com.pmrs.backend.dto.PlacementStatusDTO;
import com.pmrs.backend.dto.StudentApplicationDTO;
import com.pmrs.backend.dto.StudentProfileDTO;
import com.pmrs.backend.service.StudentPortalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Student Portal APIs", description = "Endpoints accessible only by logged-in students")
@RestController
@RequestMapping("/api/student")
@CrossOrigin("*")
public class StudentPortalController {

    private final StudentPortalService portalService;

    public StudentPortalController(StudentPortalService portalService) {
        this.portalService = portalService;
    }

    @Operation(summary = "Get the logged-in student's profile")
    @GetMapping("/profile")
    public StudentProfileDTO getProfile(Authentication auth) {
        return portalService.getProfile(auth.getName());
    }

    @Operation(summary = "Get drives the student is eligible for")
    @GetMapping("/eligible-drives")
    public List<EligibleDriveDTO> getEligibleDrives(Authentication auth) {
        return portalService.getEligibleDrives(auth.getName());
    }

    @Operation(summary = "Get the student's own applications")
    @GetMapping("/applications")
    public List<StudentApplicationDTO> getApplications(Authentication auth) {
        return portalService.getApplications(auth.getName());
    }

    @Operation(summary = "Get the student's placement status")
    @GetMapping("/placement-status")
    public PlacementStatusDTO getPlacementStatus(Authentication auth) {
        return portalService.getPlacementStatus(auth.getName());
    }

    @Operation(summary = "Get all Selected offers for the student, sorted best-first (tier DESC, then most recent)")
    @GetMapping("/placement-offers")
    public List<PlacementOfferDTO> getPlacementOffers(Authentication auth) {
        return portalService.getPlacementOffers(auth.getName());
    }

    @Operation(summary = "Get recent placement achievements (Selected status only — public celebration feed)")
    @GetMapping("/achievements")
    public List<PlacementAchievementDTO> getRecentAchievements() {
        return portalService.getRecentAchievements();
    }

    @Operation(summary = "Accept a released offer — triggers placement confirmation and revokes blocked applications")
    @PostMapping("/applications/{appId}/accept-offer")
    public ResponseEntity<Map<String, String>> acceptOffer(@PathVariable Integer appId,
                                                           Authentication auth) {
        try {
            portalService.acceptOffer(auth.getName(), appId);
            return ResponseEntity.ok(Map.of("message", "Offer accepted. Congratulations!"));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @Operation(summary = "Reject the offer at the Selected stage — carries the same 1-month ban as a final-stage withdrawal")
    @PostMapping("/applications/{appId}/reject-offer")
    public ResponseEntity<Map<String, String>> rejectOffer(@PathVariable Integer appId,
                                                           Authentication auth) {
        try {
            String message = portalService.rejectOffer(auth.getName(), appId);
            return ResponseEntity.ok(Map.of("message", message));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @Operation(summary = "Withdraw from a drive mid-process — applies the stage-based penalty")
    @PostMapping("/applications/{appId}/withdraw")
    public ResponseEntity<Map<String, String>> withdrawApplication(@PathVariable Integer appId,
                                                                   Authentication auth) {
        try {
            String message = portalService.withdrawApplication(auth.getName(), appId);
            return ResponseEntity.ok(Map.of("message", message));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @Operation(summary = "Decline an already-accepted offer — permanent bar + disciplinary referral")
    @PostMapping("/applications/{appId}/decline-offer")
    public ResponseEntity<Map<String, String>> declineAcceptedOffer(@PathVariable Integer appId,
                                                                    Authentication auth) {
        try {
            String message = portalService.declineAcceptedOffer(auth.getName(), appId);
            return ResponseEntity.ok(Map.of("message", message));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @Operation(summary = "Preview the exact penalty withdrawing/declining this application would incur")
    @GetMapping("/applications/{appId}/withdrawal-penalty-preview")
    public Map<String, String> previewWithdrawalPenalty(@PathVariable Integer appId,
                                                        Authentication auth) {
        return portalService.previewWithdrawalPenalty(auth.getName(), appId);
    }

    @Operation(summary = "Get the student's current penalty standing (bans, skip counters, referral flag)")
    @GetMapping("/penalty-status")
    public com.pmrs.backend.dto.PenaltyStatusDTO getPenaltyStatus(Authentication auth) {
        return portalService.getPenaltyStatus(auth.getName());
    }

    @Operation(summary = "Apply to a drive")
    @PostMapping("/apply/{driveId}")
    public ResponseEntity<Map<String, String>> applyToDrive(@PathVariable Integer driveId,
                                                            Authentication auth) {
        try {
            portalService.applyToDrive(auth.getName(), driveId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("message", "Application submitted successfully"));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
        // TransactionSystemException / DataIntegrityViolationException (DB trigger violations)
        // propagate to GlobalExceptionHandler which strips the INELIGIBLE: prefix and returns 400
    }
}
