FROM openjdk:8-jdk-alpine

WORKDIR /app

COPY target/api-mock-platform-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]