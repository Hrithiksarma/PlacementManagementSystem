package com.pmrs.backend.dto;

/**
 * Represents the response from Academic ERP GET /api/students/{rollNo}.
 * Only fields that PRMS cares about are mapped here.
 * placementTier is not present — PRMS always defaults it to "Unplaced".
 */
public class AcademicStudentDTO {

    private String  rollNo;
    private String  firstName;
    private String  middleName;
    private String  lastName;
    private String  email;
    private String  phone;
    private DepartmentInfo department;
    private Integer admissionYear;
    private Double  cgpa;
    private Integer activeBacklogs;

    public static class DepartmentInfo {
        private Integer deptId;
        private String  program;
        private String  branch;
        private String  section;
        private String  deptName;

        public Integer getDeptId()              { return deptId; }
        public void    setDeptId(Integer v)     { this.deptId = v; }

        public String  getProgram()             { return program; }
        public void    setProgram(String v)     { this.program = v; }

        public String  getBranch()              { return branch; }
        public void    setBranch(String v)      { this.branch = v; }

        public String  getSection()             { return section; }
        public void    setSection(String v)     { this.section = v; }

        public String  getDeptName()            { return deptName; }
        public void    setDeptName(String v)    { this.deptName = v; }
    }

    /** Builds the single name string PRMS stores. */
    public String buildFullName() {
        StringBuilder sb = new StringBuilder(firstName);
        if (middleName != null && !middleName.isBlank()) {
            sb.append(" ").append(middleName.trim());
        }
        sb.append(" ").append(lastName);
        return sb.toString().trim();
    }

    public String  getRollNo()                          { return rollNo; }
    public void    setRollNo(String v)                  { this.rollNo = v; }

    public String  getFirstName()                       { return firstName; }
    public void    setFirstName(String v)               { this.firstName = v; }

    public String  getMiddleName()                      { return middleName; }
    public void    setMiddleName(String v)              { this.middleName = v; }

    public String  getLastName()                        { return lastName; }
    public void    setLastName(String v)                { this.lastName = v; }

    public String  getEmail()                           { return email; }
    public void    setEmail(String v)                   { this.email = v; }

    public String  getPhone()                           { return phone; }
    public void    setPhone(String v)                   { this.phone = v; }

    public DepartmentInfo getDepartment()               { return department; }
    public void           setDepartment(DepartmentInfo v) { this.department = v; }

    public Integer getAdmissionYear()                   { return admissionYear; }
    public void    setAdmissionYear(Integer v)          { this.admissionYear = v; }

    public Double  getCgpa()                            { return cgpa; }
    public void    setCgpa(Double v)                    { this.cgpa = v; }

    public Integer getActiveBacklogs()                  { return activeBacklogs; }
    public void    setActiveBacklogs(Integer v)         { this.activeBacklogs = v; }
}
