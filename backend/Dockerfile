# ---- build stage ----
FROM maven:3-eclipse-temurin-26 AS build
WORKDIR /app

# Cache dependencies first: only re-download when pom.xml changes.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ---- runtime stage ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Run as an unprivileged user (busybox adduser).
RUN addgroup -S app && adduser -S -G app app
USER app

COPY --from=build /app/target/sniplink-backend-*.jar app.jar

# Small heap + SerialGC: fits comfortably in a 512 MB free-tier container.
ENV JAVA_OPTS="-XX:+UseSerialGC -Xms128m -Xmx256m"
EXPOSE 8080

# busybox wget is present in the alpine image.
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
