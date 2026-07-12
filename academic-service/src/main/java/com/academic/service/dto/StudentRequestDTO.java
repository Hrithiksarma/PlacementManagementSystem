package com.academic.service.dto;

import jakarta.validation.constraints.*;

public class StudentRequestDTO {

    @NotBlank
    private String firstName;

    private String middleName;

    @NotBlank
    private String lastName;

    private String gender;

    @NotBlank @Email
    private String email;

    @NotBlank
    @Pattern(regexp = "^[6-9][0-9]{9}$",
             message = "Must be a valid 10-digit Indian mobile number")
    private String phone;

    @NotNull
    private Integer deptId;

    @NotNull @Min(2000)
    private Integer admissionYear;

    @NotNull @DecimalMin("0.00") @DecimalMax("10.00")
    private Double cgpa;

    @Min(0)
    private Integer activeBacklogs = 0;

    // ── New academic fields ───────────────────────────────────

    private String address;

    @DecimalMin("0.00") @DecimalMax("100.00")
    private Double class10Percentage;

    @DecimalMin("0.00") @DecimalMax("100.00")
    private Double class12Percentage;

    /** Required for M.Tech students. Leave null for B.Tech. */
    @DecimalMin("0.00") @DecimalMax("10.00")
    private Double graduationCgpa;

    // ── Getters and setters ───────────────────────────────────

    public String  getFirstName()                       { return firstName; }
    public void    setFirstName(String v)               { this.firstName = v; }

    public String  getMiddleName()                      { return middleName; }
    public void    setMiddleName(String v)              { this.middleName = v; }

    public String  getLastName()                        { return lastName; }
    public void    setLastName(String v)                { this.lastName = v; }

    public String  getGender()                          { return gender; }
    public void    setGender(String v)                  { this.gender = v; }

    public String  getEmail()                           { return email; }
    public void    setEmail(String v)                   { this.email = v; }

    public String  getPhone()                           { return phone; }
    public void    setPhone(String v)                   { this.phone = v; }

    public Integer getDeptId()                          { return deptId; }
    public void    setDeptId(Integer v)                 { this.deptId = v; }

    public Integer getAdmissionYear()                   { return admissionYear; }
    public void    setAdmissionYear(Integer v)          { this.admissionYear = v; }

    public Double  getCgpa()                            { return cgpa; }
    public void    setCgpa(Double v)                    { this.cgpa = v; }

    public Integer getActiveBacklogs()                  { return activeBacklogs; }
    public void    setActiveBacklogs(Integer v)         { this.activeBacklogs = v; }

    public String  getAddress()                         { return address; }
    public void    setAddress(String v)                 { this.address = v; }

    public Double  getClass10Percentage()               { return class10Percentage; }
    public void    setClass10Percentage(Double v)       { this.class10Percentage = v; }

    public Double  getClass12Percentage()               { return class12Percentage; }
    public void    setClass12Percentage(Double v)       { this.class12Percentage = v; }

    public Double  getGraduationCgpa()                  { return graduationCgpa; }
    public void    setGraduationCgpa(Double v)          { this.graduationCgpa = v; }
}
