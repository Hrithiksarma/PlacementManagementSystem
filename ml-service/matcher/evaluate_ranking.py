"""Ranking-quality evaluation for rank_jds_for_student against the small
hand-labeled set in eval_data.py. numpy is already a project dependency
(see requirements.txt), so no new dependency is added here."""
import numpy as np

from .eval_data import JD_PROFILES, STUDENT_JD_RELEVANCE, STUDENT_PROFILES
from .ranking import rank_jds_for_student

_STUDENT_SKILLS = dict(STUDENT_PROFILES)
_ALL_JDS = [{"jd_id": jd_id, "skills": skills} for jd_id, skills in JD_PROFILES]


def _dcg(relevances: list[int]) -> float:
    """DCG = sum(rel_i / log2(i+1)) for i=1..len(relevances) (1-indexed)."""
    ranks = np.arange(1, len(relevances) + 1)
    return float(np.sum(np.asarray(relevances, dtype=float) / np.log2(ranks + 1)))


def evaluate_student(student_id: str, k: int = 5) -> dict:
    """Rank all JDs for one student and score the top-k against hand labels."""
    student_skills = _STUDENT_SKILLS[student_id]
    relevance = dict(STUDENT_JD_RELEVANCE[student_id])

    ranked = rank_jds_for_student(student_skills, _ALL_JDS)
    ranked_jd_ids = [jd["jd_id"] for jd in ranked]

    top_k_ids = ranked_jd_ids[:k]
    top_k_relevances = [relevance.get(jd_id, 0) for jd_id in top_k_ids]

    precision_at_k = sum(1 for rel in top_k_relevances if rel >= 1) / k

    ideal_relevances = sorted(relevance.values(), reverse=True)[:k]
    idcg = _dcg(ideal_relevances)
    ndcg_at_k = _dcg(top_k_relevances) / idcg if idcg > 0 else 0.0

    return {
        "student_id": student_id,
        "precision_at_k": precision_at_k,
        "ndcg_at_k": ndcg_at_k,
        "k": k,
        "ranked_jd_ids": ranked_jd_ids,
    }


def evaluate_all(k: int = 5) -> list[dict]:
    """Evaluate every student in STUDENT_PROFILES and print a summary table."""
    results = [evaluate_student(student_id, k) for student_id, _ in STUDENT_PROFILES]

    header = f"{'student_id':<25} {'precision@' + str(k):<14} {'ndcg@' + str(k):<10}"
    print(header)
    print("-" * len(header))
    for r in results:
        print(f"{r['student_id']:<25} {r['precision_at_k']:<14.3f} {r['ndcg_at_k']:<10.3f}")

    mean_precision = float(np.mean([r["precision_at_k"] for r in results]))
    mean_ndcg = float(np.mean([r["ndcg_at_k"] for r in results]))
    print("-" * len(header))
    print(f"{'mean':<25} {mean_precision:<14.3f} {mean_ndcg:<10.3f}")

    return results


if __name__ == "__main__":
    evaluate_all()
