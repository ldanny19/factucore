#!/usr/bin/env bash
set -euo pipefail

echo "=============================================="
echo " FACTUCORE FACTURADOR - BUILD"
echo "=============================================="

echo "==> Compilando y generando JAR..."

mvn clean package

echo "==> Generando imagen Docker..."

docker build \
  -f Dockerfile \
  -t factucore-facturador:local \
  .

echo
echo "==> Facturador compilado correctamente."
echo "==> Imagen generada: factucore-facturador:local"
