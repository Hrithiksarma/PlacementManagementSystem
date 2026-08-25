"""
PRMS ML service — the RAG resume/JD skills-gap analyzer.

Day 4 scope: just enough to prove this service can be built, deployed, and
reached by the Java backend over the network before any real ML logic is
written (see docs/decisions.md for why — deploy-early discipline). The real
POST /analyze pipeline (skill extraction, embeddings, matching, write-up
generation) is added in Days 5-6.
"""

from fastapi import FastAPI

app = FastAPI(title="PRMS ML Service")


@app.get("/health")
def health():
    return {"status": "ok"}
