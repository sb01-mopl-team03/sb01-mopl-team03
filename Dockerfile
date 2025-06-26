# [1] Build Stage
FROM gradle:8.14.2-jdk17-alpine AS build
WORKDIR /app

COPY gradlew gradlew.bat ./
COPY gradle ./gradle/
COPY settings.gradle ./
COPY build.gradle ./
RUN chmod +x gradlew

COPY src ./src

RUN ./gradlew build -x test

# [2] Runtime stage
FROM eclipse-temurin:17-jre-alpine-3.21 AS runtime
WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar
CMD ["java", "-jar", "app.jar"]