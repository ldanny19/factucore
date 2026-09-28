#!/usr/bin/env bash
set -euo pipefail

echo "=============================================="
echo " FACTUCORE MESSAGING - BUILD"
echo "=============================================="

echo "==> Compilando y publicando en Maven local..."

mvn clean install

echo
echo "==> factucore-messaging compilado correctamente."
echo "==> Artefacto instalado en Maven Local."
