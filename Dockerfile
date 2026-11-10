# syntax=docker/dockerfile:1
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /workspace

COPY pom.xml ./
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B clean package -DskipTests

FROM eclipse-temurin:25-jre
WORKDIR /app

RUN groupadd --system beforeyouvisit && useradd --system --gid beforeyouvisit --uid 10001 beforeyouvisit
COPY --from=build /workspace/target/before-you-visit-1.0.0.jar app.jar

USER beforeyouvisit
EXPOSE 8080

ENV APP_VERSION=local \
    JAVA_TOOL_OPTIONS="-XX:InitialRAMPercentage=30 -XX:MaxRAMPercentage=70 -XX:+ExitOnOutOfMemoryError"

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
