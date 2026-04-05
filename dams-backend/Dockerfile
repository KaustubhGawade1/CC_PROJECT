# Build stage
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /build

# Copy pom.xml and download dependencies first for caching
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy rest of the code
COPY src ./src

# Package application
RUN mvn package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jdk

WORKDIR /app

# Copy jar from build stage
COPY --from=build /build/target/databasemanagementsystem-0.0.1-SNAPSHOT.jar app.jar

ENV JAVA_TOOL_OPTIONS="-Xmx300m -Xss512k -XX:MaxMetaspaceSize=100m"

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
