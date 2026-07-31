package com.pmrs.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * A manually-logged HR contact for a company that has no presence in PRMS
 * yet — deliberately NOT linked to {@link Company}/{@link HRContact}. Never
 * promoted into a real Company or Drive; it's just contact information.
 */
@Entity
@Table(name = "ExternalHrContacts")
public class ExternalHrContact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @NotBlank(message = "Company name is required")
    @Column(name = "company_name")
    private String companyName;

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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
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
