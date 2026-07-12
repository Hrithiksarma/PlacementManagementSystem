package com.pmrs.backend.dto;

/**
 * Mirrors Academic ERP's StudentSyncMessage. Received on the "student-updates"
 * Redis channel whenever a student is edited in Academic ERP; applied to the
 * matching PRMS Student (matched by rollNo) if one has been imported.
 */
public class StudentSyncMessage {

    private String  rollNo;
    private String  firstName;
    private String  middleName;
    private String  lastName;
    private String  email;
    private String  phone;
    private Integer deptId;
    private Integer admissionYear;
    private Double  cgpa;
    private Integer activeBacklogs;

    /** Builds the single name string PRMS stores. */
    public String buildFullName() {
        StringBuilder sb = new StringBuilder(firstName);
        if (middleName != null && !middleName.isBlank()) {
            sb.append(" ").append(middleName.trim());
        }
        sb.append(" ").append(lastName);
        return sb.toString().trim();
    }

    public String  getRollNo()                     { return rollNo; }
    public void    setRollNo(String v)              { this.rollNo = v; }

    public String  getFirstName()                   { return firstName; }
    public void    setFirstName(String v)            { this.firstName = v; }

    public String  getMiddleName()                  { return middleName; }
    public void    setMiddleName(String v)           { this.middleName = v; }

    public String  getLastName()                    { return lastName; }
    public void    setLastName(String v)             { this.lastName = v; }

    public String  getEmail()                       { return email; }
    public void    setEmail(String v)                { this.email = v; }

    public String  getPhone()                       { return phone; }
    public void    setPhone(String v)                { this.phone = v; }

    public Integer getDeptId()                      { return deptId; }
    public void    setDeptId(Integer v)              { this.deptId = v; }

    public Integer getAdmissionYear()                { return admissionYear; }
    public void    setAdmissionYear(Integer v)       { this.admissionYear = v; }

    public Double  getCgpa()                        { return cgpa; }
    public void    setCgpa(Double v)                 { this.cgpa = v; }

    public Integer getActiveBacklogs()               { return activeBacklogs; }
    public void    setActiveBacklogs(Integer v)      { this.activeBacklogs = v; }
}
