#!/usr/bin/env bash
set -euo pipefail

echo "=============================================="
echo " FACTUCORE FACTURADOR - BUILD"
echo "=============================================="

MODE="${1:-}"
VERSION="${2:-}"

if [[ -z "${MODE}" || -z "${VERSION}" ]]; then
  echo "ERROR: debe indicar modo y version."
  echo "Uso: ./build.sh [skip-tests|tests] <version>"
  exit 1
fi

case "${MODE}" in
  skip-tests)
    echo "==> Ejecutando Maven sin tests..."
    mvn clean package -DskipTests
    ;;
  tests)
    echo "==> Ejecutando Maven con tests..."
    mvn clean package
    ;;
  *)
    echo "ERROR: modo invalido: ${MODE}"
    echo "Uso: ./build.sh [skip-tests|tests] <version>"
    exit 1
    ;;
esac

echo "==> Generando imagen Docker: factucore-facturador:${VERSION}"
docker build --build-arg APP_VERSION="${VERSION}" -f Dockerfile -t "factucore-facturador:${VERSION}" .

echo
echo "==> FACTURADOR compilado correctamente."
echo "==> Imagen generada: factucore-facturador:${VERSION}"
