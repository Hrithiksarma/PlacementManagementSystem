package com.pmrs.backend.service;

import com.pmrs.backend.dto.DriveSkillsGapSummaryDTO;
import com.pmrs.backend.dto.ResumeCritiqueDTO;
import com.pmrs.backend.dto.ResumeJdMatchResultDTO;
import com.pmrs.backend.entity.Drive;
import com.pmrs.backend.entity.Student;

public interface ResumeJdMatchService {

    /**
     * Ensures the student's resumeTextCache reflects their current resumeUrl —
     * re-extracts and persists only when resumeUrl has changed since the last
     * cached extraction, otherwise this is a no-op. Returns the up-to-date
     * resume text (may be null if extraction isn't possible, e.g. no resume
     * uploaded yet or the file isn't accessible).
     */
    String refreshResumeTextCache(Student student);

    /**
     * Runs a full skills-gap analysis for the logged-in student (resolved from
     * username, same convention as StudentPortalService) against a drive's JD:
     * refreshes the resume text cache, calls ml-service, and persists a new
     * ResumeJdMatch (+ its skill rows). Throws IllegalStateException if either
     * the resume or the JD text isn't available yet.
     */
    ResumeJdMatchResultDTO analyze(String username, Integer driveId);

    /** The most recent persisted match for this student/drive pair, if any (null if none yet). */
    ResumeJdMatchResultDTO getLatestMatch(String username, Integer driveId);

    /**
     * A quick fit score (0-100) between a student's cached resume text and a
     * drive's JD — extraction + matching only, no LLM call, nothing persisted.
     * Returns null if either text isn't available. Cheap enough to call once
     * per eligible drive when building the eligible-drives list.
     */
    Integer computeFitScore(Student student, Drive drive);

    /**
     * Aggregates every persisted ResumeJdMatchSkill for a drive across all
     * students who've analyzed their fit against it — the most commonly
     * missing/matched skills, for officers planning prep sessions. Pure
     * aggregation over already-persisted data, no new ml-service calls.
     */
    DriveSkillsGapSummaryDTO getSkillsGapSummary(Integer driveId);

    /**
     * A standalone resume-quality critique (formatting/clarity/ATS-friendliness),
     * not tied to any drive. One Gemini call via ml-service, nothing persisted —
     * computed fresh on every call.
     */
    ResumeCritiqueDTO critiqueResume(String username);
}
