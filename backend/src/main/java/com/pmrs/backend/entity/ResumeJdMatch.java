package com.pmrs.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
@Table(name = "ResumeJdMatches")
public class ResumeJdMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "match_id")
    private Integer matchId;

    @NotNull(message = "Student is required")
    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    @NotNull(message = "Drive is required")
    @ManyToOne
    @JoinColumn(name = "drive_id")
    private Drive drive;

    @Column(name = "score", nullable = false)
    private Integer score;

    @Column(name = "write_up", columnDefinition = "TEXT")
    private String writeUp;

    /** Newline-separated, same storage convention as writeUp — a handful of
     *  short questions doesn't warrant a separate table. */
    @Column(name = "interview_questions", columnDefinition = "TEXT")
    private String interviewQuestions;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Integer getMatchId() { return matchId; }
    public void setMatchId(Integer matchId) { this.matchId = matchId; }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public Drive getDrive() { return drive; }
    public void setDrive(Drive drive) { this.drive = drive; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public String getWriteUp() { return writeUp; }
    public void setWriteUp(String writeUp) { this.writeUp = writeUp; }

    public String getInterviewQuestions() { return interviewQuestions; }
    public void setInterviewQuestions(String interviewQuestions) { this.interviewQuestions = interviewQuestions; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
