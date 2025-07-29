FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

COPY gradlew gradlew.bat ./
COPY gradle ./gradle/
COPY settings.gradle ./
COPY build.gradle ./
RUN chmod +x gradlew || true

COPY src ./src

RUN ./gradlew build -x test

FROM eclipse-temurin:17-jre AS runtime
WORKDIR /app

EXPOSE 8080

COPY --from=build /app/build/libs/*.jar app.jar

# OpenSearch 연결 대기를 위한 스크립트 추가
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

CMD ["java", "-jar", "app.jar"]
