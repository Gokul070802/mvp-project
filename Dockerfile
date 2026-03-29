FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY . .

# 🔥 FIX: give execute permission
RUN chmod +x mvnw

# Build project
RUN ./mvnw clean install -DskipTests

# Run app
CMD ["java", "-jar", "target/mvp-backend-0.0.1-SNAPSHOT.jar"]