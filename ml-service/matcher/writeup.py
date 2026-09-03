"""Turns already-computed matched/missing skill lists into a study-plan
write-up plus a few likely interview questions — one Gemini call, no
extraction happens here."""
import re

from .llm_client import MODEL, get_client


def generate_writeup_and_questions(matched, missing, score):
    matched_names = [m["jd_skill"] for m in matched]
    prompt = (
        f"A student is being matched against a job description for a campus "
        f"placement drive. Match score: {score}%.\n"
        f"Skills they already have that the role needs: {', '.join(matched_names) or 'none'}.\n"
        f"Skills the role needs that they're missing: {', '.join(missing) or 'none'}.\n\n"
        "Respond in exactly this format, nothing else:\n"
        "WRITE_UP:\n"
        "<a short 3-5 sentence encouraging paragraph naming the 2-3 most important "
        "missing skills and concrete, actionable next steps to close the gap before "
        "the drive — plain text, no headers or bullet points>\n"
        "QUESTIONS:\n"
        "1. <a likely technical interview question targeting one of the missing skills>\n"
        "2. <a likely technical interview question targeting a different missing skill>\n"
        "3. <a likely technical interview question about the role in general>\n"
    )
    response = get_client().models.generate_content(model=MODEL, contents=prompt)
    return _parse_response(response.text)


def _parse_response(text):
    write_up_match = re.search(r"WRITE_UP:\s*(.*?)\s*QUESTIONS:", text, re.DOTALL)
    write_up = write_up_match.group(1).strip() if write_up_match else text.strip()

    questions = []
    parts = text.split("QUESTIONS:", 1)
    if len(parts) == 2:
        for line in parts[1].strip().splitlines():
            cleaned = re.sub(r"^\d+[.)]\s*", "", line.strip())
            if cleaned:
                questions.append(cleaned)

    return write_up, questions
