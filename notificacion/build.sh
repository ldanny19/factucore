#!/usr/bin/env bash
set -euo pipefail

echo "=============================================="
echo " FACTUCORE NOTIFICACION - BUILD"
echo "=============================================="

echo "==> Compilando y generando JAR..."

mvn clean package

echo "==> Generando imagen Docker..."

docker build \
  -f Dockerfile \
  -t factucore-notificacion:local \
  .

echo
echo "==> Notificacion compilado correctamente."
echo "==> Imagen generada: factucore-notificacion:local"
