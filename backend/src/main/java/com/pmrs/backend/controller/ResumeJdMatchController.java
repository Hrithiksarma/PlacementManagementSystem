package com.pmrs.backend.controller;

import com.pmrs.backend.dto.ResumeCritiqueDTO;
import com.pmrs.backend.dto.ResumeJdMatchResultDTO;
import com.pmrs.backend.service.ResumeJdMatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Resume/JD Match APIs", description = "Resume-JD skills-gap analysis for the logged-in student")
@RestController
@RequestMapping("/api/student")
@CrossOrigin("*")
public class ResumeJdMatchController {

    private final ResumeJdMatchService resumeJdMatchService;

    public ResumeJdMatchController(ResumeJdMatchService resumeJdMatchService) {
        this.resumeJdMatchService = resumeJdMatchService;
    }

    @Operation(summary = "Run a skills-gap analysis of the logged-in student's resume against a drive's JD")
    @PostMapping("/drives/{driveId}/analyze")
    public ResumeJdMatchResultDTO analyze(@PathVariable Integer driveId, Authentication auth) {
        return resumeJdMatchService.analyze(auth.getName(), driveId);
        // IllegalStateException (no resume / no JD text) propagates to
        // GlobalExceptionHandler, which returns 409 with the message intact.
    }

    @Operation(summary = "Get the most recent skills-gap match for this student/drive, if one exists")
    @GetMapping("/drives/{driveId}/matches")
    public ResponseEntity<ResumeJdMatchResultDTO> getLatestMatch(@PathVariable Integer driveId, Authentication auth) {
        ResumeJdMatchResultDTO result = resumeJdMatchService.getLatestMatch(auth.getName(), driveId);
        return result != null ? ResponseEntity.ok(result) : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @Operation(summary = "Get a critique of the logged-in student's resume — formatting, clarity, ATS-friendliness")
    @PostMapping("/resume-critique")
    public ResumeCritiqueDTO critiqueResume(Authentication auth) {
        return resumeJdMatchService.critiqueResume(auth.getName());
        // IllegalStateException (no resume) propagates to GlobalExceptionHandler → 409.
    }
}
