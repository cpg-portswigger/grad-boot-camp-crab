FROM openjdk:21-jdk-slim

USER root

WORKDIR /vulnerable-web-app

COPY . /vulnerable-web-app

RUN ./gradlew build

EXPOSE 8080
CMD ["java", "-jar", "/vulnerable-web-app/build/libs/vulnerable-web-app-1.0-SNAPSHOT.jar"]