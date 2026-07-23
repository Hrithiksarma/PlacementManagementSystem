package com.pmrs.backend.controller;

import com.pmrs.backend.dto.PenaltyDTO;
import com.pmrs.backend.service.PenaltyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Placement Penalty APIs", description = "Review and lift withdrawal/decline penalties")
@RestController
@RequestMapping("/penalties")
@CrossOrigin("*")
public class PenaltyController {

    private final PenaltyService penaltyService;

    public PenaltyController(PenaltyService penaltyService) {
        this.penaltyService = penaltyService;
    }

    @Operation(summary = "List all penalties, newest first (active and historical)")
    @GetMapping
    public List<PenaltyDTO> getAllPenalties() {
        return penaltyService.getAllPenalties();
    }

    @Operation(summary = "Lift an active penalty (recorded with the lifting admin's name)")
    @PostMapping("/{id}/lift")
    public ResponseEntity<?> liftPenalty(@PathVariable Integer id, Authentication auth) {
        try {
            return ResponseEntity.ok(penaltyService.liftPenalty(id, auth.getName()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", e.getMessage()));
        }
    }
}
