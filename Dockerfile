FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B
COPY src ./src
RUN chmod +x mvnw && ./mvnw clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S secureauth && adduser -S secureauth -G secureauth
COPY --from=builder /app/target/*.jar app.jar
RUN chown secureauth:secureauth app.jar
USER secureauth
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]