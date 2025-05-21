#!/bin/bash
set -e

echo "Starting database initialization..."

# Umgebungsvariablen ausgeben (ohne Passwörter)
echo "Using environment variables:"
echo "MARIADB_HOST: ${MARIADB_HOST}"
echo "MARIADB_PORT: ${MARIADB_PORT}"
echo "MARIADB_DB: ${MARIADB_DB}"
echo "MARIADB_USER: ${MARIADB_USER}"
echo "DOCKER_COMPOSE_ENV: ${DOCKER_COMPOSE_ENV}"

# Rest des Skripts bleibt gleich
if [ -z "$MARIADB_ROOT_PASSWORD" ] || [ -z "$MARIADB_DB" ] || [ -z "$MARIADB_USER" ] || [ -z "$MARIADB_PASSWORD" ]; then
    echo "Some environment variables are not set. Attempting to source from .env file."
    # ... (bisheriger Code)
else
    echo "Environment variables are already set."
fi

echo "Executing SQL commands to setup database and users..."
mysql -u root -p${MARIADB_ROOT_PASSWORD} <<-EOSQL
CREATE DATABASE IF NOT EXISTS ${MARIADB_DB};
DROP USER IF EXISTS '${MARIADB_USER}'@'%';
DROP USER IF EXISTS '${MARIADB_USER}'@'localhost';
CREATE USER '${MARIADB_USER}'@'%' IDENTIFIED BY "${MARIADB_PASSWORD}";
CREATE USER '${MARIADB_USER}'@'localhost' IDENTIFIED BY "${MARIADB_PASSWORD}";
GRANT ALL PRIVILEGES ON ${MARIADB_DB}.* TO '${MARIADB_USER}'@'%';
GRANT ALL PRIVILEGES ON ${MARIADB_DB}.* TO '${MARIADB_USER}'@'localhost';
FLUSH PRIVILEGES;
EOSQL
echo "Database initialization completed successfully."