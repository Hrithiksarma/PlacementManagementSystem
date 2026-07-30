package com.pmrs.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "Drives")
public class Drive {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "drive_id")
    private Integer driveId;

    @NotNull(message = "Company is required")
    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;

    @ManyToOne
    @JoinColumn(name = "hr_id")
    private HRContact hrContact;

    @NotNull(message = "Drive date is required")
    @Column(name = "drive_date")
    private LocalDate driveDate;

    @Column(name = "ppt_date")
    private LocalDate pptDate;

    @Column(name = "resume_selection_date")
    private LocalDate resumeSelectionDate;

    @Column(name = "final_selection_date")
    private LocalDate finalSelectionDate;

    @NotBlank(message = "Role offered is required")
    @Column(name = "role_offered")
    private String roleOffered;

    @Column(name = "package_lpa")
    private BigDecimal packageLpa;

    @Column(name = "drive_type")
    private String driveType;

    @Column(name = "status")
    private String status;

    @Column(name = "min_cgpa")
    private Double minCgpa;

    @Column(name = "max_backlogs")
    private Integer maxBacklogs;

    public Integer getDriveId() {
        return driveId;
    }

    public void setDriveId(Integer driveId) {
        this.driveId = driveId;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public HRContact getHrContact() {
        return hrContact;
    }

    public void setHrContact(HRContact hrContact) {
        this.hrContact = hrContact;
    }

    public LocalDate getDriveDate() {
        return driveDate;
    }

    public void setDriveDate(LocalDate driveDate) {
        this.driveDate = driveDate;
    }

    public LocalDate getPptDate() {
        return pptDate;
    }

    public void setPptDate(LocalDate pptDate) {
        this.pptDate = pptDate;
    }

    public LocalDate getResumeSelectionDate() {
        return resumeSelectionDate;
    }

    public void setResumeSelectionDate(LocalDate resumeSelectionDate) {
        this.resumeSelectionDate = resumeSelectionDate;
    }

    public LocalDate getFinalSelectionDate() {
        return finalSelectionDate;
    }

    public void setFinalSelectionDate(LocalDate finalSelectionDate) {
        this.finalSelectionDate = finalSelectionDate;
    }

    public String getRoleOffered() {
        return roleOffered;
    }

    public void setRoleOffered(String roleOffered) {
        this.roleOffered = roleOffered;
    }

    public BigDecimal getPackageLpa() {
        return packageLpa;
    }

    public void setPackageLpa(BigDecimal packageLpa) {
        this.packageLpa = packageLpa;
    }

    public String getDriveType() {
        return driveType;
    }

    public void setDriveType(String driveType) {
        this.driveType = driveType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
}
