FROM alpine:3.16.0

RUN apk add --no-cache java-cacerts openjdk17-jdk

COPY /build/libs/song_service-1.0-SNAPSHOT.jar song_service-1.0-SNAPSHOT.jar
ENTRYPOINT ["java", "-jar", "/song_service-1.0-SNAPSHOT.jar"]