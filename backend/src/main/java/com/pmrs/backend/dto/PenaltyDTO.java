package com.pmrs.backend.dto;

import java.time.LocalDate;

/** Flat view of a PlacementPenalty row for the admin Penalties page. */
public class PenaltyDTO {

    private Integer   penaltyId;
    private Integer   studentId;
    private String    studentName;
    private String    rollNo;
    private String    penaltyType;
    private Integer   drivesRemaining;
    private LocalDate barUntilDate;
    private String    reason;
    private String    issuedBy;
    private boolean   active;
    private boolean   disciplinaryReferral;
    private LocalDate createdAt;
    private String    liftedBy;
    private LocalDate liftedAt;

    public Integer   getPenaltyId()                  { return penaltyId; }
    public void      setPenaltyId(Integer v)         { this.penaltyId = v; }

    public Integer   getStudentId()                  { return studentId; }
    public void      setStudentId(Integer v)         { this.studentId = v; }

    public String    getStudentName()                { return studentName; }
    public void      setStudentName(String v)        { this.studentName = v; }

    public String    getRollNo()                     { return rollNo; }
    public void      setRollNo(String v)             { this.rollNo = v; }

    public String    getPenaltyType()                { return penaltyType; }
    public void      setPenaltyType(String v)        { this.penaltyType = v; }

    public Integer   getDrivesRemaining()            { return drivesRemaining; }
    public void      setDrivesRemaining(Integer v)   { this.drivesRemaining = v; }

    public LocalDate getBarUntilDate()               { return barUntilDate; }
    public void      setBarUntilDate(LocalDate v)    { this.barUntilDate = v; }

    public String    getReason()                     { return reason; }
    public void      setReason(String v)             { this.reason = v; }

    public String    getIssuedBy()                   { return issuedBy; }
    public void      setIssuedBy(String v)           { this.issuedBy = v; }

    public boolean   isActive()                      { return active; }
    public void      setActive(boolean v)            { this.active = v; }

    public boolean   isDisciplinaryReferral()        { return disciplinaryReferral; }
    public void      setDisciplinaryReferral(boolean v) { this.disciplinaryReferral = v; }

    public LocalDate getCreatedAt()                  { return createdAt; }
    public void      setCreatedAt(LocalDate v)       { this.createdAt = v; }

    public String    getLiftedBy()                   { return liftedBy; }
    public void      setLiftedBy(String v)           { this.liftedBy = v; }

    public LocalDate getLiftedAt()                   { return liftedAt; }
    public void      setLiftedAt(LocalDate v)        { this.liftedAt = v; }
}
