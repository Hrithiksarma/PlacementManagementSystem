"""Resume quality/ATS-friendliness critique — a separate feature from the JD
matcher (not tied to any particular drive). One Gemini call, no extraction,
no persistence — computed fresh on every request."""
import re

from .llm_client import MODEL, get_client


def critique_resume(resume_text):
    prompt = (
        "Review this resume for a student applying to campus placement drives.\n"
        f'Resume text:\n"""\n{resume_text}\n"""\n\n'
        "Respond in exactly this format, nothing else:\n"
        "OVERALL:\n"
        "<1-2 sentence overall impression>\n"
        "STRENGTHS:\n"
        "- <strength 1>\n"
        "- <strength 2>\n"
        "- <strength 3>\n"
        "IMPROVEMENTS:\n"
        "- <specific, actionable improvement — formatting, clarity, ATS-friendliness, or content>\n"
        "- <specific, actionable improvement 2>\n"
        "- <specific, actionable improvement 3>\n"
    )
    response = get_client().models.generate_content(model=MODEL, contents=prompt)
    return _parse_response(response.text)


def _bullets(text, section, next_marker):
    pattern = rf"{section}:\s*(.*?)(?:{next_marker}|$)" if next_marker else rf"{section}:\s*(.*)$"
    match = re.search(pattern, text, re.DOTALL)
    if not match:
        return []
    items = []
    for line in match.group(1).strip().splitlines():
        cleaned = re.sub(r"^[-*]\s*", "", line.strip())
        if cleaned:
            items.append(cleaned)
    return items


def _parse_response(text):
    overall_match = re.search(r"OVERALL:\s*(.*?)\s*STRENGTHS:", text, re.DOTALL)
    overall = overall_match.group(1).strip() if overall_match else text.strip()

    return {
        "overall": overall,
        "strengths": _bullets(text, "STRENGTHS", "IMPROVEMENTS:"),
        "improvements": _bullets(text, "IMPROVEMENTS", None),
    }
