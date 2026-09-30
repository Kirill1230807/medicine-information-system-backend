# Етап 1: Збірка проєкту за допомогою Maven
FROM eclipse-temurin:25-jdk AS builder
WORKDIR /app

# Копіюємо файли конфігурації Maven та обгортку
COPY mvnw pom.xml ./
COPY .mvn .mvn

# Копіюємо вихідний код
COPY src src

# Збираємо проєкт (пропускаючи тести в Docker, оскільки тести вже запускаються окремо в GitHub Actions)
RUN ./mvnw clean package -DskipTests

# Етап 2: Створення легкого фінального образу для запуску
FROM eclipse-temurin:25-jre
WORKDIR /app

# Копіюємо зібраний jar-файл з попереднього етапу
COPY --from=builder /app/target/*.jar app.jar

# Відкриваємо порт, на якому працює твій Spring Boot додаток (за замовчуванням 8080)
EXPOSE 8080

# Команда для запуску додатку
ENTRYPOINT ["java", "-jar", "app.jar"]