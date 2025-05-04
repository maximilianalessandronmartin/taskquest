#!/bin/bash

# Prüfen, ob arm64builder bereits existiert und gegebenenfalls verwenden
if docker buildx inspect arm64builder >/dev/null 2>&1; then
    echo "Builder 'arm64builder' existiert bereits, wird verwendet"
    docker buildx use arm64builder
else
    echo "Erstelle neuen buildx-Builder 'arm64builder'"
    docker buildx create --name arm64builder --platform linux/arm64
    docker buildx use arm64builder
fi

# QEMU für Cross-Platform-Builds installieren
echo "Installiere QEMU für Cross-Plattform-Builds"
docker run --privileged --rm tonistiigi/binfmt --install all

# Direktes Bauen des ARM64-Image
echo "Baue Image für Raspberry Pi (ARM64)"
DOCKER_BUILDKIT=1 docker buildx build --platform linux/arm64 \
  --build-arg TARGETARCH=arm64 \
  -t taskquest-app:latest \
  --load \
  -f Dockerfile.arm64 .

echo "Build abgeschlossen! Das Image ist für den Raspberry Pi 4 (ARM64) optimiert."