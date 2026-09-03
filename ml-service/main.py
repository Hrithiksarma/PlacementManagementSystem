"""
PRMS ML service — the RAG resume/JD skills-gap analyzer.

Skill extraction is local (curated taxonomy + embedding fallback — see
matcher/extraction.py), matching is embedding-based cosine similarity (the
actual "retrieval" step — see matcher/similarity.py), and Gemini's free tier
is called for generation: once per full analysis (write-up + interview
questions, see matcher/writeup.py), or once for a standalone resume critique
(see matcher/critique.py). /match runs extraction + matching only, with no
LLM call, for cheap bulk fit-ranking across many drives at once.
"""
from typing import List

from dotenv import load_dotenv
from fastapi import FastAPI
from pydantic import BaseModel

load_dotenv()

from matcher.critique import critique_resume
from matcher.extraction import extract_skills
from matcher.similarity import compute_score, match_skills
from matcher.writeup import generate_writeup_and_questions

app = FastAPI(title="PRMS ML Service")


class MatchedSkill(BaseModel):
    jd_skill: str
    resume_skill: str


class AnalyzeRequest(BaseModel):
    jd_text: str
    resume_text: str


class AnalyzeResponse(BaseModel):
    score: int
    matched_skills: List[MatchedSkill]
    missing_skills: List[str]
    write_up: str
    interview_questions: List[str]


class MatchRequest(BaseModel):
    jd_text: str
    resume_text: str


class MatchResponse(BaseModel):
    score: int
    matched_skills: List[MatchedSkill]
    missing_skills: List[str]


class CritiqueRequest(BaseModel):
    resume_text: str


class CritiqueResponse(BaseModel):
    overall: str
    strengths: List[str]
    improvements: List[str]


@app.get("/health")
def health():
    return {"status": "ok"}


def _run_match(jd_text: str, resume_text: str):
    jd_skills = extract_skills(jd_text)
    resume_skills = extract_skills(resume_text)
    result = match_skills(jd_skills, resume_skills)
    score = compute_score(result["matched"], jd_skills)
    return jd_skills, result, score


@app.post("/analyze", response_model=AnalyzeResponse)
def analyze(request: AnalyzeRequest):
    _, result, score = _run_match(request.jd_text, request.resume_text)
    write_up, questions = generate_writeup_and_questions(result["matched"], result["missing"], score)

    return AnalyzeResponse(
        score=score,
        matched_skills=result["matched"],
        missing_skills=result["missing"],
        write_up=write_up,
        interview_questions=questions,
    )


@app.post("/match", response_model=MatchResponse)
def match(request: MatchRequest):
    """Same extraction + matching as /analyze, no LLM call — for cheap,
    fast fit-scoring across many drives at once (e.g. ranking a student's
    eligible drives by predicted fit)."""
    _, result, score = _run_match(request.jd_text, request.resume_text)
    return MatchResponse(score=score, matched_skills=result["matched"], missing_skills=result["missing"])


@app.post("/critique-resume", response_model=CritiqueResponse)
def critique_resume_endpoint(request: CritiqueRequest):
    critique = critique_resume(request.resume_text)
    return CritiqueResponse(**critique)
