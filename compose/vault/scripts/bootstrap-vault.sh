#!/bin/sh
set -eu

VAULT_ADDR="${VAULT_ADDR:-http://vault:8200}"
export VAULT_ADDR

SECRETS_DIR="${FACTUCORE_VAULT_SECRETS_DIR:-/vault/bootstrap-secrets}"
INIT_FILE="$SECRETS_DIR/vault-init.json"
AGENT_TOKEN_FILE="$SECRETS_DIR/vault_agent_token.txt"
POLICY_FILE="/vault/policy/factucore-agent.hcl"

log() {
  printf '\n==> %s\n' "$1"
}

fail() {
  printf 'ERROR: %s\n' "$1" >&2
  exit 1
}

mkdir -p "$SECRETS_DIR"
chmod 755 "$SECRETS_DIR"

log "Esperando disponibilidad de Vault"
STATUS_RC=1
for _ in $(seq 1 60); do
  set +e
  vault status -format=json >/tmp/vault-status.json 2>/dev/null
  STATUS_RC=$?
  set -e
  if [ "$STATUS_RC" -eq 0 ] || [ "$STATUS_RC" -eq 1 ] || [ "$STATUS_RC" -eq 2 ]; then
    break
  fi
  sleep 2
done

[ "$STATUS_RC" -eq 0 ] || [ "$STATUS_RC" -eq 1 ] || [ "$STATUS_RC" -eq 2 ] || fail "No se pudo consultar el estado de Vault."

INITIALIZED=$(grep -c '"initialized"[[:space:]]*:[[:space:]]*true' /tmp/vault-status.json || true)

if [ "$INITIALIZED" -eq 0 ]; then
  [ ! -f "$INIT_FILE" ] || fail "Vault no esta inicializado pero ya existe $INIT_FILE. Elimina el estado de inicializacion anterior solo si este entorno debe comenzar desde cero."

  log "Inicializando Vault por primera vez"
  vault operator init -format=json > "$INIT_FILE"
  chmod 600 "$INIT_FILE"
fi

STATUS_JSON=$(vault status -format=json 2>/dev/null || true)
SEALED=$(printf '%s' "$STATUS_JSON" | grep -c '"sealed"[[:space:]]*:[[:space:]]*true' || true)

if [ "$SEALED" -eq 1 ]; then
  [ -f "$INIT_FILE" ] || fail "Vault esta inicializado y sellado, pero no existe $INIT_FILE. Recupera las claves de unseal de la instancia existente."

  THRESHOLD=$(grep -o '"unseal_threshold"[[:space:]]*:[[:space:]]*[0-9][0-9]*' "$INIT_FILE" | head -1 | grep -o '[0-9][0-9]*$' || true)
  [ -n "$THRESHOLD" ] || fail "No se pudo determinar el umbral de unseal desde $INIT_FILE."

  KEYS=$(sed -n '/"unseal_keys_b64"[[:space:]]*:/,/]/p' "$INIT_FILE" |
    grep -o '"[^"]*"' |
    tail -n +2 |
    tr -d '"' |
    head -n "$THRESHOLD")

  KEY_COUNT=$(printf '%s\n' "$KEYS" | sed '/^$/d' | wc -l | tr -d ' ')
  [ "$KEY_COUNT" -ge "$THRESHOLD" ] || fail "No se encontraron suficientes claves de unseal en $INIT_FILE."

  log "Desellando Vault"
  printf '%s\n' "$KEYS" | while IFS= read -r key; do
    [ -n "$key" ] || continue
    vault operator unseal "$key" >/dev/null
  done
fi

STATUS_JSON=$(vault status -format=json 2>/dev/null) || fail "No se pudo verificar el estado final de Vault."
SEALED=$(printf '%s' "$STATUS_JSON" | grep -c '"sealed"[[:space:]]*:[[:space:]]*true' || true)
[ "$SEALED" -eq 0 ] || fail "Vault continua sellado."

ROOT_TOKEN="${FACTUCORE_VAULT_ROOT_TOKEN:-${VAULT_TOKEN:-}}"
if [ -z "$ROOT_TOKEN" ] && [ -f "$INIT_FILE" ]; then
  ROOT_TOKEN=$(grep -o '"root_token"[[:space:]]*:[[:space:]]*"[^"]*"' "$INIT_FILE" | head -1 | cut -d '"' -f 4)
fi
[ -n "$ROOT_TOKEN" ] || fail "No se encontro el root token."

export VAULT_TOKEN="$ROOT_TOKEN"

log "Verificando KV v2"
if ! vault secrets list -format=json | grep -q '"secret/"'; then
  vault secrets enable -path=secret kv-v2
fi

generate_secret() {
  cat /proc/sys/kernel/random/uuid | tr -d '-'
}

POSTGRES_PASSWORD="${FACTUCORE_POSTGRES_PASSWORD:-}"
KEYCLOAK_PASSWORD="${FACTUCORE_KEYCLOAK_ADMIN_PASSWORD:-}"

if vault kv get secret/factucore/postgresql >/dev/null 2>&1; then
  log "Credencial de PostgreSQL ya existente; no se sobrescribe."
else
  [ -n "$POSTGRES_PASSWORD" ] || POSTGRES_PASSWORD=$(generate_secret)
  log "Creando credencial de PostgreSQL"
  vault kv put secret/factucore/postgresql "password=$POSTGRES_PASSWORD" >/dev/null
fi

if vault kv get secret/factucore/keycloak >/dev/null 2>&1; then
  log "Credencial de Keycloak ya existente; no se sobrescribe."
else
  [ -n "$KEYCLOAK_PASSWORD" ] || KEYCLOAK_PASSWORD=$(generate_secret)
  log "Creando credencial de Keycloak"
  vault kv put secret/factucore/keycloak "password=$KEYCLOAK_PASSWORD" >/dev/null
fi

log "Configurando policy del Vault Agent"
[ -f "$POLICY_FILE" ] || fail "No existe la policy: $POLICY_FILE"
vault policy write factucore-agent "$POLICY_FILE" >/dev/null

AGENT_TOKEN=""
if [ -f "$AGENT_TOKEN_FILE" ]; then
  AGENT_TOKEN=$(tr -d '\r\n' < "$AGENT_TOKEN_FILE")
fi

if [ -n "$AGENT_TOKEN" ]; then
  if ! VAULT_TOKEN="$AGENT_TOKEN" vault token lookup >/dev/null 2>&1; then
    AGENT_TOKEN=""
  fi
fi

if [ -z "$AGENT_TOKEN" ]; then
  log "Creando token del Vault Agent"
  AGENT_TOKEN=$(vault token create -policy=factucore-agent -orphan -format=json |
    grep -o '"client_token"[[:space:]]*:[[:space:]]*"[^"]*"' |
    head -1 |
    cut -d '"' -f 4)
  [ -n "$AGENT_TOKEN" ] || fail "Vault no devolvio un client token."
  printf '%s\n' "$AGENT_TOKEN" > "$AGENT_TOKEN_FILE"
  chmod 644 "$AGENT_TOKEN_FILE"
fi

log "Validando acceso del Vault Agent"
VAULT_TOKEN="$AGENT_TOKEN" vault kv get secret/factucore/postgresql >/dev/null
VAULT_TOKEN="$AGENT_TOKEN" vault kv get secret/factucore/keycloak >/dev/null

rm -f /tmp/vault-status.json

echo
echo "Bootstrap de Vault completado correctamente."
echo "Vault esta inicializado, desellado y listo para Vault Agent."
