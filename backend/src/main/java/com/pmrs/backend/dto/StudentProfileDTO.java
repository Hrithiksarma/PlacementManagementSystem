package com.pmrs.backend.dto;

public class StudentProfileDTO {

    private Integer studentId;
    private String  rollNo;
    private String  name;
    private String  email;
    private String  phone;
    private String  branch;
    private String  program;
    private Integer batchYear;
    private Double  cgpa;
    private Integer activeBacklogs;
    private String  placementTier;    // Actual placement outcome: Unplaced / Normal / Dream / Super Dream
    private String  placementStatus;  // "Placed" | "Not Placed"

    public StudentProfileDTO() {}

    public StudentProfileDTO(Integer studentId, String rollNo, String name, String email, String phone,
                             String branch, String program, Integer batchYear,
                             Double cgpa, Integer activeBacklogs,
                             String placementTier, String placementStatus) {
        this.studentId       = studentId;
        this.rollNo          = rollNo;
        this.name            = name;
        this.email           = email;
        this.phone           = phone;
        this.branch          = branch;
        this.program         = program;
        this.batchYear       = batchYear;
        this.cgpa            = cgpa;
        this.activeBacklogs  = activeBacklogs;
        this.placementTier   = placementTier;
        this.placementStatus = placementStatus;
    }

    public Integer getStudentId()       { return studentId; }
    public void    setStudentId(Integer v) { this.studentId = v; }

    public String  getRollNo()          { return rollNo; }
    public void    setRollNo(String v)  { this.rollNo = v; }

    public String  getName()            { return name; }
    public void    setName(String v)    { this.name = v; }

    public String  getEmail()           { return email; }
    public void    setEmail(String v)   { this.email = v; }

    public String  getPhone()           { return phone; }
    public void    setPhone(String v)   { this.phone = v; }

    public String  getBranch()          { return branch; }
    public void    setBranch(String v)  { this.branch = v; }

    public String  getProgram()         { return program; }
    public void    setProgram(String v) { this.program = v; }

    public Integer getBatchYear()           { return batchYear; }
    public void    setBatchYear(Integer v)  { this.batchYear = v; }

    public Double  getCgpa()            { return cgpa; }
    public void    setCgpa(Double v)    { this.cgpa = v; }

    public Integer getActiveBacklogs()          { return activeBacklogs; }
    public void    setActiveBacklogs(Integer v) { this.activeBacklogs = v; }

    public String  getPlacementTier()             { return placementTier; }
    public void    setPlacementTier(String v)     { this.placementTier = v; }

    public String  getPlacementStatus()           { return placementStatus; }
    public void    setPlacementStatus(String v)   { this.placementStatus = v; }
}