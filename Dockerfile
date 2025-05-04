# Builder-Stage
FROM eclipse-temurin:23-jdk AS builder

WORKDIR /build

# Copy Maven files
COPY --chmod=0755 mvnw mvnw
COPY .mvn/ .mvn/
COPY pom.xml pom.xml
COPY src/ src/

# Expliziter Befehl zum Erstellen der JAR ohne Repackage
RUN ./mvnw clean package -DskipTests

# Runtime-Stage
FROM eclipse-temurin:23-jre

WORKDIR /app

# Benutzer erstellen (wie vorher)
ARG UID=10001
RUN adduser \
    --disabled-password \
    --gecos "" \
    --home "/nonexistent" \
    --shell "/sbin/nologin" \
    --no-create-home \
    --uid "${UID}" \
    appuser
USER appuser

# JAR-Datei kopieren
COPY --from=builder /build/target/*.jar app.jar

EXPOSE 8080

# Hauptklasse explizit angeben
ENTRYPOINT ["java", "-jar", "app.jar"]