FROM python:3.11-slim
WORKDIR /app

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY . .

# Railway injects its own PORT at runtime (observed: 8080, not necessarily
# fixed) and CMD below reads whatever that is — this ENV is only a fallback
# for running the container with no PORT set (e.g. plain `docker run`). Locally
# via uvicorn directly (not Docker) we still default to 8000 to match
# prms.ml-service.base-url's local default in the Java backend. When setting
# up Railway's networking target port for a fresh service, check the deploy
# logs for the actual "Uvicorn running on http://0.0.0.0:XXXX" line rather
# than assuming it matches this file.
ENV PORT=8000
EXPOSE 8000

CMD ["sh", "-c", "uvicorn main:app --host 0.0.0.0 --port ${PORT}"]
