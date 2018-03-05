FROM openjdk:8-jdk-slim

COPY build/libs/reactive-webapp-0.0.1-SNAPSHOT.jar /opt/lib/

EXPOSE 8080

ENTRYPOINT ["/usr/bin/java"]

CMD ["-jar", "/opt/lib/reactive-webapp-0.0.1-SNAPSHOT.jar"]