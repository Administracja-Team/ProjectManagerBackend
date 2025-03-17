FROM alpine/java:22-jdk
LABEL authors="gnevilkoko"
WORKDIR /app

COPY target/project-manager-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]