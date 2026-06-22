package com.pmrs.backend.controller;

import com.pmrs.backend.entity.Company;
import com.pmrs.backend.service.CompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Company APIs", description = "Operations related to companies")
@RestController
@RequestMapping("/companies")
@CrossOrigin("*")
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @Operation(summary = "Get all companies")
    @GetMapping
    public List<Company> getAllCompanies() {
        return companyService.getAllCompanies();
    }

    @Operation(summary = "Get company by ID")
    @GetMapping("/{id}")
    public Company getCompanyById(@PathVariable Integer id) {
        return companyService.getCompanyById(id);
    }

    @Operation(summary = "Create a new company")
    @PostMapping
    public ResponseEntity<Company> saveCompany(@Valid @RequestBody Company company) {
        Company saved = companyService.saveCompany(company);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @Operation(summary = "Update company by ID")
    @PutMapping("/{id}")
    public Company updateCompany(@PathVariable Integer id,
                                 @Valid @RequestBody Company company) {
        return companyService.updateCompany(id, company);
    }

    @Operation(summary = "Delete company by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCompany(@PathVariable Integer id) {
        companyService.deleteCompany(id);
        return ResponseEntity.ok("Company deleted successfully");
    }

    @Operation(summary = "Get companies by tier")
    @GetMapping("/tier/{tier}")
    public List<Company> getCompaniesByTier(@PathVariable String tier) {
        return companyService.getCompaniesByTier(tier);
    }

    @Operation(summary = "Get companies by sector")
    @GetMapping("/sector/{sector}")
    public List<Company> getCompaniesBySector(@PathVariable String sector) {
        return companyService.getCompaniesBySector(sector);
    }
}
