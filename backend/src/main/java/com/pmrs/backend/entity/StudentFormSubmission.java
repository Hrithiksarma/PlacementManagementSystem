package com.pmrs.backend.entity;

import jakarta.persistence.*;

/**
 * Staging row for a student who registered through the Google Form.
 *
 * <p>Rows are pulled from the form's linked Google Sheet by
 * StudentFormSyncService and land here as PENDING. Nothing touches the real
 * Students table until a Placement Officer or Admin clicks "Import All
 * Students" (or a single-row "Import"), which promotes the row via
 * StudentFormService.
 *
 * <p>Mirrors {@link DriveFormSubmission} in shape and lifecycle.
 */
@Entity
@Table(name = "StudentFormSubmissions")
public class StudentFormSubmission {

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

    @Column(name = "full_name")
    private String fullName;

    /**
     * Roll number exactly as typed by the student. Length 20 (not 7) so a
     * mistyped value still stages and surfaces a clear validation error at
     * import time, rather than failing the sync. The real 7-char constraint
     * lives on {@link Student}.
     */
    @Column(name = "roll_no", length = 20)
    private String rollNo;

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "program")
    private String program;

    @Column(name = "branch")
    private String branch;

    @Column(name = "batch_year")
    private Integer batchYear;

    @Column(name = "cgpa")
    private Double cgpa;

    @Column(name = "active_backlogs")
    private Integer activeBacklogs;

    /** Raw Google Drive URL of the uploaded resume, straight from the sheet. */
    @Column(name = "resume_url", length = 500)
    private String resumeUrl;

    /** Raw Google Drive URL of the profile photo. */
    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    /** Raw Google Drive URL of the grade card / gradesheet. */
    @Column(name = "grade_sheet_url", length = 500)
    private String gradeSheetUrl;

    @Column(name = "status", nullable = false)
    private String status = STATUS_PENDING;

    /** Set once the submission is imported — the Student it created. */
    @Column(name = "student_id")
    private Integer studentId;

    /** Why the last bulk import skipped this row (null when it hasn't failed). */
    @Column(name = "failure_reason", length = 500)
    private String failureReason;

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

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getRollNo() {
        return rollNo;
    }

    public void setRollNo(String rollNo) {
        this.rollNo = rollNo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getProgram() {
        return program;
    }

    public void setProgram(String program) {
        this.program = program;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }
}
