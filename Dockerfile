FROM eclipse-temurin:25-jdk AS builder
ARG MODULE
WORKDIR /app
COPY . .
RUN chmod +x mvnw && ./mvnw -pl ${MODULE} -am clean package -DskipTests

FROM eclipse-temurin:25-jre
ARG MODULE
WORKDIR /app
COPY --from=builder /app/${MODULE}/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]