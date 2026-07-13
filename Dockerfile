FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM amazoncorretto:21-alpine
WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --from=build /app/target/*.jar app.jar

# O Logback grava app.log no diretório de trabalho.
RUN chown -R appuser:appgroup /app

USER appuser

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
