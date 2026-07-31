package com.pmrs.backend.dto;

public class HrContactSearchResult {

    private String  source;           // "MANUAL" | "GOOGLE_FORM" | "EXTERNAL"
    private Integer hrId;             // set only for real HRContact rows (enables edit/delete)
    private Integer externalId;       // set only for ExternalHrContact rows (enables edit/delete)
    private Integer submissionId;     // set only for not-yet-included form submissions
    private String  submissionStatus; // "PENDING" | "REJECTED" — only for submission-sourced rows
    private String  name;
    private String  email;
    private String  phone;
    private String  designation;
    private String  companyName;

    public String  getSource()                     { return source; }
    public void    setSource(String v)              { this.source = v; }

    public Integer getHrId()                        { return hrId; }
    public void    setHrId(Integer v)                { this.hrId = v; }

    public Integer getExternalId()                   { return externalId; }
    public void    setExternalId(Integer v)           { this.externalId = v; }

    public Integer getSubmissionId()                 { return submissionId; }
    public void    setSubmissionId(Integer v)        { this.submissionId = v; }

    public String  getSubmissionStatus()             { return submissionStatus; }
    public void    setSubmissionStatus(String v)      { this.submissionStatus = v; }

    public String  getName()                         { return name; }
    public void    setName(String v)                  { this.name = v; }

    public String  getEmail()                         { return email; }
    public void    setEmail(String v)                  { this.email = v; }

    public String  getPhone()                         { return phone; }
    public void    setPhone(String v)                  { this.phone = v; }

    public String  getDesignation()                   { return designation; }
    public void    setDesignation(String v)            { this.designation = v; }

    public String  getCompanyName()                    { return companyName; }
    public void    setCompanyName(String v)             { this.companyName = v; }
}
