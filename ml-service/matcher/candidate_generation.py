"""Stage 1 of ranking: cheap structured filtering to cut the candidate pool
before the expensive embedding-based scoring in similarity.py/ranking.py runs.
No embeddings here on purpose."""

# Drives in these statuses don't accept applications. Mirrors the real rule in
# StudentPortalServiceImpl.applyToDrive (backend/src/main/java/com/pmrs/backend/
# service/StudentPortalServiceImpl.java) -- there is no single "Open" status;
# the backend instead blocks Cancelled/Completed and allows everything else
# (Upcoming, Active).
CLOSED_STATUSES = {"cancelled", "completed"}


def filter_eligible_drives(student: dict, drives: list[dict]) -> list[dict]:
    """Keep only drives the student is structurally eligible for: CGPA meets
    the drive's minimum, backlogs are within the drive's maximum, and the
    drive is still accepting applications."""
    eligible = []
    for drive in drives:
        if drive["status"].lower() in CLOSED_STATUSES:
            continue
        if student["cgpa"] < drive["min_cgpa"]:
            continue
        if student["active_backlogs"] > drive["max_backlogs"]:
            continue
        eligible.append(drive)
    return eligible
