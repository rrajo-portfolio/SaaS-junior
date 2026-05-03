FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY backend/.mvn ./backend/.mvn
COPY backend/mvnw backend/pom.xml ./backend/
COPY backend/src ./backend/src

WORKDIR /workspace/backend
RUN sh ./mvnw -B -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN adduser --system --group app && apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/* && mkdir -p /app/storage/documents && chown -R app:app /app/storage
COPY --from=build /workspace/backend/target/backend-*.jar /app/service.jar
USER app
EXPOSE 8080
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/service.jar"]
