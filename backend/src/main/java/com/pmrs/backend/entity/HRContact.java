package com.pmrs.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "HRContacts")
public class HRContact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hr_id")
    private Integer hrId;

    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;

    @NotBlank(message = "HR name is required")
    @Column(name = "hr_name")
    private String hrName;

    @NotBlank(message = "HR email is required")
    @Email(message = "HR email must be valid")
    @Column(name = "hr_email")
    private String hrEmail;

    @NotBlank(message = "HR phone is required")
    @Column(name = "hr_phone")
    private String hrPhone;

    @Column(name = "designation")
    private String designation;

    public Integer getHrId() {
        return hrId;
    }

    public void setHrId(Integer hrId) {
        this.hrId = hrId;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
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

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }
}
