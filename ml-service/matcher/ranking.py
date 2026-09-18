"""Ranks JDs against a single student's skill set, reusing the existing
one-resume-vs-one-JD matching logic per JD.

Not built here: salary matching. Drive.packageLpa exists, but there's no
student-side salary-expectation field in the schema, so a two-sided salary
match isn't possible with current data -- a one-sided version (e.g. scoring
higher packages as universally "better") would be a fabricated preference,
not a real match signal. Documented as a future extension, pending a
student-side field to compare against.
"""
from datetime import date

from .similarity import compute_score, match_skills

# rank_drives_multi_feature weights -- must sum to 1.0.
SKILL_WEIGHT = 0.6
ELIGIBILITY_WEIGHT = 0.15
FRESHNESS_WEIGHT = 0.1
POPULARITY_WEIGHT = 0.15

# CGPA margin (student.cgpa - drive.min_cgpa) at or above which
# eligibility_margin_score saturates at 100.
MAX_ELIGIBILITY_MARGIN = 3.0

# Freshness stays at 100 inside this window, decays linearly to 0 by
# FRESHNESS_ZERO_DAYS.
FRESHNESS_FULL_DAYS = 30
FRESHNESS_ZERO_DAYS = 90


def rank_jds_for_student(student_skills: list[str], jds: list[dict]) -> list[dict]:
    """Score each JD against student_skills and sort descending by match_score.

    Each jd dict must have at least {"jd_id": ..., "skills": [...]}. The
    returned dicts are the same jd dicts augmented with "match_score",
    "matched_skills", and "missing_skills".
    """
    ranked = []
    for jd in jds:
        result = match_skills(jd["skills"], student_skills)
        score = compute_score(result["matched"], jd["skills"])
        ranked.append({
            **jd,
            "match_score": score,
            "matched_skills": result["matched"],
            "missing_skills": result["missing"],
        })
    ranked.sort(key=lambda jd: jd["match_score"], reverse=True)
    return ranked


def _freshness_score(drive_date: date, today: date) -> float:
    """100 within FRESHNESS_FULL_DAYS, decaying linearly to 0 by
    FRESHNESS_ZERO_DAYS. Past-due dates are treated as day 0 (freshest)."""
    days_until = max((drive_date - today).days, 0)
    if days_until <= FRESHNESS_FULL_DAYS:
        return 100.0
    if days_until >= FRESHNESS_ZERO_DAYS:
        return 0.0
    span = FRESHNESS_ZERO_DAYS - FRESHNESS_FULL_DAYS
    return (FRESHNESS_ZERO_DAYS - days_until) / span * 100


def rank_drives_multi_feature(student: dict, drives: list[dict], today: date | None = None) -> list[dict]:
    """Rank drives (already past Stage 1 filtering) on a weighted blend of
    skill match, CGPA eligibility margin, drive-date freshness, and relative
    application popularity.

    student: {"skills": [...], "cgpa": float}
    Each drive: {"drive_id": ..., "jd_skills": [...], "min_cgpa": float,
    "drive_date": date | "YYYY-MM-DD", "application_count": int}. Returned
    dicts are the same drives augmented with "rank_score" and each
    individual feature score (0-100), so the breakdown is inspectable.
    """
    if today is None:
        today = date.today()

    max_applications = max((d.get("application_count", 0) for d in drives), default=0)

    ranked = []
    for drive in drives:
        skill_result = match_skills(drive["jd_skills"], student["skills"])
        skill_score = compute_score(skill_result["matched"], drive["jd_skills"])

        margin = (student["cgpa"] - drive["min_cgpa"]) / MAX_ELIGIBILITY_MARGIN
        eligibility_margin_score = round(min(max(margin, 0.0), 1.0) * 100)

        drive_date = drive["drive_date"]
        if isinstance(drive_date, str):
            drive_date = date.fromisoformat(drive_date)
        freshness_score = round(_freshness_score(drive_date, today))

        application_count = drive.get("application_count", 0)
        popularity_score = round(application_count / max_applications * 100) if max_applications > 0 else 0

        rank_score = round(
            SKILL_WEIGHT * skill_score
            + ELIGIBILITY_WEIGHT * eligibility_margin_score
            + FRESHNESS_WEIGHT * freshness_score
            + POPULARITY_WEIGHT * popularity_score,
            2,
        )

        ranked.append({
            **drive,
            "rank_score": rank_score,
            "skill_score": skill_score,
            "matched_skills": skill_result["matched"],
            "missing_skills": skill_result["missing"],
            "eligibility_margin_score": eligibility_margin_score,
            "freshness_score": freshness_score,
            "popularity_score": popularity_score,
        })

    ranked.sort(key=lambda d: d["rank_score"], reverse=True)
    return ranked
