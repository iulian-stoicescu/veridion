FROM openjdk:17-jdk-slim
WORKDIR /opt
ENV PORT 8080
EXPOSE 8080
COPY build/libs/veridion.jar /opt/veridion-app.jar
ENTRYPOINT exec java $JAVA_OPTS -jar veridion-app.jar