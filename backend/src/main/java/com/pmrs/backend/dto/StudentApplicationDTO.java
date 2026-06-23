package com.pmrs.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class StudentApplicationDTO {

    private Integer   applicationId;
    private String    companyName;
    private String    companyTier;
    private String    roleOffered;
    private BigDecimal packageLpa;
    private String    status;
    private LocalDate applicationDate;
    private LocalDate  driveDate;
    private String     driveStatus;
    private LocalDate  offerDeadline;

    public StudentApplicationDTO() {}

    public Integer    getApplicationId()              { return applicationId; }
    public void       setApplicationId(Integer v)     { this.applicationId = v; }

    public String     getCompanyName()                { return companyName; }
    public void       setCompanyName(String v)        { this.companyName = v; }

    public String     getCompanyTier()                { return companyTier; }
    public void       setCompanyTier(String v)        { this.companyTier = v; }

    public String     getRoleOffered()                { return roleOffered; }
    public void       setRoleOffered(String v)        { this.roleOffered = v; }

    public BigDecimal getPackageLpa()                 { return packageLpa; }
    public void       setPackageLpa(BigDecimal v)     { this.packageLpa = v; }

    public String     getStatus()                     { return status; }
    public void       setStatus(String v)             { this.status = v; }

    public LocalDate  getApplicationDate()            { return applicationDate; }
    public void       setApplicationDate(LocalDate v) { this.applicationDate = v; }

    public LocalDate  getDriveDate()                  { return driveDate; }
    public void       setDriveDate(LocalDate v)       { this.driveDate = v; }

    public String     getDriveStatus()                { return driveStatus; }
    public void       setDriveStatus(String v)        { this.driveStatus = v; }

    public LocalDate  getOfferDeadline()              { return offerDeadline; }
    public void       setOfferDeadline(LocalDate v)   { this.offerDeadline = v; }
}
