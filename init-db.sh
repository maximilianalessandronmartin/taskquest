#!/bin/bash

# This script processes the init.sql.template file to replace environment variables
# with their values from the .env file, and outputs the result to init.sql

# Ensure the script fails on any error
set -e

echo "Processing init.sql.template..."

# Use envsubst to replace environment variables in the template
envsubst < /docker-entrypoint-initdb.d/init.sql.template > /docker-entrypoint-initdb.d/10-init.sql

echo "Generated 10-init.sql with environment variables."
echo "Docker will execute this file during database initialization."

# Continue with the default entrypoint
exec /usr/local/bin/docker-entrypoint.sh mysqld
