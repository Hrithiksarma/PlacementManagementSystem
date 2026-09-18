from .candidate_generation import filter_eligible_drives

STUDENT = {"cgpa": 7.5, "active_backlogs": 1, "department": "CSE"}

DRIVES = [
    {"drive_id": 1, "min_cgpa": 7.0, "max_backlogs": 2, "status": "Upcoming", "jd_skills": ["Python"]},
    {"drive_id": 2, "min_cgpa": 8.0, "max_backlogs": 2, "status": "Upcoming", "jd_skills": ["Python"]},
    {"drive_id": 3, "min_cgpa": 7.0, "max_backlogs": 0, "status": "Upcoming", "jd_skills": ["Python"]},
    {"drive_id": 4, "min_cgpa": 7.0, "max_backlogs": 2, "status": "Completed", "jd_skills": ["Python"]},
    {"drive_id": 5, "min_cgpa": 7.0, "max_backlogs": 2, "status": "Cancelled", "jd_skills": ["Python"]},
    {"drive_id": 6, "min_cgpa": 7.0, "max_backlogs": 2, "status": "Active", "jd_skills": ["Python"]},
]


def test_keeps_eligible_drives():
    eligible_ids = {d["drive_id"] for d in filter_eligible_drives(STUDENT, DRIVES)}
    assert eligible_ids == {1, 6}


def test_filters_out_cgpa_below_minimum():
    result = filter_eligible_drives(STUDENT, [DRIVES[1]])
    assert result == []


def test_filters_out_too_many_backlogs():
    result = filter_eligible_drives(STUDENT, [DRIVES[2]])
    assert result == []


def test_filters_out_closed_statuses():
    result = filter_eligible_drives(STUDENT, [DRIVES[3], DRIVES[4]])
    assert result == []
