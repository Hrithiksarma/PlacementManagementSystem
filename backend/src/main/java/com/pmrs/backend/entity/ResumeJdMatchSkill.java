package com.pmrs.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "ResumeJdMatchSkills")
public class ResumeJdMatchSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "skill_id")
    private Integer skillId;

    @NotNull(message = "Match is required")
    @ManyToOne
    @JoinColumn(name = "match_id")
    private ResumeJdMatch match;

    @NotBlank(message = "Skill name is required")
    @Column(name = "skill_name")
    private String skillName;

    /** "MATCHED" or "MISSING". */
    @NotBlank(message = "Status is required")
    @Column(name = "status")
    private String status;

    /** The resume skill this JD skill was matched against — set only when status is MATCHED. */
    @Column(name = "matched_resume_skill")
    private String matchedResumeSkill;

    public Integer getSkillId() { return skillId; }
    public void setSkillId(Integer skillId) { this.skillId = skillId; }

    public ResumeJdMatch getMatch() { return match; }
    public void setMatch(ResumeJdMatch match) { this.match = match; }

    public String getSkillName() { return skillName; }
    public void setSkillName(String skillName) { this.skillName = skillName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMatchedResumeSkill() { return matchedResumeSkill; }
    public void setMatchedResumeSkill(String matchedResumeSkill) { this.matchedResumeSkill = matchedResumeSkill; }
}
