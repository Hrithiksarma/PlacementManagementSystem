package com.pmrs.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class EligibleDriveDTO {

    private Integer    driveId;
    private String     companyName;
    private String     companyTier;
    private String     companyWebsite;
    private String     roleOffered;
    private BigDecimal packageLpa;
    private Double     minCgpa;
    private Integer    maxBacklogs;
    private LocalDate  driveDate;
    private String     driveStatus;
    private boolean    alreadyApplied;
    private boolean    eligible = true;
    private String     eligibilityReason;
    private boolean    upgradationBlocked;

    public EligibleDriveDTO() {}

    public Integer    getDriveId()                        { return driveId; }
    public void       setDriveId(Integer v)               { this.driveId = v; }

    public String     getCompanyName()                    { return companyName; }
    public void       setCompanyName(String v)            { this.companyName = v; }

    public String     getCompanyTier()                    { return companyTier; }
    public void       setCompanyTier(String v)            { this.companyTier = v; }

    public String     getCompanyWebsite()                 { return companyWebsite; }
    public void       setCompanyWebsite(String v)         { this.companyWebsite = v; }

    public String     getRoleOffered()                    { return roleOffered; }
    public void       setRoleOffered(String v)            { this.roleOffered = v; }

    public BigDecimal getPackageLpa()                     { return packageLpa; }
    public void       setPackageLpa(BigDecimal v)         { this.packageLpa = v; }

    public Double     getMinCgpa()                        { return minCgpa; }
    public void       setMinCgpa(Double v)                { this.minCgpa = v; }

    public Integer    getMaxBacklogs()                    { return maxBacklogs; }
    public void       setMaxBacklogs(Integer v)           { this.maxBacklogs = v; }

    public LocalDate  getDriveDate()                      { return driveDate; }
    public void       setDriveDate(LocalDate v)           { this.driveDate = v; }

    public String     getDriveStatus()                    { return driveStatus; }
    public void       setDriveStatus(String v)            { this.driveStatus = v; }

    public boolean    isAlreadyApplied()                  { return alreadyApplied; }
    public void       setAlreadyApplied(boolean v)        { this.alreadyApplied = v; }

    public boolean    isEligible()                        { return eligible; }
    public void       setEligible(boolean v)              { this.eligible = v; }

    public String     getEligibilityReason()              { return eligibilityReason; }
    public void       setEligibilityReason(String v)      { this.eligibilityReason = v; }

    public boolean    isUpgradationBlocked()               { return upgradationBlocked; }
    public void       setUpgradationBlocked(boolean v)     { this.upgradationBlocked = v; }
}
