FROM openjdk:8-jdk-alpine
RUN apk --no-cache add curl
COPY /build/libs/config-server-0.0.1-SNAPSHOT.jar config-server-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java", "-jar", "/config-server-0.0.1-SNAPSHOT.jar"]