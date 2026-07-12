package com.academic.service.dto;

public class StudentResponseDTO {

    private Long    studentId;
    private String  rollNo;
    private String  firstName;
    private String  middleName;
    private String  lastName;
    private String  gender;
    private String  email;
    private String  phone;
    private DepartmentInfo department;
    private Integer admissionYear;
    private Double  cgpa;
    private Integer activeBacklogs;

    // ── New academic fields ───────────────────────────────────
    private String  address;
    private Double  class10Percentage;
    private Double  class12Percentage;
    private Double  graduationCgpa;

    public static class DepartmentInfo {
        private Integer deptId;
        private String  section;
        private String  program;
        private String  branch;
        private String  deptName;

        public Integer getDeptId()           { return deptId; }
        public void    setDeptId(Integer v)  { this.deptId = v; }
        public String  getSection()          { return section; }
        public void    setSection(String v)  { this.section = v; }
        public String  getProgram()          { return program; }
        public void    setProgram(String v)  { this.program = v; }
        public String  getBranch()           { return branch; }
        public void    setBranch(String v)   { this.branch = v; }
        public String  getDeptName()         { return deptName; }
        public void    setDeptName(String v) { this.deptName = v; }
    }

    public Long    getStudentId()                         { return studentId; }
    public void    setStudentId(Long v)                   { this.studentId = v; }
    public String  getRollNo()                            { return rollNo; }
    public void    setRollNo(String v)                    { this.rollNo = v; }
    public String  getFirstName()                         { return firstName; }
    public void    setFirstName(String v)                 { this.firstName = v; }
    public String  getMiddleName()                        { return middleName; }
    public void    setMiddleName(String v)                { this.middleName = v; }
    public String  getLastName()                          { return lastName; }
    public void    setLastName(String v)                  { this.lastName = v; }
    public String  getGender()                            { return gender; }
    public void    setGender(String v)                    { this.gender = v; }
    public String  getEmail()                             { return email; }
    public void    setEmail(String v)                     { this.email = v; }
    public String  getPhone()                             { return phone; }
    public void    setPhone(String v)                     { this.phone = v; }
    public DepartmentInfo getDepartment()                 { return department; }
    public void    setDepartment(DepartmentInfo v)        { this.department = v; }
    public Integer getAdmissionYear()                     { return admissionYear; }
    public void    setAdmissionYear(Integer v)            { this.admissionYear = v; }
    public Double  getCgpa()                              { return cgpa; }
    public void    setCgpa(Double v)                      { this.cgpa = v; }
    public Integer getActiveBacklogs()                    { return activeBacklogs; }
    public void    setActiveBacklogs(Integer v)           { this.activeBacklogs = v; }
    public String  getAddress()                           { return address; }
    public void    setAddress(String v)                   { this.address = v; }
    public Double  getClass10Percentage()                 { return class10Percentage; }
    public void    setClass10Percentage(Double v)         { this.class10Percentage = v; }
    public Double  getClass12Percentage()                 { return class12Percentage; }
    public void    setClass12Percentage(Double v)         { this.class12Percentage = v; }
    public Double  getGraduationCgpa()                    { return graduationCgpa; }
    public void    setGraduationCgpa(Double v)            { this.graduationCgpa = v; }
}
