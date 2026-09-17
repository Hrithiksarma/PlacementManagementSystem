from .ranking import rank_jds_for_student

STUDENT_SKILLS = ["Python", "SQL", "React", "Docker"]

JDS = [
    {"jd_id": "full-overlap", "skills": ["Python", "SQL"]},
    {"jd_id": "partial-overlap", "skills": ["Python", "Java", "Kubernetes"]},
    {"jd_id": "no-overlap", "skills": ["Ruby", "Scala"]},
    {"jd_id": "empty-skills", "skills": []},
]


def test_rank_jds_for_student_sorts_descending_by_match_score():
    ranked = rank_jds_for_student(STUDENT_SKILLS, JDS)

    scores = [jd["match_score"] for jd in ranked]
    assert scores == sorted(scores, reverse=True)

    ranked_ids = {jd["jd_id"] for jd in ranked}
    assert ranked_ids == {jd["jd_id"] for jd in JDS}
    for jd in ranked:
        assert "matched_skills" in jd
        assert "missing_skills" in jd


def test_rank_jds_for_student_empty_jds_returns_empty_list():
    assert rank_jds_for_student(STUDENT_SKILLS, []) == []


def test_rank_jds_for_student_empty_skills_jd_scores_zero():
    ranked = rank_jds_for_student(STUDENT_SKILLS, JDS)
    empty_skills_jd = next(jd for jd in ranked if jd["jd_id"] == "empty-skills")
    assert empty_skills_jd["match_score"] == 0
