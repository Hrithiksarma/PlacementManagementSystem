package com.pmrs.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

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

    /** Google Drive URL of the resume PDF the student uploaded via the form. */
    @Column(name = "resume_url", length = 500)
    private String resumeUrl;

    /** Google Drive URL of the profile photo uploaded via the form. */
    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    /** Google Drive URL of the grade card / gradesheet uploaded via the form. */
    @Column(name = "grade_sheet_url", length = 500)
    private String gradeSheetUrl;

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
}