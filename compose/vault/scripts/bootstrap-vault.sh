#!/usr/bin/env bash
set -euo pipefail

VAULT_CONTAINER="${FACTUCORE_VAULT_CONTAINER:-factucore-vault}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
COMPOSE_DIR="$(cd "$SCRIPT_DIR/../.." && pwd)"
FACTUCORE_HOME_VALUE="${FACTUCORE_HOME:-}"
if [[ -z "$FACTUCORE_HOME_VALUE" && -f "$COMPOSE_DIR/.env" ]]; then
  FACTUCORE_HOME_VALUE="$(grep '^FACTUCORE_HOME=' "$COMPOSE_DIR/.env" | head -n 1 | cut -d '=' -f 2- | tr -d '\r' | sed 's/^"//; s/"$//')"
fi
if [[ -z "$FACTUCORE_HOME_VALUE" ]]; then
  fail "FACTUCORE_HOME no esta definido. Configuralo en $COMPOSE_DIR/.env o como variable de entorno."
fi

SECRETS_DIR="$FACTUCORE_HOME_VALUE/secrets"
INIT_FILE="$SECRETS_DIR/vault-init.json"
AGENT_TOKEN_FILE="$SECRETS_DIR/vault_agent_token.txt"
POLICY_FILE="$SCRIPT_DIR/../policy/factucore-agent.hcl"

log(){ printf '\n==> %s\n' "$1"; }
fail(){ printf 'ERROR: %s\n' "$1" >&2; exit 1; }
require_command(){ command -v "$1" >/dev/null 2>&1 || fail "No se encontro el comando requerido: $1"; }
require_command docker

mkdir -p "$SECRETS_DIR"; chmod 700 "$SECRETS_DIR"
docker inspect "$VAULT_CONTAINER" >/dev/null 2>&1 || fail "El contenedor '$VAULT_CONTAINER' no existe. Levanta Vault con Docker Compose antes de ejecutar este script."

log "Esperando disponibilidad de Vault"
STATUS_JSON=""; STATUS_RC=1
for _ in $(seq 1 30); do
  STATUS_JSON="$(docker exec "$VAULT_CONTAINER" vault status -format=json 2>/dev/null)" && STATUS_RC=0 || STATUS_RC=$?
  if [[ "$STATUS_RC" -eq 0 || "$STATUS_RC" -eq 1 || "$STATUS_RC" -eq 2 ]]; then break; fi
  sleep 2
done
[[ "$STATUS_RC" -eq 0 || "$STATUS_RC" -eq 1 || "$STATUS_RC" -eq 2 ]] || fail "No se pudo consultar el estado de Vault."

INITIALIZED=$(printf '%s' "$STATUS_JSON" | grep -o '"initialized"[[:space:]]*:[[:space:]]*true' || true)
if [[ -z "$INITIALIZED" ]]; then
  log "Inicializando Vault por primera vez"
  INIT_JSON="$(docker exec "$VAULT_CONTAINER" vault operator init -format=json)" || fail "No se pudo inicializar Vault."
  printf '%s\n' "$INIT_JSON" > "$INIT_FILE"; chmod 600 "$INIT_FILE"
  STATUS_JSON="$(docker exec "$VAULT_CONTAINER" vault status -format=json 2>/dev/null)" || true
fi

SEALED=$(printf '%s' "$STATUS_JSON" | grep -o '"sealed"[[:space:]]*:[[:space:]]*true' || true)
if [[ -n "$SEALED" ]]; then
  [[ -f "$INIT_FILE" ]] || fail "Vault esta sellado y no existe $INIT_FILE."

  THRESHOLD=$(printf '%s' "$STATUS_JSON" | grep -o '"t"[[:space:]]*:[[:space:]]*[0-9][0-9]*' | head -1 | grep -o '[0-9][0-9]*$' || true)
  if [[ -z "$THRESHOLD" ]]; then
    THRESHOLD=$(grep -o '"unseal_threshold"[[:space:]]*:[[:space:]]*[0-9][0-9]*' "$INIT_FILE" | head -1 | grep -o '[0-9][0-9]*$' || true)
  fi
  [[ -n "$THRESHOLD" ]] || fail "No se pudo determinar el umbral de unseal desde el estado de Vault ni desde $INIT_FILE."

  KEYS=$(awk '/"unseal_keys_b64"[[:space:]]*:/{captura=1} captura{print} captura && /]/{exit}' "$INIT_FILE" |
    grep -o '"[^"]*"' | tail -n +2 | tr -d '"' | head -n "$THRESHOLD")
  KEY_COUNT=$(printf '%s\n' "$KEYS" | sed '/^$/d' | wc -l | tr -d ' ')
  [[ "$KEY_COUNT" -ge "$THRESHOLD" ]] || fail "No se encontraron suficientes claves de unseal en $INIT_FILE."

  log "Ejecutando unseal"
  while IFS= read -r key; do
    [[ -n "$key" ]] || continue
    docker exec "$VAULT_CONTAINER" vault operator unseal "$key" >/dev/null || fail "No se pudo ejecutar el unseal."
  done <<< "$KEYS"
fi

STATUS_JSON="$(docker exec "$VAULT_CONTAINER" vault status -format=json 2>/dev/null)" || fail "No se pudo verificar el estado de Vault."
SEALED=$(printf '%s' "$STATUS_JSON" | grep -o '"sealed"[[:space:]]*:[[:space:]]*true' || true)
[[ -z "$SEALED" ]] || fail "Vault continua sellado."

log "Obteniendo credenciales"
ROOT_TOKEN="${FACTUCORE_VAULT_ROOT_TOKEN:-}"
if [[ -z "$ROOT_TOKEN" && -f "$INIT_FILE" ]]; then ROOT_TOKEN=$(grep -o '"root_token"[[:space:]]*:[[:space:]]*"[^"]*"' "$INIT_FILE" | head -1 | cut -d '"' -f 4); fi
[[ -n "$ROOT_TOKEN" ]] || fail "No se encontro el root token."

read_secret() {
  local prompt="$1"
  local value=""
  printf '%s' "$prompt" >&2
  stty -echo 2>/dev/null || true
  IFS= read -r value
  local read_status=$?
  stty echo 2>/dev/null || true
  printf '\n' >&2
  [[ "$read_status" -eq 0 ]] || fail "No se pudo leer la contraseña."
  [[ -n "$value" ]] || fail "La contraseña no puede estar vacia."
  printf '%s' "$value"
}

SECRET_EXISTS_POSTGRES=0
SECRET_EXISTS_KEYCLOAK=0
SECRETS_CURRENT="$(docker exec -e "VAULT_TOKEN=$ROOT_TOKEN" "$VAULT_CONTAINER" vault kv get -format=json secret/factucore/postgresql 2>/dev/null || true)"
if [[ -n "$SECRETS_CURRENT" ]]; then SECRET_EXISTS_POSTGRES=1; fi
SECRETS_CURRENT="$(docker exec -e "VAULT_TOKEN=$ROOT_TOKEN" "$VAULT_CONTAINER" vault kv get -format=json secret/factucore/keycloak 2>/dev/null || true)"
if [[ -n "$SECRETS_CURRENT" ]]; then SECRET_EXISTS_KEYCLOAK=1; fi

if [[ "${FACTUCORE_VAULT_ACTUALIZAR_CREDENCIALES:-false}" == "true" ]]; then
  log "Actualizando credenciales solicitadas"
  POSTGRES_PASSWORD="${FACTUCORE_POSTGRES_PASSWORD:-}"
  if [[ -z "$POSTGRES_PASSWORD" ]]; then POSTGRES_PASSWORD="$(read_secret "Nueva contraseña de PostgreSQL: ")"; fi
  KEYCLOAK_PASSWORD="${FACTUCORE_KEYCLOAK_ADMIN_PASSWORD:-}"
  if [[ -z "$KEYCLOAK_PASSWORD" ]]; then KEYCLOAK_PASSWORD="$(read_secret "Nueva contraseña del administrador de Keycloak: ")"; fi
else
  if [[ "$SECRET_EXISTS_POSTGRES" -eq 0 || "$SECRET_EXISTS_KEYCLOAK" -eq 0 ]]; then
    log "Configurando credenciales iniciales"
    POSTGRES_PASSWORD="${FACTUCORE_POSTGRES_PASSWORD:-}"
    if [[ -z "$POSTGRES_PASSWORD" ]]; then POSTGRES_PASSWORD="$(read_secret "Contraseña de PostgreSQL: ")"; fi
    KEYCLOAK_PASSWORD="${FACTUCORE_KEYCLOAK_ADMIN_PASSWORD:-}"
    if [[ -z "$KEYCLOAK_PASSWORD" ]]; then KEYCLOAK_PASSWORD="$(read_secret "Contraseña del administrador de Keycloak: ")"; fi
  else
    log "Credenciales ya existentes en Vault; no se sobrescriben."
  fi
fi

log "Configurando KV v2"
MOUNTS_JSON="$(docker exec -e "VAULT_TOKEN=$ROOT_TOKEN" "$VAULT_CONTAINER" vault secrets list -format=json)"
if ! printf '%s' "$MOUNTS_JSON" | grep -q '"secret/"'; then
  docker exec -e "VAULT_TOKEN=$ROOT_TOKEN" "$VAULT_CONTAINER" vault secrets enable -path=secret kv-v2
fi

if [[ -n "${POSTGRES_PASSWORD:-}" ]]; then
  docker exec -e "VAULT_TOKEN=$ROOT_TOKEN" "$VAULT_CONTAINER" vault kv put secret/factucore/postgresql "password=$POSTGRES_PASSWORD"
fi
if [[ -n "${KEYCLOAK_PASSWORD:-}" ]]; then
  docker exec -e "VAULT_TOKEN=$ROOT_TOKEN" "$VAULT_CONTAINER" vault kv put secret/factucore/keycloak "password=$KEYCLOAK_PASSWORD"
fi

log "Configurando policy y Vault Agent"
[[ -f "$POLICY_FILE" ]] || fail "No existe la policy: $POLICY_FILE"
docker exec -i -e "VAULT_TOKEN=$ROOT_TOKEN" "$VAULT_CONTAINER" vault policy write factucore-agent - < "$POLICY_FILE"

AGENT_TOKEN=""
[[ -f "$AGENT_TOKEN_FILE" ]] && AGENT_TOKEN="$(tr -d '\r\n' < "$AGENT_TOKEN_FILE")"
if [[ -z "$AGENT_TOKEN" ]] || ! docker exec -e "VAULT_TOKEN=$AGENT_TOKEN" "$VAULT_CONTAINER" vault token lookup >/dev/null 2>&1; then
  AGENT_TOKEN="$(docker exec -e "VAULT_TOKEN=$ROOT_TOKEN" "$VAULT_CONTAINER" vault token create -policy=factucore-agent -orphan -format=json |
    grep -o '"client_token"[[:space:]]*:[[:space:]]*"[^"]*"' | head -1 | cut -d '"' -f 4)"
  [[ -n "$AGENT_TOKEN" ]] || fail "Vault no devolvio un client token."
  printf '%s\n' "$AGENT_TOKEN" > "$AGENT_TOKEN_FILE"; chmod 600 "$AGENT_TOKEN_FILE"
fi

log "Validando configuración"
docker exec -e "VAULT_TOKEN=$AGENT_TOKEN" "$VAULT_CONTAINER" vault kv get secret/factucore/postgresql >/dev/null
docker exec -e "VAULT_TOKEN=$AGENT_TOKEN" "$VAULT_CONTAINER" vault kv get secret/factucore/keycloak >/dev/null

echo
echo "Bootstrap de Vault completado correctamente."
echo "El script no levanta ni detiene contenedores."
