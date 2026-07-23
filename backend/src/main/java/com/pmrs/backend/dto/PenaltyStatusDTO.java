package com.pmrs.backend.dto;

import java.time.LocalDate;

/**
 * A student's current standing with respect to placement penalties —
 * the resolved answer to "can this student apply to drives right now?".
 */
public class PenaltyStatusDTO {

    private boolean   barred;
    private String    penaltyType;       // strongest active: PERMANENT_BAN > TIME_BAN > SKIP_DRIVES
    private String    message;           // human-readable explanation for banners / errors
    private Integer   drivesRemaining;   // total across active SKIP_DRIVES penalties
    private LocalDate barUntilDate;      // latest active TIME_BAN end date
    private boolean   disciplinaryReferral;

    public boolean   isBarred()                      { return barred; }
    public void      setBarred(boolean v)            { this.barred = v; }

    public String    getPenaltyType()                { return penaltyType; }
    public void      setPenaltyType(String v)        { this.penaltyType = v; }

    public String    getMessage()                    { return message; }
    public void      setMessage(String v)            { this.message = v; }

    public Integer   getDrivesRemaining()            { return drivesRemaining; }
    public void      setDrivesRemaining(Integer v)   { this.drivesRemaining = v; }

    public LocalDate getBarUntilDate()               { return barUntilDate; }
    public void      setBarUntilDate(LocalDate v)    { this.barUntilDate = v; }

    public boolean   isDisciplinaryReferral()        { return disciplinaryReferral; }
    public void      setDisciplinaryReferral(boolean v) { this.disciplinaryReferral = v; }
}
