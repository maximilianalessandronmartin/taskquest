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

## Voraussetzungen

- Java 23 oder höher
- MySQL 8.x / MariaDB 10.x
- Maven 3.8+

## Installation

### Lokale Entwicklungsumgebung

1. Repository klonen:
   ```
   git clone https://github.com/yourusername/taskquest.git
   cd taskquest
   ```

2. Datenbank vorbereiten:
    - MySQL/MariaDB-Server starten
    - Datenbank erstellen: `CREATE DATABASE taskquest;`
    - Initialisierungsskript ausführen: `mysql -u username -p taskquest < init.sql`

3. Anwendung bauen und starten:
   ```
   mvn clean install
   mvn spring-boot:run
   ```

4. Die Anwendung ist nun unter `http://localhost:8080` verfügbar

### Docker-Installation

Die Anwendung kann mit Docker und Docker Compose ausgeführt werden. Wir verwenden ein einheitliches Docker Compose File mit Profilen für verschiedene Umgebungen:

```
# Standard (x86_64)
docker compose up -d

# Docker (x86_64) mit explizitem Profil
ENVIRONMENT=docker docker compose --profile docker up -d

# Raspberry Pi (ARM64)
ENVIRONMENT=raspberry-pi TARGETARCH=arm64 DB_IMAGE=arm64v8/mariadb:10.6 DOCKERFILE=Dockerfile.arm64 docker compose --profile raspberry-pi up -d
```

Weitere Informationen finden Sie in der Datei:
- `README.unified.md` - Umfassende Dokumentation für alle Umgebungen

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

### Beitrag zum Projekt

1. Fork des Repositories erstellen
2. Feature-Branch anlegen (`git checkout -b feature/AmazingFeature`)
3. Änderungen committen (`git commit -m 'Add some AmazingFeature'`)
4. Branch pushen (`git push origin feature/AmazingFeature`)
5. Pull Request erstellen
