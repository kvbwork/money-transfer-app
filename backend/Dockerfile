FROM maven:latest AS build

COPY src /app/src

COPY pom.xml /app

WORKDIR /app

RUN mvn clean package



FROM adoptopenjdk/openjdk11:alpine-jre

EXPOSE 5500

COPY --from=build /app/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
