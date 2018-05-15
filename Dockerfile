FROM openjdk:8-jdk-slim

ENV jarFile build/libs/reactive-webapp-0.0.1-SNAPSHOT.jar
ENV jarDest /opt

COPY ${jarFile} ${jarDest}

EXPOSE 8080

ENTRYPOINT ["/usr/bin/java"]

CMD ["-jar", "/opt/reactive-webapp-0.0.1-SNAPSHOT.jar"]