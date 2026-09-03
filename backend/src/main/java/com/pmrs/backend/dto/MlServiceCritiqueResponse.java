package com.pmrs.backend.dto;

import java.util.List;

/** Mirrors ml-service's POST /critique-resume response shape (already camelCase-free JSON). */
public class MlServiceCritiqueResponse {

    private String overall;
    private List<String> strengths;
    private List<String> improvements;

    public String getOverall() { return overall; }
    public void setOverall(String overall) { this.overall = overall; }

    public List<String> getStrengths() { return strengths; }
    public void setStrengths(List<String> strengths) { this.strengths = strengths; }

    public List<String> getImprovements() { return improvements; }
    public void setImprovements(List<String> improvements) { this.improvements = improvements; }
}
