FROM eclipse-temurin:17-jdk

WORKDIR /app

# Copy entire repo
COPY . .

# 🔥 Go to backend project
WORKDIR /app/mvp-backend

# Fix mvnw permission
RUN chmod +x mvnw

# Build project
RUN ./mvnw clean install -DskipTests

# Run Spring Boot app
CMD ["java", "-jar", "target/mvp-backend-0.0.1-SNAPSHOT.jar"]