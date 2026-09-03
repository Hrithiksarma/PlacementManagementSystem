"""Shared Gemini client instance — used by writeup.py and critique.py, the
only two modules in this service that make an LLM call."""
import os

from google import genai

MODEL = "gemini-flash-lite-latest"
_client = None


def get_client():
    global _client
    if _client is None:
        _client = genai.Client(api_key=os.environ["GEMINI_API_KEY"])
    return _client
