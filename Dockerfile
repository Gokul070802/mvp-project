FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY . .

# 🔥 Move into backend folder
WORKDIR /app/backend

# Give permission
RUN chmod +x mvnw

# Build project
RUN ./mvnw clean install -DskipTests

# Run app
CMD ["java", "-jar", "target/mvp-backend-0.0.1-SNAPSHOT.jar"]