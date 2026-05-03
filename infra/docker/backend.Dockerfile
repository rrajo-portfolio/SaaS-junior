FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace

COPY backend/.mvn ./backend/.mvn
COPY backend/mvnw backend/pom.xml ./backend/
COPY backend/src ./backend/src

WORKDIR /workspace/backend
RUN chmod +x ./mvnw && ./mvnw -B -DskipTests package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S app && adduser -S app -G app && apk add --no-cache curl
COPY --from=build /workspace/backend/target/backend-*.jar /app/service.jar
USER app
EXPOSE 8080
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/service.jar"]

