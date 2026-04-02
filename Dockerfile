FROM eclipse-temurin:17-jdk-alpine 
WORKDIR /app 
COPY . . 
RUN ./mvnw clean package -DskipTests 
EXPOSE 8084 
CMD ["java", "-jar", "target/user-json-service-1.0.jar"] 
