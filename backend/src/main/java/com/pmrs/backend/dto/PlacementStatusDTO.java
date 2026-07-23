package com.pmrs.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PlacementStatusDTO {

    private boolean    placed;
    private String     currentStage;
    private String     companyName;
    private String     companyTier;
    private String     roleOffered;
    private BigDecimal packageLpa;
    private LocalDate  placementDate;
    private String     message;

    // Pending offer: a "Selected" app awaiting the student's Accept/Reject decision
    private boolean    hasPendingOffer;
    private Integer    pendingOfferAppId;
    private String     pendingOfferCompany;
    private String     pendingOfferTier;
    private String     pendingOfferRole;
    private BigDecimal pendingOfferPackage;
    private LocalDate  pendingOfferDeadline;

    public PlacementStatusDTO() {}

    public boolean    isPlaced()                          { return placed; }
    public void       setPlaced(boolean v)                { this.placed = v; }

    public String     getCurrentStage()                   { return currentStage; }
    public void       setCurrentStage(String v)           { this.currentStage = v; }

    public String     getCompanyName()                    { return companyName; }
    public void       setCompanyName(String v)            { this.companyName = v; }

    public String     getCompanyTier()                    { return companyTier; }
    public void       setCompanyTier(String v)            { this.companyTier = v; }

    public String     getRoleOffered()                    { return roleOffered; }
    public void       setRoleOffered(String v)            { this.roleOffered = v; }

    public BigDecimal getPackageLpa()                     { return packageLpa; }
    public void       setPackageLpa(BigDecimal v)         { this.packageLpa = v; }

    public LocalDate  getPlacementDate()                  { return placementDate; }
    public void       setPlacementDate(LocalDate v)       { this.placementDate = v; }

    public String     getMessage()                        { return message; }
    public void       setMessage(String v)                { this.message = v; }

    public boolean    isHasPendingOffer()                 { return hasPendingOffer; }
    public void       setHasPendingOffer(boolean v)       { this.hasPendingOffer = v; }

    public Integer    getPendingOfferAppId()               { return pendingOfferAppId; }
    public void       setPendingOfferAppId(Integer v)      { this.pendingOfferAppId = v; }

    public String     getPendingOfferCompany()             { return pendingOfferCompany; }
    public void       setPendingOfferCompany(String v)     { this.pendingOfferCompany = v; }

    public String     getPendingOfferTier()                { return pendingOfferTier; }
    public void       setPendingOfferTier(String v)        { this.pendingOfferTier = v; }

    public String     getPendingOfferRole()                { return pendingOfferRole; }
    public void       setPendingOfferRole(String v)        { this.pendingOfferRole = v; }

    public BigDecimal getPendingOfferPackage()             { return pendingOfferPackage; }
    public void       setPendingOfferPackage(BigDecimal v) { this.pendingOfferPackage = v; }

    public LocalDate  getPendingOfferDeadline()            { return pendingOfferDeadline; }
    public void       setPendingOfferDeadline(LocalDate v) { this.pendingOfferDeadline = v; }
}
