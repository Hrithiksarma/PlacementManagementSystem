package com.pmrs.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "Students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "student_id")
    private Integer studentId;

    // Roll number is assigned by LocalRollNumberService before save.
    // JPA writes and reads this column normally — no trigger involved.
    @Column(name = "roll_no", unique = true, length = 7, nullable = false)
    private String rollNo;

    @NotBlank(message = "Name is required")
    @Column(name = "name")
    private String name;

    @ManyToOne
    @JoinColumn(name = "dept_id")
    private Department department;

    @NotNull(message = "Batch year is required")
    @Column(name = "batch_year")
    private Integer batchYear;

    @DecimalMin(value = "0.0", message = "CGPA must be at least 0.0")
    @DecimalMax(value = "10.0", message = "CGPA must be at most 10.0")
    @Column(name = "cgpa")
    private Double cgpa;

    @Min(value = 0, message = "Active backlogs cannot be negative")
    @Column(name = "active_backlogs")
    private Integer activeBacklogs;

    @NotBlank(message = "Phone is required")
    @Column(name = "phone")
    private String phone;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Column(name = "email")
    private String email;

    @Column(name = "placement_tier")
    private String placementTier;

    @Column(name = "opted_for_higher_studies")
    private Boolean optedForHigherStudies = false;

    /** Google Drive URL of the resume PDF the student uploaded via the form. */
    @Column(name = "resume_url", length = 500)
    private String resumeUrl;

    /** Google Drive URL of the profile photo uploaded via the form. */
    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    /** Google Drive URL of the grade card / gradesheet uploaded via the form. */
    @Column(name = "grade_sheet_url", length = 500)
    private String gradeSheetUrl;

    /** Plain text extracted from resumeUrl via DocumentTextExtractionService —
     *  cached so the resume/JD skills-gap analyzer doesn't re-download and
     *  re-parse the same resume on every analysis request. */
    @Column(name = "resume_text_cache", columnDefinition = "TEXT")
    private String resumeTextCache;

    /** The resumeUrl value resumeTextCache was extracted from — if resumeUrl no
     *  longer matches this, the cache is stale and needs re-extracting. */
    @Column(name = "resume_text_source_url", length = 500)
    private String resumeTextSourceUrl;

    @Column(name = "resume_text_extracted_at")
    private LocalDateTime resumeTextExtractedAt;

    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }

    public String getRollNo() {
        return rollNo;
    }

    public void setRollNo(String rollNo) {
        this.rollNo = rollNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public Integer getBatchYear() {
        return batchYear;
    }

    public void setBatchYear(Integer batchYear) {
        this.batchYear = batchYear;
    }

    public Double getCgpa() {
        return cgpa;
    }

    public void setCgpa(Double cgpa) {
        this.cgpa = cgpa;
    }

    public Integer getActiveBacklogs() {
        return activeBacklogs;
    }

    public void setActiveBacklogs(Integer activeBacklogs) {
        this.activeBacklogs = activeBacklogs;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPlacementTier() {
        return placementTier;
    }

    public void setPlacementTier(String placementTier) {
        this.placementTier = placementTier;
    }

    public Boolean getOptedForHigherStudies() {
        return optedForHigherStudies;
    }

    public void setOptedForHigherStudies(Boolean optedForHigherStudies) {
        this.optedForHigherStudies = optedForHigherStudies;
    }

    public String getResumeUrl() {
        return resumeUrl;
    }

    public void setResumeUrl(String resumeUrl) {
        this.resumeUrl = resumeUrl;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getGradeSheetUrl() {
        return gradeSheetUrl;
    }

    public void setGradeSheetUrl(String gradeSheetUrl) {
        this.gradeSheetUrl = gradeSheetUrl;
    }

    public String getResumeTextCache() {
        return resumeTextCache;
    }

    public void setResumeTextCache(String resumeTextCache) {
        this.resumeTextCache = resumeTextCache;
    }

    public String getResumeTextSourceUrl() {
        return resumeTextSourceUrl;
    }

    public void setResumeTextSourceUrl(String resumeTextSourceUrl) {
        this.resumeTextSourceUrl = resumeTextSourceUrl;
    }

    public LocalDateTime getResumeTextExtractedAt() {
        return resumeTextExtractedAt;
    }

    public void setResumeTextExtractedAt(LocalDateTime resumeTextExtractedAt) {
        this.resumeTextExtractedAt = resumeTextExtractedAt;
    }
}