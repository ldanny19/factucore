#!/bin/sh
set -eu

SECRETS_DIR="/vault/secrets"

[ -s "$SECRETS_DIR/basedatos_admin_password" ] || {
  echo "Falta el secreto de administrador de base de datos." >&2
  exit 1
}
[ -s "$SECRETS_DIR/basedatos_facturador_password" ] || {
  echo "Falta el secreto de base de datos de Facturador." >&2
  exit 1
}
[ -s "$SECRETS_DIR/basedatos_auth_password" ] || {
  echo "Falta el secreto de base de datos de Auth." >&2
  exit 1
}
[ -s "$SECRETS_DIR/basedatos_notificaciones_password" ] || {
  echo "Falta el secreto de base de datos de Notificaciones." >&2
  exit 1
}

export PGPASSWORD="$(cat "$SECRETS_DIR/basedatos_admin_password")"

psql \
  --host=db_factucore \
  --port=5432 \
  --username=postgres \
  --dbname=postgres \
  --set=ON_ERROR_STOP=1 \
  --set=facturador_password="$(cat "$SECRETS_DIR/basedatos_facturador_password")" \
  --set=auth_password="$(cat "$SECRETS_DIR/basedatos_auth_password")" \
  --set=notificaciones_password="$(cat "$SECRETS_DIR/basedatos_notificaciones_password")" <<'SQL'
ALTER ROLE usr_factucore PASSWORD :'facturador_password';
ALTER ROLE usr_auth PASSWORD :'auth_password';
ALTER ROLE usr_notificaciones PASSWORD :'notificaciones_password';
SQL

echo "Credenciales de los roles de servicio sincronizadas con Vault."
