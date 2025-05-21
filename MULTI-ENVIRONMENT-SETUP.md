# Best Practices for Multi-Environment Setup in TaskQuest

This document outlines best practices for maintaining a clean and efficient multi-environment setup for the TaskQuest application. It covers environment configuration, Docker setup, and deployment strategies.

## Current Setup Overview

TaskQuest currently supports multiple deployment environments:

1. **Local Development**: Using local .env file and default application.properties
2. **Docker**: Using .env.docker and application-docker.properties
3. **Raspberry Pi**: Using .env.raspberry-pi and application-raspberry-pi.properties

## Recommended Best Practices

### 1. Environment Variable Management

#### Current Approach
- Multiple .env files (.env, .env.docker, .env.raspberry-pi)
- Spring imports environment variables from these files
- Docker Compose files set some environment variables and use env_file directive

#### Recommended Improvements
- **Use a consistent naming convention**: Keep using the pattern `.env.<environment>` for all environments
- **Create a .env.example file**: Provide a template with all required variables but no sensitive values
- **Add .env files to .gitignore**: Prevent sensitive information from being committed to the repository
- **Document all environment variables**: Create a section in documentation explaining each variable
- **Use environment-specific defaults**: Set sensible defaults for each environment in the application properties files

```
# Example .env.example file structure
APP_NAME=TaskQuest
PORT=8080
UPLOAD_DIR=/app/uploads

# Database configuration
MARIADB_HOST=localhost
MARIADB_PORT=3306
MARIADB_DB=taskquest
MARIADB_USER=user
MARIADB_PASSWORD=password
MARIADB_ROOT_PASSWORD=root_password

# URLs
BACKEND_URL=http://localhost:8080
FRONTEND_URL=http://localhost:3000

# JWT Configuration
JWT_SECRET_KEY=your_secret_key
JWT_TOKEN_EXPIRATION_TIME=3600000
REFRESH_TOKEN_EXPIRATION_TIME=604800000
TOKEN_TYPE=Bearer
```

### 2. Spring Boot Configuration

#### Current Approach
- Base application.properties with profile-specific properties files
- Each properties file imports from a specific .env file
- Profiles activated via SPRING_PROFILES_ACTIVE environment variable

#### Recommended Improvements
- **Minimize duplication**: Extract common properties to application.properties
- **Use Spring's property hierarchy**: Take advantage of Spring's property resolution order
- **Consider using YAML**: For more structured configuration (application.yml instead of .properties)
- **Add comments**: Document the purpose of each property and its relationship to environment variables

```yaml
# Example application.yml structure
spring:
  application:
    name: ${APP_NAME:TaskQuest}
  datasource:
    url: jdbc:mariadb://${MARIADB_HOST:localhost}:${MARIADB_PORT:3306}/${MARIADB_DB:taskquest}
    username: ${MARIADB_USER:user}
    password: ${MARIADB_PASSWORD:password}
    driver-class-name: org.mariadb.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        format_sql: true
    defer-datasource-initialization: true
    show-sql: false
    open-in-view: false

# Profile-specific configuration can override these defaults
```

### 3. Docker and Docker Compose Configuration

#### Current Approach
- Separate docker-compose.yaml and docker-compose.arm64.yaml
- Different Dockerfiles for different architectures
- Environment variables set in Docker Compose files
- Init script for database initialization

#### Recommended Improvements
- **Use Docker Compose profiles**: Instead of separate files, use profiles within a single docker-compose.yaml
- **Standardize service configurations**: Ensure consistent naming, networking, and health checks
- **Use build arguments**: For architecture-specific builds
- **Implement proper health checks**: Ensure services start in the correct order
- **Use Docker secrets**: For sensitive information (in production environments)

```yaml
# Example docker-compose.yaml with profiles
version: '3.8'

services:
  db:
    profiles: [default, docker, raspberry-pi]
    image: ${DB_IMAGE:-mariadb:10.6}
    env_file:
      - .env.${ENVIRONMENT:-local}
    environment:
      MARIADB_ROOT_PASSWORD: ${MARIADB_ROOT_PASSWORD}
      MARIADB_DB: ${MARIADB_DB}
      MARIADB_USER: ${MARIADB_USER}
      MARIADB_PASSWORD: ${MARIADB_PASSWORD}
    ports:
      - "${DB_PORT:-3306}:3306"
    volumes:
      - db-data:/var/lib/mysql
      - ./init-db.sh:/docker-entrypoint-initdb.d/init-db.sh
      - ./.env.${ENVIRONMENT:-local}:/docker-entrypoint-initdb.d/.env.${ENVIRONMENT:-local}
    command: --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci --default-authentication-plugin=mysql_native_password
    networks:
      - taskquest-network
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-u", "root", "-p${MARIADB_ROOT_PASSWORD}"]
      interval: 10s
      timeout: 5s
      retries: 5
      start_period: 30s

  app:
    profiles: [default, docker, raspberry-pi]
    build:
      context: .
      dockerfile: ${DOCKERFILE:-Dockerfile}
      args:
        - TARGETARCH=${TARGETARCH:-amd64}
    ports:
      - "${PORT:-8080}:8080"
    environment:
      SPRING_PROFILES_ACTIVE: ${ENVIRONMENT:-local}
    env_file:
      - .env.${ENVIRONMENT:-local}
    depends_on:
      db:
        condition: service_healthy
    volumes:
      - ${UPLOAD_DIR:-./uploads}:/app/uploads
    networks:
      - taskquest-network

networks:
  taskquest-network:
    driver: bridge

volumes:
  db-data:
  uploads:
```

### 4. Initialization and Migration Scripts

#### Current Approach
- init-db.sh script for database initialization
- Script sources environment variables from .env files

#### Recommended Improvements
- **Use Flyway or Liquibase**: For database migrations and versioning
- **Simplify initialization script**: Make it more robust and easier to maintain
- **Add validation**: Check for required environment variables before executing

### 5. Documentation

#### Current Approach
- Multiple README files (README.md, README.Docker.md, README.Raspberry-Pi.md)
- Some troubleshooting information in README.Raspberry-Pi.md

#### Recommended Improvements
- **Unified documentation**: Create a single comprehensive README with sections for each environment
- **Environment setup guide**: Clear instructions for setting up each environment
- **Troubleshooting guide**: Common issues and solutions for all environments
- **Development workflow**: Document the recommended workflow for development and deployment

### 6. CI/CD Integration

#### Recommended Approach
- **Environment-specific pipelines**: Configure CI/CD pipelines for each environment
- **Automated testing**: Run tests in environment-specific containers
- **Deployment automation**: Automate deployment to different environments
- **Environment variables in CI/CD**: Securely manage environment variables in CI/CD systems

## Implementation Plan

1. **Refactor environment variable management**:
   - Create .env.example file
   - Update .gitignore
   - Document all environment variables

2. **Optimize Spring Boot configuration**:
   - Extract common properties
   - Consider converting to YAML
   - Add comments

3. **Consolidate Docker configuration**:
   - Implement Docker Compose profiles
   - Standardize service configurations
   - Improve health checks

4. **Enhance database initialization**:
   - Consider implementing Flyway or Liquibase
   - Improve initialization script

5. **Improve documentation**:
   - Create unified README
   - Add environment setup guides
   - Create troubleshooting section

6. **Set up CI/CD integration**:
   - Configure environment-specific pipelines
   - Implement automated testing
   - Set up deployment automation

## Conclusion

Following these best practices will result in a cleaner, more maintainable multi-environment setup for the TaskQuest application. The recommended approach leverages Spring Boot's built-in capabilities for environment-specific configuration, Docker Compose's flexibility for defining different service configurations, and modern DevOps practices for managing environment variables and deployments.

By implementing these recommendations, the TaskQuest team will benefit from:

1. **Reduced complexity**: Simplified configuration and deployment process
2. **Improved maintainability**: Clearer structure and better documentation
3. **Enhanced security**: Better management of sensitive information
4. **Increased reliability**: More robust initialization and health checks
5. **Streamlined workflow**: Clearer development and deployment process