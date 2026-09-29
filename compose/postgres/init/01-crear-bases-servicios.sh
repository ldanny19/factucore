#!/bin/bash
set -euo pipefail

FACTURADOR_PASSWORD_FILE="/vault/secrets/postgres_facturador_password"
AUTH_PASSWORD_FILE="/vault/secrets/postgres_auth_password"
NOTIFICACIONES_PASSWORD_FILE="/vault/secrets/postgres_notificaciones_password"

for file in "$FACTURADOR_PASSWORD_FILE" "$AUTH_PASSWORD_FILE" "$NOTIFICACIONES_PASSWORD_FILE"; do
  if [ ! -s "$file" ]; then
    echo "ERROR: no existe o esta vacio el secreto $file" >&2
    exit 1
  fi
done

FACTURADOR_PASSWORD="$(cat "$FACTURADOR_PASSWORD_FILE")"
AUTH_PASSWORD="$(cat "$AUTH_PASSWORD_FILE")"
NOTIFICACIONES_PASSWORD="$(cat "$NOTIFICACIONES_PASSWORD_FILE")"

echo "==> Creando rol usr_factucore"
if ! psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname postgres -tAc "SELECT 1 FROM pg_roles WHERE rolname = 'usr_factucore'" | grep -q 1; then
  psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname postgres \
    -v facturador_password="$FACTURADOR_PASSWORD" \
    -c "CREATE ROLE usr_factucore LOGIN PASSWORD :'facturador_password';"
fi

echo "==> Creando rol usr_auth"
if ! psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname postgres -tAc "SELECT 1 FROM pg_roles WHERE rolname = 'usr_auth'" | grep -q 1; then
  psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname postgres \
    -v auth_password="$AUTH_PASSWORD" \
    -c "CREATE ROLE usr_auth LOGIN PASSWORD :'auth_password';"
fi

echo "==> Creando rol usr_notificaciones"
if ! psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname postgres -tAc "SELECT 1 FROM pg_roles WHERE rolname = 'usr_notificaciones'" | grep -q 1; then
  psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname postgres \
    -v notificaciones_password="$NOTIFICACIONES_PASSWORD" \
    -c "CREATE ROLE usr_notificaciones LOGIN PASSWORD :'notificaciones_password';"
fi

echo "==> Creando base db_factucore"
if ! psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname postgres -tAc "SELECT 1 FROM pg_database WHERE datname = 'db_factucore'" | grep -q 1; then
  psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname postgres \
    -c "CREATE DATABASE db_factucore OWNER usr_factucore;"
fi

echo "==> Creando base db_auth"
if ! psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname postgres -tAc "SELECT 1 FROM pg_database WHERE datname = 'db_auth'" | grep -q 1; then
  psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname postgres \
    -c "CREATE DATABASE db_auth OWNER usr_auth;"
fi

echo "==> Creando base db_notificaciones"
if ! psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname postgres -tAc "SELECT 1 FROM pg_database WHERE datname = 'db_notificaciones'" | grep -q 1; then
  psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname postgres \
    -c "CREATE DATABASE db_notificaciones OWNER usr_notificaciones;"
fi

echo "==> Bases y roles de FactuCore creados correctamente"