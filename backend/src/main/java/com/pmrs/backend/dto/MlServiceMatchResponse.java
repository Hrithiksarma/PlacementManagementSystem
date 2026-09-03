package com.pmrs.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/** Mirrors ml-service's POST /match response — same shape as /analyze minus
 * write_up/interview_questions, since /match makes no LLM call. */
public class MlServiceMatchResponse {

    private int score;

    @JsonProperty("matched_skills")
    private List<MlServiceAnalyzeResponse.MatchedSkill> matchedSkills;

    @JsonProperty("missing_skills")
    private List<String> missingSkills;

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public List<MlServiceAnalyzeResponse.MatchedSkill> getMatchedSkills() { return matchedSkills; }
    public void setMatchedSkills(List<MlServiceAnalyzeResponse.MatchedSkill> matchedSkills) { this.matchedSkills = matchedSkills; }

    public List<String> getMissingSkills() { return missingSkills; }
    public void setMissingSkills(List<String> missingSkills) { this.missingSkills = missingSkills; }
}
