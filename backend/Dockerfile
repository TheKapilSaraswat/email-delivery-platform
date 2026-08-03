# syntax=docker/dockerfile:1
# ============================================================================
# Email Delivery Platform - production backend image (multi-stage build)
# ============================================================================

# --- Stage 1: build the Spring Boot fat jar --------------------------------
FROM maven:3.9.8-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q package -DskipTests

# --- Stage 2: minimal runtime ----------------------------------------------
FROM eclipse-temurin:17-jre-jammy

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system app \
    && useradd --system --gid app app

WORKDIR /app
RUN mkdir -p /app/data && chown -R app:app /app

COPY --from=build /workspace/target/*.jar app.jar

USER app

EXPOSE 5000

HEALTHCHECK --interval=30s --timeout=5s --start-period=90s --retries=3 \
    CMD curl -fsS "http://localhost:$${PORT:-5000}/api/health" >/dev/null || exit 1

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-XX:+UseContainerSupport", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
