package com.pmrs.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/** Mirrors ml-service's POST /analyze response shape (snake_case JSON fields). */
public class MlServiceAnalyzeResponse {

    private int score;

    @JsonProperty("matched_skills")
    private List<MatchedSkill> matchedSkills;

    @JsonProperty("missing_skills")
    private List<String> missingSkills;

    @JsonProperty("write_up")
    private String writeUp;

    @JsonProperty("interview_questions")
    private List<String> interviewQuestions;

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public List<MatchedSkill> getMatchedSkills() { return matchedSkills; }
    public void setMatchedSkills(List<MatchedSkill> matchedSkills) { this.matchedSkills = matchedSkills; }

    public List<String> getMissingSkills() { return missingSkills; }
    public void setMissingSkills(List<String> missingSkills) { this.missingSkills = missingSkills; }

    public String getWriteUp() { return writeUp; }
    public void setWriteUp(String writeUp) { this.writeUp = writeUp; }

    public List<String> getInterviewQuestions() { return interviewQuestions; }
    public void setInterviewQuestions(List<String> interviewQuestions) { this.interviewQuestions = interviewQuestions; }

    public static class MatchedSkill {
        @JsonProperty("jd_skill")
        private String jdSkill;

        @JsonProperty("resume_skill")
        private String resumeSkill;

        public String getJdSkill() { return jdSkill; }
        public void setJdSkill(String jdSkill) { this.jdSkill = jdSkill; }

        public String getResumeSkill() { return resumeSkill; }
        public void setResumeSkill(String resumeSkill) { this.resumeSkill = resumeSkill; }
    }
}
