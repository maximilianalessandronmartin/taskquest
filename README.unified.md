# TaskQuest

TaskQuest ist eine moderne Aufgabenverwaltungsanwendung, die entwickelt wurde, um Teams und Einzelpersonen bei der
Verwaltung ihrer Aufgaben zu unterstützen. Die Anwendung bietet Echtzeit-Updates und erweiterte Funktionen zur
Zeiterfassung.

## Funktionen

- **Aufgabenverwaltung**: Erstellen, Bearbeiten und Löschen von Aufgaben
- **Timer-Funktionalität**: Zeitmessung für Aufgaben mit Echtzeit-Updates
- **Benutzerauthentifizierung**: Sichere Anmeldung mit JWT-Token
- **Echtzeitkommunikation**: WebSocket-Integration für sofortige Updates
- **Datenpersistenz**: Speicherung aller Daten in einer relationalen Datenbank
- **REST API**: Umfassende API für die Integration mit anderen Anwendungen
- **Responsive Design**: Optimierte Benutzeroberfläche für verschiedene Gerätetypen

## Technologien

- **Backend**: Spring Boot 3.4.2
- **Sicherheit**: Spring Security mit JWT
- **Datenbank**: MySQL/MariaDB
- **Echtzeit-Kommunikation**: WebSocket mit STOMP
- **Dokumentation**: Swagger/OpenAPI
- **Build-Tool**: Maven
- **Container**: Docker

## Voraussetzungen

- Java 23 oder höher
- MySQL 8.x / MariaDB 10.x
- Maven 3.8+
- Docker und Docker Compose (für Container-Deployment)

## Umgebungen und Installation

TaskQuest unterstützt mehrere Deployment-Umgebungen:

1. [Lokale Entwicklungsumgebung](#lokale-entwicklungsumgebung)
2. [Docker-Deployment (x86_64)](#docker-deployment-x86_64)
3. [Raspberry Pi / ARM64-Deployment](#raspberry-pi--arm64-deployment)

### Umgebungsvariablen

TaskQuest verwendet Umgebungsvariablen für die Konfiguration. Eine Beispieldatei `.env.example` ist im Repository enthalten. Kopieren Sie diese Datei und passen Sie sie an Ihre Umgebung an:

```bash
# Für lokale Entwicklung
cp .env.example .env

# Für Docker-Deployment
cp .env.example .env.docker

# Für Raspberry Pi / ARM64-Deployment
cp .env.example .env.raspberry-pi
```

### Lokale Entwicklungsumgebung

1. Repository klonen:
   ```bash
   git clone https://github.com/yourusername/taskquest.git
   cd taskquest
   ```

2. Umgebungsvariablen konfigurieren:
   ```bash
   cp .env.example .env
   # Bearbeiten Sie die .env-Datei und passen Sie die Werte an
   ```

3. Datenbank vorbereiten:
   - MySQL/MariaDB-Server starten
   - Datenbank erstellen: `CREATE DATABASE taskquest;`
   - Benutzer und Berechtigungen einrichten (entsprechend Ihrer .env-Datei)

4. Anwendung bauen und starten:
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

5. Die Anwendung ist nun unter `http://localhost:8080` verfügbar

### Docker-Deployment (x86_64)

TaskQuest kann mit Docker und Docker Compose einfach bereitgestellt werden:

1. Repository klonen:
   ```bash
   git clone https://github.com/yourusername/taskquest.git
   cd taskquest
   ```

2. Umgebungsvariablen konfigurieren:
   ```bash
   cp .env.example .env.docker
   # Bearbeiten Sie die .env.docker-Datei und passen Sie die Werte an
   ```

3. Mit dem einheitlichen Docker Compose File starten:
   ```bash
   ENVIRONMENT=docker docker compose --profile docker up -d
   ```

   Alternativ können Sie auch das ursprüngliche Docker Compose File verwenden:
   ```bash
   docker compose up -d
   ```

4. Die Anwendung ist nun unter `http://localhost:8080` verfügbar

### Raspberry Pi / ARM64-Deployment

Für Raspberry Pi oder andere ARM64-basierte Geräte:

1. Repository klonen:
   ```bash
   git clone https://github.com/yourusername/taskquest.git
   cd taskquest
   ```

2. Umgebungsvariablen konfigurieren:
   ```bash
   cp .env.example .env.raspberry-pi
   # Bearbeiten Sie die .env.raspberry-pi-Datei und passen Sie die Werte an
   ```

3. Mit dem einheitlichen Docker Compose File starten:
   ```bash
   ENVIRONMENT=raspberry-pi TARGETARCH=arm64 DB_IMAGE=arm64v8/mariadb:10.6 DOCKERFILE=Dockerfile.arm64 docker compose --profile raspberry-pi up -d
   ```


4. Die Anwendung ist nun unter `http://localhost:8080` verfügbar

## API-Dokumentation

Nach dem Start der Anwendung ist die API-Dokumentation unter folgender URL verfügbar:
[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

## WebSocket-Endpunkte

Die Anwendung verwendet WebSockets für Echtzeit-Updates, insbesondere für die Timer-Funktionalität:

- **STOMP-Endpunkt**: `/ws`
- **Application Destination Prefix**: `/app`
- **Topic für Timer-Updates**: `/topic/task/{taskId}/timer`

## Entwicklung

### Projektstruktur

- `src/main/java/org/novize/api/` - Java-Quellcode
  - `auth` - Authentifizierungslogik
  - `config` - Konfigurationsklassen
  - `controller` - REST-Controller
  - `dtos` - Data Transfer Objects
  - `model` - Entitätsklassen
  - `repository` - Datenbankzugriff
  - `services` - Geschäftslogik

### Multi-Environment-Setup

TaskQuest verwendet Spring Profiles für umgebungsspezifische Konfigurationen:

- **Default**: Verwendet `application.properties` und `.env`
- **Docker**: Verwendet `application-docker.properties` und `.env.docker`
- **Raspberry Pi**: Verwendet `application-raspberry-pi.properties` und `.env.raspberry-pi`

Weitere Informationen zum Multi-Environment-Setup finden Sie in der Datei [MULTI-ENVIRONMENT-SETUP.md](MULTI-ENVIRONMENT-SETUP.md).

## Fehlerbehebung

### Allgemeine Probleme

1. **Datenbank-Verbindungsprobleme**:
   - Überprüfen Sie, ob die Datenbank läuft und erreichbar ist
   - Stellen Sie sicher, dass die Datenbank-Anmeldedaten in der .env-Datei korrekt sind
   - Überprüfen Sie, ob der Datenbankbenutzer die erforderlichen Berechtigungen hat

2. **Umgebungsvariablen-Warnungen**:
   - Wenn Sie Warnungen über nicht gesetzte Umgebungsvariablen sehen, stellen Sie sicher, dass die entsprechende .env-Datei existiert und korrekt konfiguriert ist
   - Bei Docker-Deployment: Stellen Sie sicher, dass die .env-Datei korrekt in den Container gemountet wird

3. **Port-Konflikte**:
   - Wenn der Port 8080 bereits verwendet wird, ändern Sie den PORT-Wert in der .env-Datei

### Docker-spezifische Probleme

1. **Container startet nicht**:
   - Überprüfen Sie die Container-Logs: `docker logs taskquest-app`
   - Überprüfen Sie die Datenbank-Logs: `docker logs taskquest-db-1`

2. **Healthcheck-Fehler**:
   - Stellen Sie sicher, dass die Datenbank korrekt initialisiert wird
   - Überprüfen Sie, ob die Datenbank-Anmeldedaten in der .env-Datei mit denen im init-db.sh-Skript übereinstimmen

### Raspberry Pi / ARM64-spezifische Probleme

1. **Speicherprobleme**:
   - Raspberry Pi hat begrenzten Speicher. Das ARM64-Dockerfile enthält Speichereinstellungen für Java (-Xmx512m, -Xms256m), um den Speicherverbrauch zu begrenzen.
   - Wenn Sie weiterhin Speicherprobleme haben, erwägen Sie, den Swap-Speicher zu erhöhen.

2. **Build-Fehler**:
   - Wenn der Build fehlschlägt, versuchen Sie, mit ausführlicher Ausgabe zu bauen:
     ```bash
     DOCKER_BUILDKIT=1 docker buildx build --platform linux/arm64 --build-arg TARGETARCH=arm64 -t taskquest-app:latest --load -f Dockerfile.arm64 . --progress=plain
     ```

3. **Performance-Probleme**:
   - Raspberry Pi hat begrenzte CPU-Leistung. Erwägen Sie, die Anwendung für geringeren Ressourcenverbrauch zu optimieren, wenn die Leistung ein Problem darstellt.

## Beitrag zum Projekt

1. Fork des Repositories erstellen
2. Feature-Branch anlegen (`git checkout -b feature/AmazingFeature`)
3. Änderungen committen (`git commit -m 'Add some AmazingFeature'`)
4. Branch pushen (`git push origin feature/AmazingFeature`)
5. Pull Request erstellen

## Weitere Ressourcen

- [Spring Boot Dokumentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Docker Dokumentation](https://docs.docker.com/)
- [Raspberry Pi Dokumentation](https://www.raspberrypi.org/documentation/)
