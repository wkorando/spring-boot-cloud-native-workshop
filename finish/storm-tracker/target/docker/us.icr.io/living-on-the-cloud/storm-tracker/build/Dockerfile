FROM adoptopenjdk/openjdk8-openj9:alpine-slim

COPY target/storm-tracker.jar /

ENTRYPOINT ["java", "-jar", "storm-tracker.jar" ]
