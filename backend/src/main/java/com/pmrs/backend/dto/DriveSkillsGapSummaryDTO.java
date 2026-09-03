package com.pmrs.backend.dto;

import java.util.List;

public class DriveSkillsGapSummaryDTO {

    private Integer driveId;
    private int totalAnalyses;
    private List<SkillCountDTO> topMissingSkills;
    private List<SkillCountDTO> topMatchedSkills;

    public DriveSkillsGapSummaryDTO() {}

    public Integer             getDriveId()                            { return driveId; }
    public void                setDriveId(Integer v)                   { this.driveId = v; }

    public int                 getTotalAnalyses()                       { return totalAnalyses; }
    public void                setTotalAnalyses(int v)                  { this.totalAnalyses = v; }

    public List<SkillCountDTO> getTopMissingSkills()                    { return topMissingSkills; }
    public void                setTopMissingSkills(List<SkillCountDTO> v) { this.topMissingSkills = v; }

    public List<SkillCountDTO> getTopMatchedSkills()                    { return topMatchedSkills; }
    public void                setTopMatchedSkills(List<SkillCountDTO> v) { this.topMatchedSkills = v; }

    public static class SkillCountDTO {
        private String skillName;
        private long count;

        public SkillCountDTO() {}

        public SkillCountDTO(String skillName, long count) {
            this.skillName = skillName;
            this.count = count;
        }

        public String getSkillName()          { return skillName; }
        public void   setSkillName(String v)  { this.skillName = v; }

        public long   getCount()              { return count; }
        public void   setCount(long v)        { this.count = v; }
    }
}
