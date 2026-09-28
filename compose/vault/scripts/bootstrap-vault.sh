#!/usr/bin/env bash
set -euo pipefail

VAULT_CONTAINER="${FACTUCORE_VAULT_CONTAINER:-factucore-vault}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
COMPOSE_DIR="$(cd "$SCRIPT_DIR/../.." && pwd)"
FACTUCORE_HOME_VALUE="${FACTUCORE_HOME:-}"
if [[ -z "$FACTUCORE_HOME_VALUE" ]]; then FACTUCORE_HOME_VALUE="$(cd "$COMPOSE_DIR/.." && pwd)"; fi
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
  THRESHOLD=$(sed -n 's/.*"unseal_threshold"[[:space:]]*:[[:space:]]*\([0-9][0-9]*\).*/\1/p' "$INIT_FILE" | head -1)
  [[ -n "$THRESHOLD" ]] || fail "No se pudo determinar el umbral de unseal desde $INIT_FILE."
  KEYS=$(sed -n '/\"unseal_keys_b64\"/,/]/p' "$INIT_FILE" | grep -o '"[^"]*"' | tail -n +2 | tr -d '"' | head -n "$THRESHOLD")
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

POSTGRES_PASSWORD="${FACTUCORE_POSTGRES_PASSWORD:-}"
if [[ -z "$POSTGRES_PASSWORD" ]]; then read -r -s -p "Contraseña de PostgreSQL: " POSTGRES_PASSWORD; printf '\n'; fi
KEYCLOAK_PASSWORD="${FACTUCORE_KEYCLOAK_ADMIN_PASSWORD:-}"
if [[ -z "$KEYCLOAK_PASSWORD" ]]; then read -r -s -p "Contraseña del administrador de Keycloak: " KEYCLOAK_PASSWORD; printf '\n'; fi

log "Configurando KV v2"
MOUNTS_JSON="$(docker exec -e "VAULT_TOKEN=$ROOT_TOKEN" "$VAULT_CONTAINER" vault secrets list -format=json)"
if ! printf '%s' "$MOUNTS_JSON" | grep -q '"secret/"'; then
  docker exec -e "VAULT_TOKEN=$ROOT_TOKEN" "$VAULT_CONTAINER" vault secrets enable -path=secret kv-v2
fi

docker exec -e "VAULT_TOKEN=$ROOT_TOKEN" "$VAULT_CONTAINER" vault kv put secret/factucore/postgresql "password=$POSTGRES_PASSWORD"
docker exec -e "VAULT_TOKEN=$ROOT_TOKEN" "$VAULT_CONTAINER" vault kv put secret/factucore/keycloak "password=$KEYCLOAK_PASSWORD"

log "Configurando policy y Vault Agent"
[[ -f "$POLICY_FILE" ]] || fail "No existe la policy: $POLICY_FILE"
docker exec -i -e "VAULT_TOKEN=$ROOT_TOKEN" "$VAULT_CONTAINER" vault policy write factucore-agent - < "$POLICY_FILE"

AGENT_TOKEN=""
[[ -f "$AGENT_TOKEN_FILE" ]] && AGENT_TOKEN="$(tr -d '\r\n' < "$AGENT_TOKEN_FILE")"
if [[ -z "$AGENT_TOKEN" ]] || ! docker exec -e "VAULT_TOKEN=$AGENT_TOKEN" "$VAULT_CONTAINER" vault token lookup >/dev/null 2>&1; then
  AGENT_TOKEN="$(docker exec -e "VAULT_TOKEN=$ROOT_TOKEN" "$VAULT_CONTAINER" vault token create -policy=factucore-agent -orphan -format=json | grep -o '"client_token"[[:space:]]*:[[:space:]]*"[^"]*"' | head -1 | cut -d '"' -f 4)"
  [[ -n "$AGENT_TOKEN" ]] || fail "Vault no devolvio un client token."
  printf '%s\n' "$AGENT_TOKEN" > "$AGENT_TOKEN_FILE"; chmod 600 "$AGENT_TOKEN_FILE"
fi

log "Validando configuración"
docker exec -e "VAULT_TOKEN=$AGENT_TOKEN" "$VAULT_CONTAINER" vault kv get secret/factucore/postgresql >/dev/null
docker exec -e "VAULT_TOKEN=$AGENT_TOKEN" "$VAULT_CONTAINER" vault kv get secret/factucore/keycloak >/dev/null
echo; echo "Bootstrap de Vault completado correctamente."; echo "El script no levanta ni detiene contenedores."
