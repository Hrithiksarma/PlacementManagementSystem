package com.pmrs.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ResumeJdMatchResultDTO {

    private Integer   matchId;
    private Integer   score;
    private String    writeUp;
    private List<String> interviewQuestions;
    private LocalDateTime createdAt;
    private List<SkillDTO> skills;

    public ResumeJdMatchResultDTO() {}

    public Integer        getMatchId()                    { return matchId; }
    public void           setMatchId(Integer v)            { this.matchId = v; }

    public Integer        getScore()                       { return score; }
    public void           setScore(Integer v)               { this.score = v; }

    public String         getWriteUp()                      { return writeUp; }
    public void           setWriteUp(String v)              { this.writeUp = v; }

    public List<String>   getInterviewQuestions()           { return interviewQuestions; }
    public void           setInterviewQuestions(List<String> v) { this.interviewQuestions = v; }

    public LocalDateTime  getCreatedAt()                    { return createdAt; }
    public void           setCreatedAt(LocalDateTime v)     { this.createdAt = v; }

    public List<SkillDTO> getSkills()                        { return skills; }
    public void           setSkills(List<SkillDTO> v)        { this.skills = v; }

    public static class SkillDTO {
        private String skillName;
        private String status;
        private String matchedResumeSkill;

        public SkillDTO() {}

        public SkillDTO(String skillName, String status, String matchedResumeSkill) {
            this.skillName = skillName;
            this.status = status;
            this.matchedResumeSkill = matchedResumeSkill;
        }

        public String getSkillName()                          { return skillName; }
        public void   setSkillName(String v)                  { this.skillName = v; }

        public String getStatus()                              { return status; }
        public void   setStatus(String v)                      { this.status = v; }

        public String getMatchedResumeSkill()                  { return matchedResumeSkill; }
        public void   setMatchedResumeSkill(String v)          { this.matchedResumeSkill = v; }
    }
}
