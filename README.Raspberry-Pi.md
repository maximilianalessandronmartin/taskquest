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

2. Build and run using the ARM64-specific configuration:
   ```
   docker-compose -f docker-compose.arm64.yaml up -d
   ```

   Alternatively, if you're building on a non-ARM64 machine for deployment on Raspberry Pi, use the build script:
   ```
   chmod +x build.sh
   ./build.sh
   ```

3. Access the application at `http://localhost:8080`

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

## Troubleshooting

If you encounter issues with the build or deployment, try the following:

1. **Environment Variable Warnings**: If you see warnings about environment variables not being set (e.g., "WARNING: The MARIADB_DB variable is not set"), make sure you're using the correct variable names in your docker-compose.arm64.yaml file. The ARM64 version of MariaDB expects MARIADB_DB instead of MARIADB_DATABASE.

2. **Memory Issues**: Raspberry Pi has limited memory. The ARM64 Dockerfile includes memory settings for Java (-Xmx512m, -Xms256m) to limit memory usage. If you're still experiencing memory issues, consider increasing the swap space.

3. **Database Connection Issues**: If the application can't connect to the database, check the logs:
   ```
   docker-compose -f docker-compose.arm64.yaml logs app
   docker-compose -f docker-compose.arm64.yaml logs db
   ```

4. **Build Failures**: If the build fails, try building with verbose output:
   ```
   DOCKER_BUILDKIT=1 docker buildx build --platform linux/arm64 --build-arg TARGETARCH=arm64 -t taskquest-app:latest --load -f Dockerfile.arm64 . --progress=plain
   ```

5. **Performance Issues**: Raspberry Pi has limited CPU power. Consider optimizing the application for lower resource usage if performance is an issue.

## Additional Resources

- [Docker on Raspberry Pi Documentation](https://docs.docker.com/engine/install/debian/)
- [Raspberry Pi Documentation](https://www.raspberrypi.org/documentation/)
