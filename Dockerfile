FROM gradle:8.9-jdk21 AS builder
WORKDIR /home/gradle/project
COPY . .
RUN gradle clean build -x test

FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY build/libs/agile-runner-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
