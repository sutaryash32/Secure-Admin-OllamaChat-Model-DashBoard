# Build stage
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Add MySQL client for debugging
RUN apk add --no-cache curl

EXPOSE 8080

ENV JAVA_OPTS="-Xms512m -Xmx1024m"
ENV SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
