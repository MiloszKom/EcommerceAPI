FROM openjdk:17-jdk-alpine

# Install curl for health checks
RUN apk add --no-cache curl

WORKDIR /app
ADD https://repo1.maven.org/maven2/io/opentelemetry/javaagent/opentelemetry-javaagent/2.11.0/opentelemetry-javaagent-2.11.0.jar \
    /app/libs/opentelemetry-javaagent-2.11.0.jar

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

ENV JAVA_TOOL_OPTIONS="-javaagent:/app/libs/opentelemetry-javaagent-2.11.0.jar"

ENTRYPOINT ["java", "-jar", "app.jar"]