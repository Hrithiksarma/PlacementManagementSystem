"""Embedding-based similarity matching — the genuine retrieval step of the
RAG pipeline. No LLM call anywhere in this module."""
import numpy as np

from .model import get_model
from .taxonomy import CANONICAL_SKILLS

MATCH_THRESHOLD = 0.6

_taxonomy_embeddings = None


def _get_taxonomy_embeddings():
    global _taxonomy_embeddings
    if _taxonomy_embeddings is None:
        vectors = get_model().encode(CANONICAL_SKILLS, normalize_embeddings=True)
        _taxonomy_embeddings = dict(zip(CANONICAL_SKILLS, vectors))
    return _taxonomy_embeddings


def _embed_skills(skills):
    cache = _get_taxonomy_embeddings()
    unknown = [s for s in skills if s not in cache]
    fresh = {}
    if unknown:
        fresh = dict(zip(unknown, get_model().encode(unknown, normalize_embeddings=True)))
    return np.array([cache.get(s, fresh.get(s)) for s in skills])


def match_skills(jd_skills, resume_skills):
    if not jd_skills:
        return {"matched": [], "missing": []}
    if not resume_skills:
        return {"matched": [], "missing": list(jd_skills)}

    jd_embeddings = _embed_skills(jd_skills)
    resume_embeddings = _embed_skills(resume_skills)
    similarities = jd_embeddings @ resume_embeddings.T

    matched, missing = [], []
    for i, jd_skill in enumerate(jd_skills):
        best_idx = int(np.argmax(similarities[i]))
        best_score = float(similarities[i][best_idx])
        if best_score >= MATCH_THRESHOLD:
            matched.append({"jd_skill": jd_skill, "resume_skill": resume_skills[best_idx]})
        else:
            missing.append(jd_skill)
    return {"matched": matched, "missing": missing}


def compute_score(matched, jd_skills):
    if not jd_skills:
        return 0
    return round(len(matched) / len(jd_skills) * 100)
