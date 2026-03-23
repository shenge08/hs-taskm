# HS-TASKM Dockerfile
# This is a sample Dockerfile for building the HS-TASKM application

FROM eclipse-temurin:17-jre-alpine
MAINTAINER shenge08 <shenge08@example.com>

# Set working directory
WORKDIR /app

# Add JAR file
ARG JAR_FILE
COPY ${JAR_FILE} hs-taskm.jar

# Expose port (adjust if needed)
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "hs-taskm.jar"]
