FROM  maven:3.9.9-eclipse-temurin-24 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:24-jdk
ENV DATABASE_URL=jdbc:postgresql://db:5432/jobee
ENV DATABASE_USER=abrizvi
ENV DATABASE_PASSWORD=Password!
ENV JWT_SECRET=06d508be1f0e59cb5bfde3e8f5f3872c34f48dbf14df9f2cc3c5c510e4988246
WORKDIR /app
COPY --from=builder /app/target/jobee-0.0.1-SNAPSHOT.jar jobee-0.0.1-SNAPSHOT.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "jobee-0.0.1-SNAPSHOT.jar"]
