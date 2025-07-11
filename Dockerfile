FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

COPY gradlew gradlew.bat ./
COPY gradle ./gradle/
COPY settings.gradle ./
COPY build.gradle ./
RUN chmod +x gradlew || true

COPY src ./src

#RUN ./gradlew build -x test
RUN ./gradlew build

FROM eclipse-temurin:17-jre AS runtime
WORKDIR /app

EXPOSE 8080

COPY --from=build /app/build/libs/*.jar app.jar
CMD ["java", "-jar", "app.jar"]