package com.pmrs.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PlacementOfferDTO {

    private Integer    applicationId;
    private String     companyName;
    private String     companyTier;
    private String     roleOffered;
    private BigDecimal packageLpa;
    private LocalDate  selectionDate;
    private boolean    currentOffer;

    public PlacementOfferDTO() {}

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

    public LocalDate  getSelectionDate()              { return selectionDate; }
    public void       setSelectionDate(LocalDate v)   { this.selectionDate = v; }

    public boolean    isCurrentOffer()                { return currentOffer; }
    public void       setCurrentOffer(boolean v)      { this.currentOffer = v; }
}
