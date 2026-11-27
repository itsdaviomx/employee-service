# syntax=docker/dockerfile:1

# ---- Build stage: compiles the code and packages the executable jar.
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace

# Dependencies are downloaded in their own layer, so editing code does not download them again.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
# The test suite needs MySQL, so it runs in the IDE or the CI pipeline, not inside the image build.
RUN mvn -B -q package -DskipTests

# ---- Runtime stage: only the JRE and the jar, running as a non-root user.
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

RUN groupadd --system app && useradd --system --gid app --no-create-home app
COPY --from=build /workspace/target/employee-service-*.jar app.jar
USER app

# Inside a container, logs go to stdout only (docker logs); no log file is written.
ENV LOG_FILE=""
EXPOSE 8080

HEALTHCHECK --interval=15s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -fsS http://localhost:8080/actuator/health/readiness || exit 1

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
