FROM adoptopenjdk/openjdk11:latest
ADD target/NordbahnService.jar NordbahnService.jar
EXPOSE 8001
ENTRYPOINT ["java", "-jar",  "NordbahnService.jar"]