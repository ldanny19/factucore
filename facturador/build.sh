#!/usr/bin/env bash
set -euo pipefail

echo "=============================================="
echo " FACTUCORE FACTURADOR - BUILD"
echo "=============================================="

MODE="${1:-}"
VERSION="${2:-}"
FORCE="${3:-}"
IMAGE_REGISTRY="host.docker.internal:5000"

if [[ -z "${MODE}" || -z "${VERSION}" ]]; then
  echo "ERROR: debe indicar modo y version."
  echo "Uso: ./build.sh [skipTests] <version> [force]"
  exit 1
fi

if [[ "${MODE}" != "skipTests" ]]; then
  echo "ERROR: parametro invalido: ${MODE}"
  echo "Uso: ./build.sh [skipTests] <version> [force]"
  exit 1
fi

if [[ -n "${FORCE}" && "${FORCE}" != "force" ]]; then
  echo "ERROR: parametro invalido: ${FORCE}"
  echo "Uso: ./build.sh [skipTests] <version> [force]"
  exit 1
fi

echo "==> Ejecutando Maven sin tests..."
mvn clean package -DskipTests
DOCKER_BUILD_ARGS=(
  --build-arg APP_VERSION="${VERSION}"
  -f Dockerfile
  -t "factucore-facturador:${VERSION}"
)

if [[ "${FORCE}" == "force" ]]; then
  echo "==> Forzando reconstruccion de la imagen Docker sin cache..."
  DOCKER_BUILD_ARGS+=(--no-cache)
else
  echo "==> Generando imagen Docker usando cache: factucore-facturador:${VERSION}"
fi

docker build "${DOCKER_BUILD_ARGS[@]}" .

echo
echo "==> FACTURADOR compilado correctamente."
echo "==> Imagen generada: factucore-facturador:${VERSION}"

echo
echo "==> Etiquetando imagen para el registry..."
docker tag "factucore-facturador:${VERSION}" \
  "${IMAGE_REGISTRY}/factucore-facturador:${VERSION}"

echo
echo "==> Publicando imagen..."
docker push "${IMAGE_REGISTRY}/factucore-facturador:${VERSION}"

echo
echo "==> Imagen publicada: ${IMAGE_REGISTRY}/factucore-facturador:${VERSION}"
