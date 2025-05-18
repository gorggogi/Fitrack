# Stage 1: Build the application using Maven
FROM maven:3.8.5-openjdk-17 AS builder

# Set the working directory in the container
WORKDIR /app

# Copy the Maven project file
COPY pom.xml .

# Download project dependencies.
# This layer is cached, so dependencies are only re-downloaded if pom.xml changes.
RUN mvn dependency:go-offline -B

# Copy the rest of the source code
COPY src ./src

# Package the application
RUN mvn package -DskipTests

# Stage 2: Create the final, smaller image
FROM eclipse-temurin:17-jre-alpine

# Set the working directory
WORKDIR /app

# Copy the JAR file from the builder stage
COPY --from=builder /app/target/fitrack-0.0.1-SNAPSHOT.jar app.jar

# Expose the port your application runs on (default for Spring Boot is 8080)
EXPOSE 8080

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"] 