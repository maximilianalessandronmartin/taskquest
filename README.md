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

Die Anwendung kann auch mit Docker und Docker Compose ausgeführt werden:

```
docker-compose up -d
```

Für ARM64-Architekturen (z.B. Apple Silicon oder Raspberry Pi):

Option 1: Erstellen Sie eine .env Datei mit den korrekten Variablen (empfohlen):
```
# Kopieren Sie den Inhalt von .env.raspberry-pi in eine neue .env Datei
cp .env.raspberry-pi .env
# Oder unter Windows:
# copy .env.raspberry-pi .env

# Starten Sie die Anwendung mit dem ARM64-spezifischen Docker Compose File
docker-compose -f docker-compose.arm64.yaml up -d
```

Option 2: Verwenden Sie die --env-file Flag, um die Umgebungsvariablen explizit anzugeben:
```
docker-compose -f docker-compose.arm64.yaml --env-file .env.raspberry-pi up -d
```

Weitere Informationen finden Sie in den Dateien:
- `README.Docker.md` - Allgemeine Docker-Informationen
- `README.Raspberry-Pi.md` - Spezifische Anweisungen für Raspberry Pi

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
