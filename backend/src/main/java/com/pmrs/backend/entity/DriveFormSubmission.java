package com.pmrs.backend.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Staging row for a drive submitted by a company through the Google Form.
 *
 * <p>Rows are pulled from the form's linked Google Sheet by
 * GoogleFormSyncService and land here as PENDING. Nothing touches the real
 * Companies/Drives tables until a Placement Officer or Admin clicks
 * "Include Company", which promotes the row via DriveFormService.
 */
@Entity
@Table(name = "DriveFormSubmissions")
public class DriveFormSubmission {

    public static final String STATUS_PENDING  = "PENDING";
    public static final String STATUS_INCLUDED = "INCLUDED";
    public static final String STATUS_REJECTED = "REJECTED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "submission_id")
    private Integer submissionId;

    /** Stable fingerprint of the sheet row — prevents duplicates across polls. */
    @Column(name = "response_key", unique = true, nullable = false, length = 300)
    private String responseKey;

    /** Raw timestamp string from the form's Timestamp column (kept as-is for display). */
    @Column(name = "submitted_at")
    private String submittedAt;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "sector")
    private String sector;

    @Column(name = "tier")
    private String tier;

    @Column(name = "website")
    private String website;

    @Column(name = "hr_name")
    private String hrName;

    @Column(name = "hr_email")
    private String hrEmail;

    @Column(name = "hr_phone")
    private String hrPhone;

    @Column(name = "drive_date")
    private LocalDate driveDate;

    @Column(name = "ppt_date")
    private LocalDate pptDate;

    @Column(name = "resume_selection_date")
    private LocalDate resumeSelectionDate;

    @Column(name = "final_selection_date")
    private LocalDate finalSelectionDate;

    @Column(name = "role_offered")
    private String roleOffered;

    @Column(name = "jd_url", length = 500)
    private String jdUrl;

    @Column(name = "package_lpa")
    private BigDecimal packageLpa;

    @Column(name = "drive_type")
    private String driveType;

    @Column(name = "min_cgpa")
    private Double minCgpa;

    @Column(name = "max_backlogs")
    private Integer maxBacklogs;

    @Column(name = "status", nullable = false)
    private String status = STATUS_PENDING;

    /** Set once the submission is included — the Drive it created. */
    @Column(name = "drive_id")
    private Integer driveId;

    public Integer getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(Integer submissionId) {
        this.submissionId = submissionId;
    }

    public String getResponseKey() {
        return responseKey;
    }

    public void setResponseKey(String responseKey) {
        this.responseKey = responseKey;
    }

    public String getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(String submittedAt) {
        this.submittedAt = submittedAt;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getSector() {
        return sector;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }

    public String getTier() {
        return tier;
    }

    public void setTier(String tier) {
        this.tier = tier;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getHrName() {
        return hrName;
    }

    public void setHrName(String hrName) {
        this.hrName = hrName;
    }

    public String getHrEmail() {
        return hrEmail;
    }

    public void setHrEmail(String hrEmail) {
        this.hrEmail = hrEmail;
    }

    public String getHrPhone() {
        return hrPhone;
    }

    public void setHrPhone(String hrPhone) {
        this.hrPhone = hrPhone;
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

    public String getJdUrl() {
        return jdUrl;
    }

    public void setJdUrl(String jdUrl) {
        this.jdUrl = jdUrl;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getDriveId() {
        return driveId;
    }

    public void setDriveId(Integer driveId) {
        this.driveId = driveId;
    }
}
