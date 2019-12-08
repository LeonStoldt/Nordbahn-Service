FROM adoptopenjdk/openjdk11:latest
ADD target/NordbahnService.jar NordbahnService.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar",  "NordbahnService.jar"]