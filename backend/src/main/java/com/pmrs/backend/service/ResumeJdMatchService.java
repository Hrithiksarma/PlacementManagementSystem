package com.pmrs.backend.service;

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
}
