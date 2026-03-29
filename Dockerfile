# Build stage
FROM gradle:9.4.0-jdk17 AS build
WORKDIR /workspace
COPY oficina/ .
RUN chmod +x gradlew \
	&& ./gradlew bootJar --no-daemon -x test \
	&& cp "build/libs/$(ls build/libs | grep -v plain | grep '\.jar$' | head -n1)" /workspace/application.jar

# Runtime
FROM eclipse-temurin:17-jre-alpine
RUN apk add --no-cache wget
WORKDIR /app
COPY --from=build /workspace/application.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
