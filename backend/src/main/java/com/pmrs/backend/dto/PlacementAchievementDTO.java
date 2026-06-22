package com.pmrs.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PlacementAchievementDTO {

    private String     studentName;
    private String     companyName;
    private String     companyTier;
    private String     roleOffered;
    private BigDecimal packageLpa;
    private LocalDate  selectionDate;

    public String     getStudentName()               { return studentName; }
    public void       setStudentName(String v)        { this.studentName = v; }

    public String     getCompanyName()                { return companyName; }
    public void       setCompanyName(String v)        { this.companyName = v; }

    public String     getCompanyTier()                { return companyTier; }
    public void       setCompanyTier(String v)        { this.companyTier = v; }

    public String     getRoleOffered()                { return roleOffered; }
    public void       setRoleOffered(String v)        { this.roleOffered = v; }

    public BigDecimal getPackageLpa()                 { return packageLpa; }
    public void       setPackageLpa(BigDecimal v)     { this.packageLpa = v; }

    public LocalDate  getSelectionDate()              { return selectionDate; }
    public void       setSelectionDate(LocalDate v)   { this.selectionDate = v; }
}
