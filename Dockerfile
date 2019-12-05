FROM adoptopenjdk/openjdk11:latest
ADD target/NordbahnService.jar NordbahnService.jar
EXPOSE 8003
ENTRYPOINT ["java", "-jar",  "NordbahnService.jar"]