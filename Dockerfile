FROM gradle:8.9-jdk21 AS builder
WORKDIR /home/gradle/project
COPY . .
RUN gradle clean build -x test

FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app

COPY --from=builder /home/gradle/project/build/libs/agile-runner-0.0.1-SNAPSHOT.jar app.jar

ENV PORT=8080
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT} -jar app.jar"]