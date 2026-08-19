# ── Build stage ────────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Cache dependencies separately from source so a source-only change doesn't
# re-download the whole dependency tree.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q -DskipTests package

# ── Runtime stage ──────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

COPY --from=build /app/target/backend-0.0.1-SNAPSHOT.jar app.jar

# application.properties currently hardcodes server.port=8080. Deploy platforms
# that inject their own PORT env var will need that changed to
# server.port=${PORT:8080} — tracked as a later deploy-prep step, not done here.
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
