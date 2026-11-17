# syntax=docker/dockerfile:1

# Build stage: the Maven prod profile also builds the frontend (frontend-maven-plugin) and puts it inside the jar.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY backend backend
COPY frontend frontend
# Tests run in CI; the image only packages the jar. The cache mount keeps Maven downloads between builds.
RUN --mount=type=cache,target=/root/.m2 mvn -f backend/pom.xml -B -Pprod -DskipTests package

# Run stage: only the JRE and the jar.
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/backend/target/protube-back-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
