#!/bin/sh
set -eu

VAULT_ADDR="${VAULT_ADDR:-http://127.0.0.1:8200}"
export VAULT_ADDR

CONFIG_FILE="/vault/config/secretos.conf"
INIT_FILE="${FACTUCORE_VAULT_SECRETS_DIR:-/vault/secrets-persist}/vault-init.json"

fail() {
  printf 'ERROR: %s\n' "$1" >&2
  exit 1
}

[ -r "$CONFIG_FILE" ] || fail "No existe el catalogo de secretos: $CONFIG_FILE"
[ -r "$INIT_FILE" ] || fail "No existe el archivo de inicializacion de Vault."

ROOT_TOKEN="${FACTUCORE_VAULT_ROOT_TOKEN:-}"
if [ -z "$ROOT_TOKEN" ]; then
  ROOT_TOKEN=$(
    grep -o '"root_token"[[:space:]]*:[[:space:]]*"[^"]*"' "$INIT_FILE" |
      head -1 |
      cut -d '"' -f 4
  )
fi
[ -n "$ROOT_TOKEN" ] || fail "No se encontro el root token de Vault."
export VAULT_TOKEN="$ROOT_TOKEN"

# Cada contrasena llega por stdin. No se guarda en archivos ni se incluye
# como argumento del comando Vault. Las rutas y usuarios provienen del catalogo.
exec 3<&0

while IFS='|' read -r KEY SECRET_PATH USERNAME DESCRIPTION; do
  case "$KEY" in
    ""|\#*) continue ;;
  esac

  if vault kv get "$SECRET_PATH" >/dev/null 2>&1; then
    printf 'El secreto ya existe; se conserva: %s\n' "$SECRET_PATH"
    continue
  fi

  PASSWORD=""
  IFS= read -r PASSWORD <&3 || [ -n "$PASSWORD" ]
  [ -n "$PASSWORD" ] || fail "Se recibio una contrasena vacia para $SECRET_PATH."

  printf 'Guardando secreto: %s\n' "$SECRET_PATH"
  if [ -n "$USERNAME" ]; then
    printf '%s' "$PASSWORD" |
      vault kv put "$SECRET_PATH" username="$USERNAME" password=@/dev/stdin >/dev/null
  else
    printf '%s' "$PASSWORD" |
      vault kv put "$SECRET_PATH" password=@/dev/stdin >/dev/null
  fi

  unset PASSWORD
done < "$CONFIG_FILE"

exec 3<&-

printf 'Secretos iniciales almacenados correctamente en Vault.\n'
