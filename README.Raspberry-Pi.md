# TaskQuest on Raspberry Pi

This document provides instructions for building and running TaskQuest on a Raspberry Pi or other ARM64-based devices.

## Prerequisites

- Raspberry Pi 4 or newer with at least 4GB RAM (recommended)
- Raspberry Pi OS (64-bit) or other ARM64-based Linux distribution
- Docker and Docker Compose installed
- Git installed

## Setup Instructions

1. Clone the repository:
   ```
   git clone https://github.com/yourusername/taskquest.git
   cd taskquest
   ```

2. Use the Raspberry Pi specific environment file:
   ```
   # The docker-compose.arm64.yaml file is already configured to use .env.raspberry-pi
   # No additional steps are needed
   ```

3. Build and run using the ARM64-specific configuration:
   ```
   docker-compose -f docker-compose.arm64.yaml up -d
   ```

   Alternatively, if you're building on a non-ARM64 machine for deployment on Raspberry Pi, use the build script:
   ```
   chmod +x build.sh
   ./build.sh
   ```

4. Access the application at `http://localhost:8080`

## Recent Fixes for Raspberry Pi Compatibility

The following issues have been fixed to ensure proper functionality on Raspberry Pi:

1. **Fixed Dockerfile.arm64**:
   - Removed duplicate ARG UID declaration
   - Added missing WORKDIR directive
   - Standardized user setup to match the regular Dockerfile

2. **Fixed docker-compose.arm64.yaml**:
   - Corrected database user in healthcheck configuration to match .env.docker
   - Added missing environment variables for MariaDB (MARIADB_ROOT_PASSWORD, MARIADB_DB, MARIADB_USER, MARIADB_PASSWORD)
   - Ensured consistent configuration with the regular docker-compose.yaml

3. **Standardized docker-compose.yaml**:
   - Added network configuration
   - Added restart policies
   - Updated healthcheck configuration
   - Added environment variable substitution for PORT and UPLOAD_DIR

4. **Fixed Environment Variable Warnings**:
   - Added instructions for handling environment variables properly
   - Updated setup instructions to include environment variable configuration
   - Added detailed troubleshooting information for environment variable warnings

5. **Created Raspberry Pi Specific Environment File**:
   - Created a dedicated .env.raspberry-pi file with correct variables for ARM64 MariaDB
   - Updated docker-compose.arm64.yaml to use .env.raspberry-pi instead of .env.docker
   - Added explicit environment variable settings in docker-compose.arm64.yaml

## Troubleshooting

If you encounter issues with the build or deployment, try the following:

1. **Environment Variable Warnings**: If you see warnings about environment variables not being set (e.g., "WARNING: The MARIADB_DB variable is not set"), try one of these solutions:

   - Option 1 (Recommended): Create a .env file with the same content as .env.raspberry-pi:
     ```
     # Copy the content of .env.raspberry-pi to a new .env file
      b
     # Then run docker-compose
     docker-compose -f docker-compose.arm64.yaml up -d
     ```

   - Option 2: Explicitly specify the environment file when running docker-compose:
     ```
     docker-compose -f docker-compose.arm64.yaml --env-file .env.raspberry-pi up -d
     ```

   - If you're still seeing warnings, check that the .env.raspberry-pi file exists and contains the correct variables.
   - Make sure you're using the correct variable names in your docker-compose.arm64.yaml file. The ARM64 version of MariaDB expects MARIADB_DB instead of MARIADB_DATABASE.

2. **Database Connection Issues**: If you encounter database connection issues (e.g., "Access denied for user 'taskquest_user'"), try the following:

   - Make sure you're using the correct database credentials in .env.raspberry-pi
   - Check that the database initialization script (init.sql) is being executed properly
   - Ensure that there are no conflicting environment variables in other .env files
   - The application is configured to load environment variables from multiple files in the following order: .env.raspberry-pi, .env.docker, .env, ../.env, .env.test
   - If you have multiple .env files with different credentials, make sure .env.raspberry-pi takes precedence
   - Note that the docker-compose.arm64.yaml file now explicitly sets the database credentials to match those in init.sql, which should resolve most connection issues
   - If you're still having issues, try removing the .env file or ensuring that it doesn't contain conflicting database credentials

3. **Memory Issues**: Raspberry Pi has limited memory. The ARM64 Dockerfile includes memory settings for Java (-Xmx512m, -Xms256m) to limit memory usage. If you're still experiencing memory issues, consider increasing the swap space.

4. **Check Logs**: If you're still having issues, check the logs:
   ```
   docker-compose -f docker-compose.arm64.yaml logs app
   docker-compose -f docker-compose.arm64.yaml logs db
   ```

5. **Build Failures**: If the build fails, try building with verbose output:
   ```
   DOCKER_BUILDKIT=1 docker buildx build --platform linux/arm64 --build-arg TARGETARCH=arm64 -t taskquest-app:latest --load -f Dockerfile.arm64 . --progress=plain
   ```

6. **Performance Issues**: Raspberry Pi has limited CPU power. Consider optimizing the application for lower resource usage if performance is an issue.

## Additional Resources

- [Docker on Raspberry Pi Documentation](https://docs.docker.com/engine/install/debian/)
- [Raspberry Pi Documentation](https://www.raspberrypi.org/documentation/)
