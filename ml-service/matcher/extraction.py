"""Local skill extraction — no LLM call here. Two passes:

1. Alias matching against the curated taxonomy (fast, precise).
2. An embedding-based keyphrase fallback for skills the taxonomy doesn't
   cover — candidate n-grams from the text, kept if they're semantically
   close enough to the document as a whole.
"""
import re

from .model import get_model
from .taxonomy import ALIAS_TO_CANONICAL

FALLBACK_THRESHOLD = 0.62
FALLBACK_MAX_CANDIDATES = 10

_STOPWORDS = {
    "the", "and", "or", "a", "an", "of", "to", "in", "on", "for", "with",
    "is", "are", "be", "as", "at", "by", "this", "that", "will", "your",
    "you", "we", "our", "it", "from", "have", "has", "including", "etc",
}


def _alias_matches(text_lower):
    found = set()
    for form, canonical in ALIAS_TO_CANONICAL.items():
        if re.search(r"\b" + re.escape(form) + r"\b", text_lower):
            found.add(canonical)
    return found


def _candidate_phrases(text):
    """1-3 word windows, restricted to Title Case runs. Real skill mentions in
    JDs/resumes are almost always capitalized ("Edge Computing", "Kubernetes");
    requiring it filters out grammatical fragments like "evaluate edge server"
    that otherwise score deceptively high on similarity-to-document alone
    (any substring of the doc is naturally close to the doc's own embedding)."""
    words = re.findall(r"[A-Za-z][A-Za-z+.#/-]*", text)
    words = [w.rstrip(".") for w in words]
    words = [w for w in words if w.lower() not in _STOPWORDS and len(w) > 1]
    candidates = set()
    for n in (1, 2, 3):
        for i in range(len(words) - n + 1):
            window = words[i:i + n]
            if not all(w[:1].isupper() for w in window):
                continue
            phrase = " ".join(window)
            if len(phrase) >= 3:
                candidates.add(phrase)
    return list(candidates)


def _drop_substrings_of_kept(scored):
    """Given (phrase, score) pairs sorted best-first, drop a phrase that
    overlaps (in either direction) with an already-kept phrase — e.g. once
    "AWS experience" is kept, both "hiring Edge" (a fragment of it) and "APIs
    AWS experience plus" (an expansion of it) are redundant. N-grams otherwise
    flood the results with overlapping fragments of the same mention."""
    kept_lower = []
    for phrase, score in scored:
        phrase_lower = phrase.lower()
        if any(phrase_lower in existing or existing in phrase_lower for existing in kept_lower):
            continue
        kept_lower.append(phrase_lower)
    return [phrase for phrase, score in scored if phrase.lower() in kept_lower]


def _contains_found_skill(phrase_lower, already_found_lower):
    return any(
        re.search(r"\b" + re.escape(found) + r"\b", phrase_lower)
        for found in already_found_lower
    )


def _embedding_fallback(text, already_found_lower):
    candidates = [
        c for c in _candidate_phrases(text)
        if not _contains_found_skill(c.lower(), already_found_lower)
    ]
    if not candidates:
        return []

    model = get_model()
    doc_embedding = model.encode([text], normalize_embeddings=True)[0]
    candidate_embeddings = model.encode(candidates, normalize_embeddings=True)
    sims = candidate_embeddings @ doc_embedding

    scored = sorted(zip(candidates, sims), key=lambda pair: pair[1], reverse=True)
    above_threshold = [(phrase, score) for phrase, score in scored if score >= FALLBACK_THRESHOLD]
    return _drop_substrings_of_kept(above_threshold)[:FALLBACK_MAX_CANDIDATES]


def extract_skills(text):
    if not text or not text.strip():
        return []

    text_lower = text.lower()
    taxonomy_hits = _alias_matches(text_lower)
    already_found_lower = {s.lower() for s in taxonomy_hits}

    fallback_hits = _embedding_fallback(text, already_found_lower)

    return list(taxonomy_hits) + fallback_hits
