package com.pmrs.backend.dto;

import java.util.List;

public class ResumeCritiqueDTO {

    private String overall;
    private List<String> strengths;
    private List<String> improvements;

    public ResumeCritiqueDTO() {}

    public ResumeCritiqueDTO(String overall, List<String> strengths, List<String> improvements) {
        this.overall = overall;
        this.strengths = strengths;
        this.improvements = improvements;
    }

    public String       getOverall()                    { return overall; }
    public void         setOverall(String v)             { this.overall = v; }

    public List<String> getStrengths()                   { return strengths; }
    public void         setStrengths(List<String> v)     { this.strengths = v; }

    public List<String> getImprovements()                 { return improvements; }
    public void         setImprovements(List<String> v)   { this.improvements = v; }
}
