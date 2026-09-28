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
AGENT_TOKEN_FILE="$SECRETS_DIR/vault_agent_token.txt"
POLICY_FILE="$SCRIPT_DIR/../policy/factucore-agent.hcl"

log() {
  printf '\n==> %s\n' "$1"
}

fail() {
  printf 'ERROR: %s\n' "$1" >&2
  exit 1
}

require_command() {
  command -v "$1" >/dev/null 2>&1 || fail "No se encontro el comando requerido: $1"
}

require_command docker

log "Levantando Vault"
docker compose -f "$COMPOSE_DIR/docker-compose.yml" up -d vault >/dev/null

log "Esperando disponibilidad de Vault"
STATUS_JSON=""
STATUS_RC=1
for _ in $(seq 1 30); do
  STATUS_RC=0
  STATUS_JSON="$(docker exec "$VAULT_CONTAINER" vault status -format=json 2>/dev/null)" || STATUS_RC=$?
  if [[ "$STATUS_RC" -eq 0 || "$STATUS_RC" -eq 1 || "$STATUS_RC" -eq 2 ]]; then
    break
  fi
  sleep 2
done

if [[ "$STATUS_RC" -ne 0 && "$STATUS_RC" -ne 1 && "$STATUS_RC" -ne 2 ]]; then
  fail "No se pudo consultar el estado de Vault. Verifica el contenedor '$VAULT_CONTAINER'."
fi

INITIALIZED=$(printf '%s' "$STATUS_JSON" | grep -o '"initialized"[[:space:]]*:[[:space:]]*true' || true)
SEALED=$(printf '%s' "$STATUS_JSON" | grep -o '"sealed"[[:space:]]*:[[:space:]]*true' || true)

[[ -n "$INITIALIZED" ]] || fail "Vault no esta inicializado. Ejecuta 'vault operator init' una sola vez y conserva las claves fuera del repositorio."

if [[ -n "$SEALED" ]]; then
  THRESHOLD=$(printf '%s' "$STATUS_JSON" | grep -o '"t"[[:space:]]*:[[:space:]]*[0-9]*' | head -1 | grep -o '[0-9]*$' || true)
  [[ -n "$THRESHOLD" ]] || THRESHOLD=1

  log "Vault esta sellado; iniciando unseal interactivo"
  for ((i=1; i<=THRESHOLD; i++)); do
    read -r -s -p "Unseal Key $i/$THRESHOLD: " UNSEAL_KEY
    printf '\n'
    [[ -n "$UNSEAL_KEY" ]] || fail "La clave de unseal no puede estar vacia."
    docker exec "$VAULT_CONTAINER" vault operator unseal "$UNSEAL_KEY" >/dev/null || fail "La clave de unseal no fue aceptada."
    unset UNSEAL_KEY
  done
fi

STATUS_JSON="$(docker exec "$VAULT_CONTAINER" vault status -format=json 2>/dev/null)" || fail "No se pudo verificar el estado final de Vault."
SEALED=$(printf '%s' "$STATUS_JSON" | grep -o '"sealed"[[:space:]]*:[[:space:]]*true' || true)
[[ -z "$SEALED" ]] || fail "Vault continua sellado. Verifica las claves de unseal."

log "Obteniendo credenciales externas"
ROOT_TOKEN="${FACTUCORE_VAULT_ROOT_TOKEN:-}"
if [[ -z "$ROOT_TOKEN" ]]; then
  read -r -s -p "FACTUCORE_VAULT_ROOT_TOKEN: " ROOT_TOKEN
  printf '\n'
fi
[[ -n "$ROOT_TOKEN" ]] || fail "Se requiere FACTUCORE_VAULT_ROOT_TOKEN."

POSTGRES_PASSWORD="${FACTUCORE_POSTGRES_PASSWORD:-}"
if [[ -z "$POSTGRES_PASSWORD" ]]; then
  read -r -s -p "Contraseña de PostgreSQL: " POSTGRES_PASSWORD
  printf '\n'
fi

KEYCLOAK_PASSWORD="${FACTUCORE_KEYCLOAK_ADMIN_PASSWORD:-}"
if [[ -z "$KEYCLOAK_PASSWORD" ]]; then
  read -r -s -p "Contraseña del administrador de Keycloak: " KEYCLOAK_PASSWORD
  printf '\n'
fi

log "Habilitando/verificando KV v2"
MOUNTS_JSON="$(docker exec -e "VAULT_TOKEN=$ROOT_TOKEN" "$VAULT_CONTAINER" vault secrets list -format=json)"
if printf '%s' "$MOUNTS_JSON" | grep -q '"secret/"'; then
  if ! printf '%s' "$MOUNTS_JSON" | grep '"secret/"' | grep -q '"type"[[:space:]]*:[[:space:]]*"kv"' || ! printf '%s' "$MOUNTS_JSON" | grep '"secret/"' | grep -q '"version"[[:space:]]*:[[:space:]]*"2"'; then
    fail "El mount 'secret/' ya existe pero no es KV v2."
  fi
else
  docker exec -e "VAULT_TOKEN=$ROOT_TOKEN" "$VAULT_CONTAINER" vault secrets enable -path=secret kv-v2
fi

log "Cargando secretos en Vault"
docker exec -e "VAULT_TOKEN=$ROOT_TOKEN" "$VAULT_CONTAINER" vault kv put secret/factucore/postgresql "password=$POSTGRES_PASSWORD"
docker exec -e "VAULT_TOKEN=$ROOT_TOKEN" "$VAULT_CONTAINER" vault kv put secret/factucore/keycloak "password=$KEYCLOAK_PASSWORD"

log "Aplicando policy del Vault Agent"
[[ -f "$POLICY_FILE" ]] || fail "No existe la policy: $POLICY_FILE"
docker exec -i -e "VAULT_TOKEN=$ROOT_TOKEN" "$VAULT_CONTAINER" vault policy write factucore-agent - < "$POLICY_FILE"

log "Creando/reutilizando token del Vault Agent"
mkdir -p "$SECRETS_DIR"
chmod 700 "$SECRETS_DIR"

AGENT_TOKEN=""
if [[ -f "$AGENT_TOKEN_FILE" ]]; then
  AGENT_TOKEN="$(tr -d '\r\n' < "$AGENT_TOKEN_FILE")"
fi

TOKEN_VALID=false
if [[ -n "$AGENT_TOKEN" ]]; then
  if docker exec -e "VAULT_TOKEN=$AGENT_TOKEN" "$VAULT_CONTAINER" vault token lookup >/dev/null 2>&1; then
    TOKEN_VALID=true
  fi
fi

if [[ "$TOKEN_VALID" == "false" ]]; then
  AGENT_TOKEN="$(docker exec -e "VAULT_TOKEN=$ROOT_TOKEN" "$VAULT_CONTAINER" vault token create -policy=factucore-agent -orphan -format=json | grep -o '"client_token"[[:space:]]*:[[:space:]]*"[^"]*"' | head -1 | cut -d '"' -f 4)"
  [[ -n "$AGENT_TOKEN" ]] || fail "Vault no devolvio un client token."
  printf '%s\n' "$AGENT_TOKEN" > "$AGENT_TOKEN_FILE"
  chmod 600 "$AGENT_TOKEN_FILE"
  echo "Token del Vault Agent creado en $AGENT_TOKEN_FILE"
else
  echo "Token del Vault Agent existente y valido."
fi

log "Validando acceso del Vault Agent"
docker exec -e "VAULT_TOKEN=$AGENT_TOKEN" "$VAULT_CONTAINER" vault kv get secret/factucore/postgresql >/dev/null
docker exec -e "VAULT_TOKEN=$AGENT_TOKEN" "$VAULT_CONTAINER" vault kv get secret/factucore/keycloak >/dev/null

echo
echo "Bootstrap de Vault completado correctamente."
echo "Ahora puedes levantar el stack para que Vault Agent renderice los secretos."
