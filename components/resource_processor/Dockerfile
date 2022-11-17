FROM adoptopenjdk/openjdk11:alpine-jre
COPY /build/libs/resource_processor-1.0-SNAPSHOT.jar resource_processor-1.0-SNAPSHOT.jar
ENTRYPOINT ["java", "-jar", "/resource_processor-1.0-SNAPSHOT.jar"]