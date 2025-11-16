FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
COPY mvnw mvnw.cmd ./
COPY .mvn .mvn
RUN mvn -B -DskipTests dependency:go-offline
COPY src src
RUN mvn -B -DskipTests clean package

FROM eclipse-temurin:17-jre
WORKDIR /app
ENV APP_DB_URL=jdbc:postgresql://db:5432/orm_platform \
    APP_DB_USERNAME=postgres \
    APP_DB_PASSWORD=postgres \
    JAVA_OPTS=""
COPY --from=build /workspace/target/orm-platform-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]


