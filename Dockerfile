# Etapa 1: compilar JAR
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B dependency:go-offline -DskipTests

COPY src ./src
RUN ./mvnw -B package -DskipTests

# Etapa 2: imagen de ejecución
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

RUN groupadd -r app && useradd -r -g app app
USER app

COPY --from=build /app/target/admin-club-*.jar app.jar

EXPOSE 8080

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
ENV SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
