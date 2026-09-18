from datetime import date, timedelta

from .ranking import rank_drives_multi_feature

STUDENT = {"skills": ["Python", "SQL"], "cgpa": 8.0}
TODAY = date(2026, 1, 1)


def test_higher_popularity_and_freshness_can_outrank_higher_skill_score():
    # A: marginally higher skill_score (2/3 matched), but far out and unpopular.
    drive_a = {
        "drive_id": "A_higher_skill",
        "jd_skills": ["Python", "SQL", "Java"],
        "min_cgpa": 7.0,
        "drive_date": TODAY + timedelta(days=60),
        "application_count": 1,
    }
    # B: lower skill_score (2/4 matched), but imminent and heavily applied-to.
    drive_b = {
        "drive_id": "B_lower_skill_more_popular_fresher",
        "jd_skills": ["Python", "SQL", "Java", "Go"],
        "min_cgpa": 7.0,
        "drive_date": TODAY + timedelta(days=5),
        "application_count": 50,
    }

    ranked = rank_drives_multi_feature(STUDENT, [drive_a, drive_b], today=TODAY)

    by_id = {d["drive_id"]: d for d in ranked}
    assert by_id["A_higher_skill"]["skill_score"] > by_id["B_lower_skill_more_popular_fresher"]["skill_score"]

    # Despite A's higher skill_score, B's freshness + popularity flip the
    # overall ranking -- this is the point of the multi-feature upgrade.
    assert ranked[0]["drive_id"] == "B_lower_skill_more_popular_fresher"
    assert ranked[0]["rank_score"] > ranked[1]["rank_score"]


def test_rank_score_breakdown_is_present_per_drive():
    drive = {
        "drive_id": "solo",
        "jd_skills": ["Python"],
        "min_cgpa": 7.0,
        "drive_date": TODAY,
        "application_count": 5,
    }
    ranked = rank_drives_multi_feature(STUDENT, [drive], today=TODAY)
    result = ranked[0]
    for key in ("rank_score", "skill_score", "eligibility_margin_score", "freshness_score", "popularity_score"):
        assert key in result
