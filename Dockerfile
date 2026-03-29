# Use Java 17
FROM eclipse-temurin:17-jdk

# Set working directory
WORKDIR /app

# Copy project files
COPY . .

# Build the project
RUN ./mvnw clean install -DskipTests

# Run the app
CMD ["java", "-jar", "target/mvp-backend-0.0.1-SNAPSHOT.jar"]