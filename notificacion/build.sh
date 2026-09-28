#!/usr/bin/env bash
set -euo pipefail

echo "=============================================="
echo " FACTUCORE NOTIFICACION - BUILD"
echo "=============================================="

MODE="${1:-}"

if [[ -z "${MODE}" ]]; then
  echo
  echo "Seleccione el modo de compilacion:"
  echo "  1) skip-tests  - Compilar sin ejecutar tests"
  echo "  2) tests       - Compilar ejecutando tests"
  echo
  read -r -p "Opcion [1/2]: " OPTION
  case "${OPTION}" in
    1) MODE="skip-tests" ;;
    2) MODE="tests" ;;
    *) echo "ERROR: opcion invalida."; exit 1 ;;
  esac
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
    echo "Uso: ./build.sh [skip-tests|tests]"
    exit 1
    ;;
esac
echo '==> Generando imagen Docker...'
docker build -f Dockerfile -t factucore-notificacion:local .

echo
echo "==> NOTIFICACION compilado correctamente."
echo '==> Imagen generada: factucore-notificacion:local'