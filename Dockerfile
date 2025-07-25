# Stage 1: Build the application
FROM gradle:8.5-jdk17-jammy AS builder

WORKDIR /app

# Copy gradle wrapper files
COPY gradlew ./
COPY gradle ./gradle

# Copy build configuration files
COPY build.gradle.kts ./
COPY settings.gradle.kts ./

# Download dependencies
RUN ./gradlew dependencies

# Copy source code
COPY src ./src

# Build the application
RUN ./gradlew bootJar

# Stage 2: Run the application
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Copy the executable jar from the build stage
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
