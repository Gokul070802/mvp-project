FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY . .

# 🔥 FIX: give permission to mvnw
RUN chmod +x mvnw

# Build the project
RUN ./mvnw clean install -DskipTests

# Run the app
CMD ["java", "-jar", "target/mvp-backend-0.0.1-SNAPSHOT.jar"]