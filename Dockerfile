# Stage 1: Build the application
FROM eclipse-temurin:21-jdk-alpine AS build

# Set work directory
WORKDIR /app

# Copy gradle wrapper and build files
COPY gradlew .
COPY gradle ./gradle
COPY build.gradle .
COPY settings.gradle .

# Copy source code
COPY src ./src

# Make gradlew executable
RUN chmod +x ./gradlew

# Build the jar (produces build/libs/*.jar)
RUN ./gradlew clean bootJar --no-daemon

# Stage 2: Package the application
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Copy jar from build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Expose port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java","-jar","app.jar"]
