FROM alpine:3.16.0

RUN apk add --no-cache java-cacerts openjdk17-jdk

COPY /build/libs/resource_service-1.0-SNAPSHOT.jar resource_service-1.0-SNAPSHOT.jar
ENTRYPOINT ["java", "-jar", "/resource_service-1.0-SNAPSHOT.jar"]