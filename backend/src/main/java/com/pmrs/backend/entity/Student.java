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

    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
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
}
