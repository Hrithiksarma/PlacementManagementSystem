package com.pmrs.backend.controller;

import com.pmrs.backend.dto.DashboardDTO;
import com.pmrs.backend.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Dashboard API", description = "Placement analytics dashboard")
@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin("*")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Operation(summary = "Get placement analytics — optionally filtered by year, branch, program")
    @GetMapping
    public DashboardDTO getDashboard(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String branch,
            @RequestParam(required = false) String program) {
        return dashboardService.getDashboardData(year, branch, program);
    }
}
