#!/usr/bin/env bash
set -Eeuo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
COMPOSE_FILE="$ROOT_DIR/compose/docker-compose.yml"
ENV_FILE="$ROOT_DIR/compose/.env"

log() { printf '\n==> %s\n' "$1"; }
fail() { printf '\nERROR: %s\n' "$1" >&2; exit 1; }

command -v docker >/dev/null 2>&1 || fail "Se requiere Docker."
docker compose version >/dev/null 2>&1 || fail "Docker Compose v2 no esta disponible."
[ -f "$ENV_FILE" ] || fail "No existe $ENV_FILE."
[ -f "$COMPOSE_FILE" ] || fail "No existe $COMPOSE_FILE."

FACTUCORE_HOME_VALUE="${FACTUCORE_HOME:-}"
if [ -z "$FACTUCORE_HOME_VALUE" ]; then
  FACTUCORE_HOME_VALUE="$(sed -n 's/^FACTUCORE_HOME=//p' "$ENV_FILE" | tail -n 1 | tr -d '\r')"
fi
FACTUCORE_HOME_VALUE="${FACTUCORE_HOME_VALUE%\"}"
FACTUCORE_HOME_VALUE="${FACTUCORE_HOME_VALUE#\"}"
FACTUCORE_HOME_VALUE="${FACTUCORE_HOME_VALUE%\'}"
FACTUCORE_HOME_VALUE="${FACTUCORE_HOME_VALUE#\'}"
[ -n "$FACTUCORE_HOME_VALUE" ] || fail "FACTUCORE_HOME no esta definido."

HOST_HOME="$FACTUCORE_HOME_VALUE"
if command -v cygpath >/dev/null 2>&1; then HOST_HOME="$(cygpath -u "$HOST_HOME")"; fi
CATALOG_FILE="$HOST_HOME/vault/config/secretos.conf"
[ -f "$CATALOG_FILE" ] || fail "No existe el catalogo de secretos $CATALOG_FILE."

ENV_FILE_NATIVE="$ENV_FILE"
COMPOSE_FILE_NATIVE="$COMPOSE_FILE"
if command -v cygpath >/dev/null 2>&1; then
  ENV_FILE_NATIVE="$(cygpath -w "$ENV_FILE")"
  COMPOSE_FILE_NATIVE="$(cygpath -w "$COMPOSE_FILE")"
fi
COMPOSE=(docker compose --env-file "$ENV_FILE_NATIVE" -f "$COMPOSE_FILE_NATIVE")

log "Verificando que Vault este operativo y desbloqueado."
if ! MSYS_NO_PATHCONV=1 "${COMPOSE[@]}" exec -T vault vault status -format=json >/dev/null 2>&1; then
  "${COMPOSE[@]}" logs --tail=80 vault >&2 || true
  fail "Vault no esta disponible o permanece sellado."
fi

while IFS='|' read -r key secret_path username description; do
  case "$key" in ""|\#*) continue ;; esac
  if ! MSYS_NO_PATHCONV=1 "${COMPOSE[@]}" exec -T vault sh -c '
    set -eu
    INIT_FILE="${FACTUCORE_VAULT_SECRETS_DIR:-/vault/secrets-persist}/vault-init.json"
    ROOT_TOKEN="${FACTUCORE_VAULT_ROOT_TOKEN:-}"
    if [ -z "$ROOT_TOKEN" ]; then
      ROOT_TOKEN=$(awk -F "\"" "/root_token/ { print \$4; exit }" "$INIT_FILE")
    fi
    [ -n "$ROOT_TOKEN" ]
    export VAULT_TOKEN="$ROOT_TOKEN"
    vault kv get "$1" >/dev/null 2>&1
  ' sh "$secret_path" >/dev/null 2>&1; then
    fail "Falta el secreto requerido '$secret_path'. Ejecuta instalar-factucore.sh."
  fi
done < "$CATALOG_FILE"

log "Credenciales verificadas. Docker Compose respetara depends_on y los healthchecks."
"${COMPOSE[@]}" up -d
log "Servicios de FactuCore iniciados."
