# ----------------------------------------------------------------------
# --- CORRECTED DOCKERFILE CONTENT (Changed all Java versions to 21) ---
# ----------------------------------------------------------------------

# --- STAGE 1: BUILD STAGE ---
# Use an official Maven image with JDK 21 for building the application
FROM maven:3.9-eclipse-temurin-21 AS build

# Set the working directory inside the container
WORKDIR /app

# Copy the pom.xml and src/ files needed to download dependencies
COPY pom.xml .
COPY src ./src

# Build the application, skipping tests to speed up the process
RUN mvn clean package -DskipTests

# --- STAGE 2: RUNNER STAGE ---
# Use a smaller, secure JRE 21 image for the final running container
FROM eclipse-temurin:21-jre-alpine

# Set arguments for configuration (Render will use these as environment variables)
ARG RDS_HOSTNAME
ARG RDS_USERNAME
ARG RDS_PASSWORD
ARG SPRING_MAIL_HOST
ARG SPRING_MAIL_PORT
ARG SPRING_MAIL_USERNAME
ARG SPRING_MAIL_PASSWORD

# Set the working directory
WORKDIR /app

# Copy the built JAR from the build stage (assuming the JAR name is simple, e.g., sam-0.0.1-SNAPSHOT.jar)
COPY --from=build /app/target/*.jar app.jar

# Expose the port your Spring Boot app runs on (default is 8080)
EXPOSE 8080

# Configure the temporary upload directory (Render persistent disk path)
RUN mkdir -p /tmp/sam-uploads/

# Define the entry point for the container
ENTRYPOINT ["java", "-jar", "app.jar"]