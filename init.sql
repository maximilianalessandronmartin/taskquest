-- Datenbank erstellen (falls sie nicht existiert)
CREATE DATABASE IF NOT EXISTS taskquest;

-- Benutzer erstellen und Passwort setzen
CREATE USER IF NOT EXISTS 'taskquest_user'@'%' IDENTIFIED BY 'secure_password';

-- Berechtigungen erteilen
GRANT ALL PRIVILEGES ON taskquest.* TO 'taskquest_user'@'%';

-- Berechtigungen aktualisieren
FLUSH PRIVILEGES;

-- Verwenden Sie die taskquest-Datenbank
USE taskquest;

-- Hier können Sie weitere SQL-Befehle einfügen,
-- wie z.B. Tabellen erstellen oder Daten einfügen