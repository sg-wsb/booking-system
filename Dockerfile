FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jdk
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar
COPY src/main/resources/application.yml /app/application.yml

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]