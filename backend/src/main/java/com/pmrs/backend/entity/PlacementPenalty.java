package com.pmrs.backend.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

/**
 * A disciplinary bar on a student's participation in placement drives.
 *
 * Three kinds:
 *   SKIP_DRIVES   — barred from the next N companies they'd be eligible for
 *                   (drivesRemaining decrements as new eligible drives open);
 *   TIME_BAN      — barred from all drives until barUntilDate;
 *   PERMANENT_BAN — barred from all further drives (decline-after-accept);
 *                   carries the disciplinaryReferral flag.
 *
 * Penalties stack: multiple active rows may exist per student. A penalty is
 * never deleted — it is deactivated when it expires or an admin lifts it.
 */
@Entity
@Table(name = "PlacementPenalties")
public class PlacementPenalty {

    public static final String TYPE_SKIP_DRIVES   = "SKIP_DRIVES";
    public static final String TYPE_TIME_BAN      = "TIME_BAN";
    public static final String TYPE_PERMANENT_BAN = "PERMANENT_BAN";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "penalty_id")
    private Integer penaltyId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "student_id")
    private Student student;

    @Column(name = "penalty_type", nullable = false)
    private String penaltyType;

    @Column(name = "drives_remaining")
    private Integer drivesRemaining;

    @Column(name = "bar_until_date")
    private LocalDate barUntilDate;

    @Column(name = "reason", length = 500)
    private String reason;

    @Column(name = "issued_by", nullable = false)
    private String issuedBy;

    @Column(name = "source_application_id")
    private Integer sourceApplicationId;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "disciplinary_referral", nullable = false)
    private boolean disciplinaryReferral = false;

    @Column(name = "created_at")
    private LocalDate createdAt;

    @Column(name = "lifted_by")
    private String liftedBy;

    @Column(name = "lifted_at")
    private LocalDate liftedAt;

    public Integer getPenaltyId()                     { return penaltyId; }
    public void setPenaltyId(Integer v)               { this.penaltyId = v; }

    public Student getStudent()                       { return student; }
    public void setStudent(Student v)                 { this.student = v; }

    public String getPenaltyType()                    { return penaltyType; }
    public void setPenaltyType(String v)              { this.penaltyType = v; }

    public Integer getDrivesRemaining()               { return drivesRemaining; }
    public void setDrivesRemaining(Integer v)         { this.drivesRemaining = v; }

    public LocalDate getBarUntilDate()                { return barUntilDate; }
    public void setBarUntilDate(LocalDate v)          { this.barUntilDate = v; }

    public String getReason()                         { return reason; }
    public void setReason(String v)                   { this.reason = v; }

    public String getIssuedBy()                       { return issuedBy; }
    public void setIssuedBy(String v)                 { this.issuedBy = v; }

    public Integer getSourceApplicationId()           { return sourceApplicationId; }
    public void setSourceApplicationId(Integer v)     { this.sourceApplicationId = v; }

    public boolean isActive()                         { return active; }
    public void setActive(boolean v)                  { this.active = v; }

    public boolean isDisciplinaryReferral()           { return disciplinaryReferral; }
    public void setDisciplinaryReferral(boolean v)    { this.disciplinaryReferral = v; }

    public LocalDate getCreatedAt()                   { return createdAt; }
    public void setCreatedAt(LocalDate v)             { this.createdAt = v; }

    public String getLiftedBy()                       { return liftedBy; }
    public void setLiftedBy(String v)                 { this.liftedBy = v; }

    public LocalDate getLiftedAt()                    { return liftedAt; }
    public void setLiftedAt(LocalDate v)              { this.liftedAt = v; }
}
