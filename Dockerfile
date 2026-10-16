# Stage 1: Build the JAR with Maven
FROM maven:3.9-eclipse-temurin-17-alpine AS builder
WORKDIR /app

# Cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Build application
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

# Memory tuning for AWS EC2 t2.micro (1GB RAM)
ENV JAVA_TOOL_OPTIONS="-Xms128m -Xmx384m"

ENTRYPOINT ["java", "-jar", "app.jar"]