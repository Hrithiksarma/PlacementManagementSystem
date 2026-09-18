# Personalization — Multi-Feature Drive Ranker

Model-card-style notes on the one personalization signal in `rank_drives_multi_feature` (`ml-service/matcher/ranking.py`): what real data backs it, what it doesn't cover, and why. Same dated, decision-and-rationale format as [`decisions.md`](decisions.md); see [`ranking_evaluation.md`](ranking_evaluation.md) for the separate precision/NDCG evaluation of the underlying skill matcher.

---

## 2026-09-18 — Sector affinity as the one personalization signal

**Decision:** `compute_sector_affinity` (`ml-service/matcher/ranking.py`) scores a drive by what fraction of a student's *actual past applications* were to companies in the same `sector` as that drive, and feeds it into `rank_drives_multi_feature` as a 5th weighted feature (`sector_affinity_score`, weight 0.1). The other four weights were scaled down proportionally (×0.9) to make room, preserving their relative balance: skill 0.6→0.54, eligibility 0.15→0.135, freshness 0.1→0.09, popularity 0.15→0.135.

**Alternatives considered:** location preference, salary preference, "experience" preference.

**Why not the alternatives:** none of them have a backing field in the schema. `Student` has no location or salary-expectation column; `Drive.packageLpa` exists but is one-sided (a job's pay, not what a student wants), so scoring it as a match would mean fabricating a preference the student never stated. Building these anyway would produce a personalization signal that looks real but isn't grounded in any actual student behavior or stated preference. They're **documented here as future extensions that need new data collection** (e.g. a location-preference field on `Student`, an expected-salary range) — not silently dropped, not faked with a placeholder.

**Why sector affinity is real, not fabricated:** it's derived entirely from `Application` rows the student actually created (`Application.student_id` → `Application.drive_id` → `Drive.company` → `Company.sector`), i.e. revealed behavior, not a guessed or declared preference. A student who's applied to three IT-sector drives and zero Core Engineering drives has a real, observable lean toward IT — that's the only personalization signal in this schema with genuine data behind it.

## Cold-start limitation

A student with no prior applications (new students, first drive of the season, or anyone who hasn't applied yet) has `application_history = []`, and `compute_sector_affinity` returns exactly `0.0` for every drive — never a crash, never a guessed default. This means **sector affinity contributes nothing to their ranking**, not a penalty relative to students with history: a cold-start student's ranking is driven entirely by the other four features (skill match, eligibility margin, freshness, popularity), which is the intended, safe fallback. It does mean personalization only helps students who've already applied to something — by construction, it can't help on someone's very first look at the platform.

## Reproducing

```
cd ml-service
python -m pytest matcher/test_ranking_multi_feature.py -v
```
