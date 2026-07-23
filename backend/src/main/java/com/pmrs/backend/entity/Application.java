package com.pmrs.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "Applications")
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "application_id")
    private Integer applicationId;

    @NotNull(message = "Student is required")
    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    @NotNull(message = "Drive is required")
    @ManyToOne
    @JoinColumn(name = "drive_id")
    private Drive drive;

    @NotBlank(message = "Status is required")
    @Column(name = "status", nullable = false)
    private String status = "Applied";

    @PrePersist
    public void prePersist() {
        if (status == null || status.trim().isEmpty()) {
            status = "Applied";
        }
    }

    @Column(name = "round")
    private String round;

    @Column(name = "application_date")
    private LocalDate applicationDate;

    @Column(name = "updated_at")
    private LocalDate updatedAt;

    @Column(name = "selected_at")
    private LocalDateTime selectedAt;

    @Column(name = "offer_released_at")
    private LocalDateTime offerReleasedAt;

    @Column(name = "offer_accepted_at")
    private LocalDateTime offerAcceptedAt;

    @Column(name = "offer_rejected_at")
    private LocalDateTime offerRejectedAt;

    @Column(name = "offer_deadline")
    private LocalDate offerDeadline;

    @Column(name = "selection_email_sent")
    private Boolean selectionEmailSent = false;

    public Integer getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Integer applicationId) {
        this.applicationId = applicationId;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public Drive getDrive() {
        return drive;
    }

    public void setDrive(Drive drive) {
        this.drive = drive;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRound() {
        return round;
    }

    public void setRound(String round) {
        this.round = round;
    }

    public LocalDate getApplicationDate() {
        return applicationDate;
    }

    public void setApplicationDate(LocalDate applicationDate) {
        this.applicationDate = applicationDate;
    }

    public LocalDate getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDate updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getSelectedAt()                            { return selectedAt; }
    public void          setSelectedAt(LocalDateTime v)             { this.selectedAt = v; }

    public LocalDateTime getOfferReleasedAt()                       { return offerReleasedAt; }
    public void          setOfferReleasedAt(LocalDateTime v)        { this.offerReleasedAt = v; }

    public LocalDateTime getOfferAcceptedAt()                       { return offerAcceptedAt; }
    public void          setOfferAcceptedAt(LocalDateTime v)        { this.offerAcceptedAt = v; }

    public LocalDateTime getOfferRejectedAt()                       { return offerRejectedAt; }
    public void          setOfferRejectedAt(LocalDateTime v)        { this.offerRejectedAt = v; }

    public LocalDate     getOfferDeadline()                         { return offerDeadline; }
    public void          setOfferDeadline(LocalDate v)              { this.offerDeadline = v; }

    public Boolean       getSelectionEmailSent()                    { return selectionEmailSent; }
    public void          setSelectionEmailSent(Boolean v)           { this.selectionEmailSent = v; }
}
