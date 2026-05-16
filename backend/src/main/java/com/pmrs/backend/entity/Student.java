package com.pmrs.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "Students")
public class Student {

    @Id
    @Column(name = "student_id")
    private Integer studentId;

    @Column(name = "sname")
    private String sname;

    @Column(name = "email")
    private String email;

    @Column(name = "cgpa")
    private Double cgpa;

    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }

    public String getSname() {
        return sname;
    }

    public void setSname(String sname) {
        this.sname = sname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Double getCgpa() {
        return cgpa;
    }

    public void setCgpa(Double cgpa) {
        this.cgpa = cgpa;
    }
}