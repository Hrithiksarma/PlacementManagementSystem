package com.academic.service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(
    name = "students",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_student_roll",  columnNames = "roll_no"),
        @UniqueConstraint(name = "uq_student_email", columnNames = "email"),
        @UniqueConstraint(name = "uq_student_phone", columnNames = "phone")
    }
)
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "student_id")
    private Long studentId;

    @Column(name = "roll_no", length = 7, nullable = false)
    private String rollNo;

    @NotBlank
    @Column(name = "first_name", length = 60, nullable = false)
    private String firstName;

    @Column(name = "middle_name", length = 60)
    private String middleName;

    @NotBlank
    @Column(name = "last_name", length = 60, nullable = false)
    private String lastName;

    @Column(name = "gender", length = 10)
    private String gender;

    @NotBlank @Email
    @Column(name = "email", length = 120, nullable = false)
    private String email;

    @NotBlank
    @Pattern(regexp = "^[6-9][0-9]{9}$",
             message = "Must be a valid 10-digit Indian mobile number")
    @Column(name = "phone", length = 15, nullable = false)
    private String phone;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "dept_id", nullable = false)
    private Department department;

    @NotNull @Min(2000)
    @Column(name = "admission_year", nullable = false)
    private Integer admissionYear;

    @NotNull @DecimalMin("0.00") @DecimalMax("10.00")
    @Column(name = "cgpa", nullable = false)
    private Double cgpa;

    @Min(0)
    @Column(name = "active_backlogs", nullable = false)
    private Integer activeBacklogs = 0;

    // ── New academic fields ───────────────────────────────────

    @Column(name = "address", length = 500)
    private String address;

    @DecimalMin("0.00") @DecimalMax("100.00")
    @Column(name = "class10_percentage")
    private Double class10Percentage;

    @DecimalMin("0.00") @DecimalMax("100.00")
    @Column(name = "class12_percentage")
    private Double class12Percentage;

    /** B.Tech CGPA at time of M.Tech admission. NULL for B.Tech students. */
    @DecimalMin("0.00") @DecimalMax("10.00")
    @Column(name = "graduation_cgpa")
    private Double graduationCgpa;

    // ── Getters and setters ───────────────────────────────────

    public Long       getStudentId()                 { return studentId; }
    public void       setStudentId(Long v)           { this.studentId = v; }

    public String     getRollNo()                    { return rollNo; }
    public void       setRollNo(String v)            { this.rollNo = v; }

    public String     getFirstName()                 { return firstName; }
    public void       setFirstName(String v)         { this.firstName = v; }

    public String     getMiddleName()                { return middleName; }
    public void       setMiddleName(String v)        { this.middleName = v; }

    public String     getLastName()                  { return lastName; }
    public void       setLastName(String v)          { this.lastName = v; }

    public String     getGender()                    { return gender; }
    public void       setGender(String v)            { this.gender = v; }

    public String     getEmail()                     { return email; }
    public void       setEmail(String v)             { this.email = v; }

    public String     getPhone()                     { return phone; }
    public void       setPhone(String v)             { this.phone = v; }

    public Department getDepartment()                { return department; }
    public void       setDepartment(Department v)    { this.department = v; }

    public Integer    getAdmissionYear()             { return admissionYear; }
    public void       setAdmissionYear(Integer v)    { this.admissionYear = v; }

    public Double     getCgpa()                      { return cgpa; }
    public void       setCgpa(Double v)              { this.cgpa = v; }

    public Integer    getActiveBacklogs()            { return activeBacklogs; }
    public void       setActiveBacklogs(Integer v)   { this.activeBacklogs = v; }

    public String     getAddress()                   { return address; }
    public void       setAddress(String v)           { this.address = v; }

    public Double     getClass10Percentage()         { return class10Percentage; }
    public void       setClass10Percentage(Double v) { this.class10Percentage = v; }

    public Double     getClass12Percentage()         { return class12Percentage; }
    public void       setClass12Percentage(Double v) { this.class12Percentage = v; }

    public Double     getGraduationCgpa()            { return graduationCgpa; }
    public void       setGraduationCgpa(Double v)    { this.graduationCgpa = v; }
}
