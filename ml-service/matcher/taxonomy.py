"""Loads the curated skills taxonomy once at import time."""
import json
from pathlib import Path

_TAXONOMY_PATH = Path(__file__).resolve().parent.parent / "data" / "skills_taxonomy.json"

with open(_TAXONOMY_PATH, encoding="utf-8") as f:
    TAXONOMY = json.load(f)

CANONICAL_SKILLS = [entry["skill"] for entry in TAXONOMY]

# lowercased surface form (canonical name or alias) -> canonical name
ALIAS_TO_CANONICAL = {}
for _entry in TAXONOMY:
    _canonical = _entry["skill"]
    for _form in [_canonical] + _entry.get("aliases", []):
        ALIAS_TO_CANONICAL[_form.lower()] = _canonical
