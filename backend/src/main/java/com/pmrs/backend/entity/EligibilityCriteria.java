package com.pmrs.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;

@Entity
@Table(name = "EligibilityCriteria")
public class EligibilityCriteria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "criteria_id")
    private Integer criteriaId;

    @ManyToOne
    @JoinColumn(name = "drive_id")
    private Drive drive;

    @DecimalMin(value = "0.0", message = "Minimum CGPA must be at least 0.0")
    @DecimalMax(value = "10.0", message = "Minimum CGPA must be at most 10.0")
    @Column(name = "min_cgpa")
    private Double minCgpa;

    @Min(value = 0, message = "Max backlogs cannot be negative")
    @Column(name = "max_backlogs")
    private Integer maxBacklogs;

    @Column(name = "allowed_tiers")
    private String allowedTiers;

    @Column(name = "allowed_departments")
    private String allowedDepartments;

    public Integer getCriteriaId() {
        return criteriaId;
    }

    public void setCriteriaId(Integer criteriaId) {
        this.criteriaId = criteriaId;
    }

    public Drive getDrive() {
        return drive;
    }

    public void setDrive(Drive drive) {
        this.drive = drive;
    }

    public Double getMinCgpa() {
        return minCgpa;
    }

    public void setMinCgpa(Double minCgpa) {
        this.minCgpa = minCgpa;
    }

    public Integer getMaxBacklogs() {
        return maxBacklogs;
    }

    public void setMaxBacklogs(Integer maxBacklogs) {
        this.maxBacklogs = maxBacklogs;
    }

    public String getAllowedTiers() {
        return allowedTiers;
    }

    public void setAllowedTiers(String allowedTiers) {
        this.allowedTiers = allowedTiers;
    }

    public String getAllowedDepartments() {
        return allowedDepartments;
    }

    public void setAllowedDepartments(String allowedDepartments) {
        this.allowedDepartments = allowedDepartments;
    }
}
