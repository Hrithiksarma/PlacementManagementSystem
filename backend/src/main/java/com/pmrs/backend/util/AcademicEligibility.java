package com.pmrs.backend.util;

import com.pmrs.backend.entity.Drive;
import com.pmrs.backend.entity.EligibilityCriteria;
import com.pmrs.backend.entity.Student;

import java.util.Optional;

/**
 * Academic (CGPA/backlog) eligibility check, shared by every place that
 * needs to know whether a student meets a drive's requirements.
 *
 * <p>{@link EligibilityCriteria} is meant to override a drive's own
 * {@code minCgpa}/{@code maxBacklogs} on a per-department basis, but nothing
 * in this app currently creates criteria rows — the CRUD endpoint exists but
 * is never called from any UI. Without a fallback, "no criteria row" was
 * silently treated as "no CGPA/backlog requirement at all", ignoring the
 * limits set directly on the {@link Drive}. This mirrors the fallback
 * already present in {@code sql/fix_eligibility_trigger.sql}'s DB trigger.
 */
public final class AcademicEligibility {

    private AcademicEligibility() {}

    /** Returns null when eligible, or a human-readable reason when not. */
    public static String checkReason(Student student, Drive drive, Optional<EligibilityCriteria> ec) {
        Double minCgpa = ec.map(EligibilityCriteria::getMinCgpa).orElse(null);
        if (minCgpa == null) {
            minCgpa = drive.getMinCgpa();
        }
        Integer maxBacklogs = ec.map(EligibilityCriteria::getMaxBacklogs).orElse(null);
        if (maxBacklogs == null) {
            maxBacklogs = drive.getMaxBacklogs();
        }

        if (minCgpa != null && (student.getCgpa() == null || student.getCgpa() < minCgpa)) {
            return "Minimum CGPA required is " + minCgpa + ".";
        }
        if (maxBacklogs != null && (student.getActiveBacklogs() == null
                || student.getActiveBacklogs() > maxBacklogs)) {
            return "Maximum allowed backlogs is " + maxBacklogs + ".";
        }
        return null;
    }
}
