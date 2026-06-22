package com.pmrs.backend.controller;

import com.pmrs.backend.entity.EligibilityCriteria;
import com.pmrs.backend.service.EligibilityCriteriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Eligibility Criteria APIs", description = "Operations related to drive eligibility criteria")
@RestController
@RequestMapping("/eligibility")
@CrossOrigin("*")
public class EligibilityCriteriaController {

    private final EligibilityCriteriaService eligibilityService;

    public EligibilityCriteriaController(EligibilityCriteriaService eligibilityService) {
        this.eligibilityService = eligibilityService;
    }

    @Operation(summary = "Get all eligibility criteria")
    @GetMapping
    public List<EligibilityCriteria> getAllCriteria() {
        return eligibilityService.getAllCriteria();
    }

    @Operation(summary = "Get eligibility criteria by ID")
    @GetMapping("/{id}")
    public EligibilityCriteria getCriteriaById(@PathVariable Integer id) {
        return eligibilityService.getCriteriaById(id);
    }

    @Operation(summary = "Get eligibility criteria by drive ID")
    @GetMapping("/drive/{driveId}")
    public List<EligibilityCriteria> getCriteriaByDrive(@PathVariable Integer driveId) {
        return eligibilityService.getCriteriaByDriveId(driveId);
    }

    @Operation(summary = "Create eligibility criteria")
    @PostMapping
    public EligibilityCriteria createCriteria(@Valid @RequestBody EligibilityCriteria criteria) {
        return eligibilityService.saveCriteria(criteria);
    }

    @Operation(summary = "Update eligibility criteria by ID")
    @PutMapping("/{id}")
    public EligibilityCriteria updateCriteria(@PathVariable Integer id,
                                              @Valid @RequestBody EligibilityCriteria criteria) {
        return eligibilityService.updateCriteria(id, criteria);
    }

    @Operation(summary = "Delete eligibility criteria by ID")
    @DeleteMapping("/{id}")
    public String deleteCriteria(@PathVariable Integer id) {
        eligibilityService.deleteCriteria(id);
        return "Eligibility Criteria deleted successfully";
    }
}
