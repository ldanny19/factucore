#!/usr/bin/env bash
set -euo pipefail

VAULT_CONTAINER="${FACTUCORE_VAULT_CONTAINER:-factucore-vault}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
COMPOSE_DIR="$(cd "$SCRIPT_DIR/../.." && pwd)"
FACTUCORE_HOME_VALUE="${FACTUCORE_HOME:-}"
if [[ -z "$FACTUCORE_HOME_VALUE" ]]; then
  FACTUCORE_HOME_VALUE="$(cd "$COMPOSE_DIR/.." && pwd)"
fi
SECRETS_DIR="$FACTUCORE_HOME_VALUE/secrets"
INIT_FILE="$SECRETS_DIR/vault-init.json"
AGENT_TOKEN_FILE="$SECRETS_DIR/vault_agent_token.txt"
POLICY_FILE="$SCRIPT_DIR/../policy/factucore-agent.hcl"

log() { printf '\n==> %s\n' "$1"; }
fail() { printf 'ERROR: %s\n' "$1" >&2; exit 1; }
require_command() { command -v "$1" >/dev/null 2>&1 || fail "No se encontro el comando requerido: $1"; }
require_command docker

mkdir -p "$SECRETS_DIR"
chmod 700 "$SECRETS_DIR"

log "Levantando Vault"
docker compose -f "$COMPOSE_DIR/docker-compose.yml" up -d vault >/dev/null

log "Esperando disponibilidad de Vault"
STATUS_JSON=""
STATUS_RC=1
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
  printf '%s\n' "$INIT_JSON" > "$INIT_FILE"
  chmod 600 "$INIT_FILE"
  echo "Inicializacion guardada en $INIT_FILE"
  STATUS_JSON="$(docker exec "$VAULT_CONTAINER" vault status -format=json 2>/dev/null)" || true
fi

SEALED=$(printf '%s' "$STATUS_JSON" | grep -o '"sealed"[[:space:]]*:[[:space:]]*true' || true)
if [[ -n "$SEALED" ]]; then
  THRESHOLD=$(grep -o '"unseal_keys_b64"[[:space:]]*:[[:space:]]*\[[^]]*\]' "$INIT_FILE" | grep -o '"[^"]*"' | tail -n +2 | wc -l | tr -d ' ')
  [[ "$THRESHOLD" -gt 0 ]] || fail "No se encontraron claves de unseal en $INIT_FILE."
  log "Ejecutando unseal"
  KEYS=$(grep -o '"unseal_keys_b64"[[:space:]]*:[[:space:]]*\[[^]]*\]' "$INIT_FILE" | grep -o '"[^"]*"' | tail -n +2 | tr -d '"' | head -n "$THRESHOLD")
  while IFS= read -r UNSEAL_KEY; do
    [[ -n "$UNSEAL_KEY" ]] || continue
    docker exec "$VAULT_CONTAINER" vault operator unseal "$UNSEAL_KEY" >/dev/null || fail "No se pudo ejecutar el unseal."
  done <<< "$KEYS"
fi

STATUS_JSON="$(docker exec "$VAULT_CONTAINER" vault status -format=json 2>/dev/null)" || fail "No se pudo verificar el estado final de Vault."
SEALED=$(printf '%s' "$STATUS_JSON" | grep -o '"sealed"[[:space:]]*:[[:space:]]*true' || true)
[[ -z "$SEALED" ]] || fail "Vault continua sellado."

log "Obteniendo credenciales"
ROOT_TOKEN="${FACTUCORE_VAULT_ROOT_TOKEN:-}"
if [[ -z "$ROOT_TOKEN" && -f "$INIT_FILE" ]]; then
  ROOT_TOKEN=$(grep -o '"root_token"[[:space:]]*:[[:space:]]*"[^"]*"' "$INIT_FILE" | head -1 | cut -d '"' -f 4)
fi
if [[ -z "$ROOT_TOKEN" ]]; then read -r -s -p "FACTUCORE_VAULT_ROOT_TOKEN: " ROOT_TOKEN; printf '\n'; fi
[[ -n "$ROOT_TOKEN" ]] || fail "Se requiere el root token."

POSTGRES_PASSWORD="${FACTUCORE_POSTGRES_PASSWORD:-}"
if [[ -z "$POSTGRES_PASSWORD" ]]; then read -r -s -p "Contraseña de PostgreSQL: " POSTGRES_PASSWORD; printf '\n'; fi
KEYCLOAK_PASSWORD="${FACTUCORE_KEYCLOAK_ADMIN_PASSWORD:-}"
if [[ -z "$KEYCLOAK_PASSWORD" ]]; then read -r -s -p "Contraseña del administrador de Keycloak: " KEYCLOAK_PASSWORD; printf '\n'; fi

log "Habilitando/verificando KV v2"
MOUNTS_JSON="$(docker exec -e "VAULT_TOKEN=$ROOT_TOKEN" "$VAULT_CONTAINER" vault secrets list -format=json)"
if printf '%s' "$MOUNTS_JSON" | grep -q '"secret/"'; then
  if ! printf '%s' "$MOUNTS_JSON" | grep '"secret/"' | grep -q '"type"[[:space:]]*:[[:space:]]*"kv"' || ! printf '%s' "$MOUNTS_JSON" | grep '"secret/"' | grep -q '"version"[[:space:]]*:[[:space:]]*"2"'; then
    fail "El mount 'secret/' ya existe pero no es KV v2."
  fi
else
  docker exec -e "VAULT_TOKEN=$ROOT_TOKEN" "$VAULT_CONTAINER" vault secrets enable -path=secret kv-v2
fi

log "Cargando secretos"
docker exec -e "VAULT_TOKEN=$ROOT_TOKEN" "$VAULT_CONTAINER" vault kv put secret/factucore/postgresql "password=$POSTGRES_PASSWORD"
docker exec -e "VAULT_TOKEN=$ROOT_TOKEN" "$VAULT_CONTAINER" vault kv put secret/factucore/keycloak "password=$KEYCLOAK_PASSWORD"

log "Aplicando policy"
[[ -f "$POLICY_FILE" ]] || fail "No existe la policy: $POLICY_FILE"
docker exec -i -e "VAULT_TOKEN=$ROOT_TOKEN" "$VAULT_CONTAINER" vault policy write factucore-agent - < "$POLICY_FILE"

log "Creando/reutilizando token del Vault Agent"
AGENT_TOKEN=""
if [[ -f "$AGENT_TOKEN_FILE" ]]; then AGENT_TOKEN="$(tr -d '\r\n' < "$AGENT_TOKEN_FILE")"; fi
if [[ -n "$AGENT_TOKEN" ]] && docker exec -e "VAULT_TOKEN=$AGENT_TOKEN" "$VAULT_CONTAINER" vault token lookup >/dev/null 2>&1; then
  echo "Token del Vault Agent existente y valido."
else
  AGENT_TOKEN="$(docker exec -e "VAULT_TOKEN=$ROOT_TOKEN" "$VAULT_CONTAINER" vault token create -policy=factucore-agent -orphan -format=json | grep -o '"client_token"[[:space:]]*:[[:space:]]*"[^"]*"' | head -1 | cut -d '"' -f 4)"
  [[ -n "$AGENT_TOKEN" ]] || fail "Vault no devolvio un client token."
  printf '%s\n' "$AGENT_TOKEN" > "$AGENT_TOKEN_FILE"
  chmod 600 "$AGENT_TOKEN_FILE"
fi

log "Validando acceso"
docker exec -e "VAULT_TOKEN=$AGENT_TOKEN" "$VAULT_CONTAINER" vault kv get secret/factucore/postgresql >/dev/null
docker exec -e "VAULT_TOKEN=$AGENT_TOKEN" "$VAULT_CONTAINER" vault kv get secret/factucore/keycloak >/dev/null
echo
echo "Bootstrap de Vault completado correctamente."
echo "Ahora puedes levantar el stack para que Vault Agent renderice los secretos."
