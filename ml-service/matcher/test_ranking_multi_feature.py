from datetime import date, timedelta

from .ranking import compute_sector_affinity, rank_drives_multi_feature

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
    for key in ("rank_score", "skill_score", "eligibility_margin_score", "freshness_score",
                "popularity_score", "sector_affinity_score"):
        assert key in result


def test_compute_sector_affinity_cold_start_returns_zero():
    assert compute_sector_affinity(student_id=1, application_history=[], drive_sector="IT") == 0.0


def test_compute_sector_affinity_is_fraction_of_matching_history():
    history = [
        {"drive_id": 1, "company_sector": "IT"},
        {"drive_id": 2, "company_sector": "IT"},
        {"drive_id": 3, "company_sector": "Finance"},
        {"drive_id": 4, "company_sector": "IT"},
    ]
    assert compute_sector_affinity(1, history, "IT") == 0.75
    assert compute_sector_affinity(1, history, "Finance") == 0.25
    assert compute_sector_affinity(1, history, "Consulting") == 0.0


def test_sector_history_ranks_same_sector_drive_higher():
    # Same skills/CGPA/eligibility/freshness/popularity on both drives --
    # only company_sector differs, isolating the sector-affinity effect.
    drive_it = {
        "drive_id": "it_drive",
        "jd_skills": ["Python"],
        "min_cgpa": 7.0,
        "drive_date": TODAY,
        "application_count": 10,
        "company_sector": "IT",
    }
    drive_core_eng = {
        "drive_id": "core_eng_drive",
        "jd_skills": ["Python"],
        "min_cgpa": 7.0,
        "drive_date": TODAY,
        "application_count": 10,
        "company_sector": "Core Engineering",
    }

    student_with_it_history = {
        **STUDENT,
        "application_history": [
            {"drive_id": 901, "company_sector": "IT"},
            {"drive_id": 902, "company_sector": "IT"},
            {"drive_id": 903, "company_sector": "IT"},
        ],
    }

    ranked = rank_drives_multi_feature(student_with_it_history, [drive_it, drive_core_eng], today=TODAY)

    assert ranked[0]["drive_id"] == "it_drive"
    assert ranked[0]["sector_affinity_score"] == 100
    assert ranked[1]["sector_affinity_score"] == 0
    assert ranked[0]["rank_score"] > ranked[1]["rank_score"]


def test_sector_affinity_cold_start_does_not_crash_or_bias_ranking():
    drive_it = {
        "drive_id": "it_drive",
        "jd_skills": ["Python"],
        "min_cgpa": 7.0,
        "drive_date": TODAY,
        "application_count": 10,
        "company_sector": "IT",
    }
    drive_core_eng = {
        "drive_id": "core_eng_drive",
        "jd_skills": ["Python"],
        "min_cgpa": 7.0,
        "drive_date": TODAY,
        "application_count": 10,
        "company_sector": "Core Engineering",
    }

    student_no_history = {**STUDENT, "application_history": []}

    ranked = rank_drives_multi_feature(student_no_history, [drive_it, drive_core_eng], today=TODAY)

    assert ranked[0]["sector_affinity_score"] == 0
    assert ranked[1]["sector_affinity_score"] == 0
    assert ranked[0]["rank_score"] == ranked[1]["rank_score"]
