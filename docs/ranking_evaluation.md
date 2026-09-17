# Ranking Evaluation — Resume/JD Skills-Gap Matcher

How `rank_jds_for_student` (`ml-service/matcher/ranking.py`) was evaluated against a small hand-labeled relevance set, and what the resulting numbers do and don't show. See [`decisions.md`](decisions.md) for the dashboard's decision log and [`flow.md`](flow.md) for the request-flow trace — this document is neither; it's an evaluation report for one ML component.

---

## Method

- **Data:** `ml-service/matcher/eval_data.py` defines 5 student skill profiles (full-stack, ML, Java backend, cybersecurity, DevOps/cloud) and 8 JD skill profiles spanning the same domains, all skills drawn from the canonical `data/skills_taxonomy.json` entries.
- **Labels:** `STUDENT_JD_RELEVANCE` grades every (student, JD) pair 0 (not relevant) / 1 (somewhat relevant) / 2 (strongly relevant), judged from the two skill lists directly — not derived from the matcher's own output.
- **Metrics:** `ml-service/matcher/evaluate_ranking.py` calls `rank_jds_for_student` per student, then computes, over the top-k of that ranking:
  - **Precision@k** — fraction of the top-k JDs with label ≥ 1.
  - **NDCG@k** — standard graded-relevance formula, `DCG = Σ rel_i / log2(i+1)` for `i = 1..k`, normalized by the ideal DCG (labels sorted descending). A ranker that places all relevant JDs first, in the right relative order, scores 1.0.

## Sample size and annotator caveat

This is a **5-student × 8-JD hand-labeled set (40 pairs), labeled by a single annotator** (Claude, at the repo owner's direction, applying an explicit rule of thumb: 2 = strong overlap on a JD's core/defining skills, 1 = one genuinely transferable shared skill even with a different core stack, 0 = no meaningful overlap). It is not a validated ground truth, not double-annotated, and not drawn from real student/JD data — treat every number below as indicative of whether the ranking logic is doing something reasonable on a clean, well-separated toy set, not as a generalizable quality benchmark.

Two labels were caught and corrected during the first evaluation run by cross-checking labels ≥ 1 against the matcher's raw match score (a label ≥ 1 with a score of exactly 0 signals the annotator saw an overlap that isn't actually in the data): `student_fullstack`×`jd_java_backend` and `student_cybersecurity`×`jd_platform_devops` were both labeled `1` on the mistaken belief that a skill (REST API, Linux respectively) was shared, when it wasn't present in that JD's skill list. Both were corrected to `0` before the results below were produced.

**Ceiling effect:** each student has only 2 labeled-relevant JDs among the 8 candidates. Precision@k cannot exceed `min(k, 2) / k` regardless of ranking quality — 0.4 at k=5, 0.667 at k=3 are the maximum achievable scores on this set, not just what this ranker happened to score. NDCG@k is the metric that actually reflects ranking quality here.

## Results

### k=5

| student_id | precision@5 | ndcg@5 |
|---|---|---|
| student_fullstack | 0.400 | 1.000 |
| student_ml | 0.400 | 1.000 |
| student_backend_java | 0.400 | 1.000 |
| student_cybersecurity | 0.400 | 1.000 |
| student_devops_cloud | 0.400 | 1.000 |
| **mean** | **0.400** | **1.000** |

### k=3

| student_id | precision@3 | ndcg@3 |
|---|---|---|
| student_fullstack | 0.667 | 1.000 |
| student_ml | 0.667 | 1.000 |
| student_backend_java | 0.667 | 1.000 |
| student_cybersecurity | 0.667 | 1.000 |
| student_devops_cloud | 0.667 | 1.000 |
| **mean** | **0.667** | **1.000** |

Both precision@k values sit exactly at this set's ceiling (`2/5` and `2/3`), and NDCG@k is 1.0 for every student at both k — the matcher places both relevant JDs ahead of all irrelevant ones, correctly ordered, for every profile. Given the toy set's clean domain separation (five distinct career tracks, JDs cleanly aligned to one track apiece bar two deliberate near-miss pairs), a perfect score here is expected and should not be read as evidence the matcher performs this well on real, messier resumes and JDs — it confirms the ranking logic itself (sorting by `compute_score` from `matcher/similarity.py`) works correctly on unambiguous cases, nothing more.

## Reproducing

```
cd ml-service
python -m matcher.evaluate_ranking       # runs at k=5
python -c "from matcher.evaluate_ranking import evaluate_all; evaluate_all(k=3)"
```
