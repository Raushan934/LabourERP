# Multi-stage Dockerfile for Spring Boot 3 Backend with Java 22
FROM maven:3.9.8-eclipse-temurin-22-alpine AS build
WORKDIR /app

# Copy Maven mirror settings to bypass Maven Central 429 Too Many Requests
COPY settings.xml .
COPY pom.xml .
COPY src ./src

# Package application using Google Maven mirror with network retries
RUN mvn clean package -DskipTests -s settings.xml -Dmaven.wagon.http.retryHandler.count=5

FROM eclipse-temurin:22-jre-alpine
WORKDIR /app
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser
COPY --from=build /app/target/*.jar app.jar

ENV SPRING_PROFILES_ACTIVE=supabase
ENV PORT=8080

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-8080} -jar app.jar"]
