"""Ranks JDs against a single student's skill set, reusing the existing
one-resume-vs-one-JD matching logic per JD."""
from .similarity import compute_score, match_skills


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
