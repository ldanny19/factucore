#!/bin/sh
set -eu

VAULT_ADDR="${VAULT_ADDR:-http://127.0.0.1:8200}"
export VAULT_ADDR

SECRETS_DIR="${FACTUCORE_VAULT_SECRETS_DIR:-/vault/secrets-persist}"
INIT_FILE="$SECRETS_DIR/vault-init.json"

AGENT_TOKEN_FILE="${FACTUCORE_VAULT_AGENT_TOKEN_FILE:-/vault/secrets-persist/vault_agent_token.txt}"
AGENT_TOKEN_RUNTIME_FILE="/vault/agent-token/vault_agent_token.txt"

POLICY_FILE="/vault/policy/factucore-agent.hcl"

log() {
  printf '\n==> %s\n' "$1"
}

fail() {
  printf 'ERROR: %s\n' "$1" >&2
  exit 1
}

log "INICIO BOOTSTRAP DE VAULT"
log "VAULT_ADDR=$VAULT_ADDR"
log "SECRETS_DIR=$SECRETS_DIR"

mkdir -p "$SECRETS_DIR"
chmod 700 "$SECRETS_DIR"

mkdir -p "$(dirname "$AGENT_TOKEN_RUNTIME_FILE")"

# ---------------------------------------------------------------------------
# Esperar disponibilidad de Vault
# ---------------------------------------------------------------------------

log "Esperando disponibilidad de Vault"

STATUS_RC=1

for _ in $(seq 1 60); do
  set +e
  vault status -format=json >/tmp/vault-status.json 2>/dev/null
  STATUS_RC=$?
  set -e

  if [ "$STATUS_RC" -eq 0 ] ||
     [ "$STATUS_RC" -eq 1 ] ||
     [ "$STATUS_RC" -eq 2 ]; then
    break
  fi

  sleep 2
done

[ "$STATUS_RC" -eq 0 ] ||
[ "$STATUS_RC" -eq 1 ] ||
[ "$STATUS_RC" -eq 2 ] ||
fail "No se pudo consultar el estado de Vault."

# ---------------------------------------------------------------------------
# Inicialización
# ---------------------------------------------------------------------------

INITIALIZED=$(
  grep -c \
    '"initialized"[[:space:]]*:[[:space:]]*true' \
    /tmp/vault-status.json || true
)

if [ "$INITIALIZED" -eq 0 ]; then

  [ ! -f "$INIT_FILE" ] ||
    fail "Vault no esta inicializado pero ya existe $INIT_FILE."

  log "Inicializando Vault por primera vez"

  vault operator init -format=json > "$INIT_FILE"

  chmod 600 "$INIT_FILE"
fi

# ---------------------------------------------------------------------------
# Obtener estado actual
# ---------------------------------------------------------------------------

STATUS_JSON=$(vault status -format=json 2>/dev/null || true)

SEALED=$(
  printf '%s' "$STATUS_JSON" |
    grep -c \
      '"sealed"[[:space:]]*:[[:space:]]*true' || true
)

# ---------------------------------------------------------------------------
# Unseal
# ---------------------------------------------------------------------------

if [ "$SEALED" -eq 1 ]; then

  [ -f "$INIT_FILE" ] ||
    fail "Vault esta inicializado y sellado, pero no existe $INIT_FILE."

  THRESHOLD=$(
    grep -o \
      '"unseal_threshold"[[:space:]]*:[[:space:]]*[0-9][0-9]*' \
      "$INIT_FILE" |
      head -1 |
      grep -o '[0-9][0-9]*$' ||
      true
  )

  [ -n "$THRESHOLD" ] ||
    fail "No se pudo determinar el umbral de unseal."

  KEYS=$(
    awk '
      /"unseal_keys_b64"[[:space:]]*:/ {
        inside=1
        next
      }

      inside && /]/ {
        exit
      }

      inside {
        line=$0

        gsub(/^[[:space:]]*"/, "", line)
        gsub(/",[[:space:]]*$/, "", line)
        gsub(/"[,[:space:]]*$/, "", line)

        if (line != "") {
          print line
        }
      }
    ' "$INIT_FILE" |
    head -n "$THRESHOLD"
  )

  KEY_COUNT=$(
    printf '%s\n' "$KEYS" |
      sed '/^$/d' |
      wc -l |
      tr -d ' '
  )

  [ "$KEY_COUNT" -ge "$THRESHOLD" ] ||
    fail "No se encontraron suficientes claves de unseal."

  log "Desellando Vault"

  KEY_INDEX=0

  for key in $KEYS; do

    KEY_INDEX=$((KEY_INDEX + 1))

    log "Aplicando clave de unseal $KEY_INDEX de $THRESHOLD"

    if ! vault operator unseal "$key" >/tmp/vault-unseal.json 2>&1; then
      cat /tmp/vault-unseal.json >&2
      fail "Vault rechazo la clave de unseal $KEY_INDEX."
    fi

  done

  STATUS_JSON=$(vault status -format=json 2>/dev/null || true)

  SEALED=$(
    printf '%s' "$STATUS_JSON" |
      grep -c \
        '"sealed"[[:space:]]*:[[:space:]]*true' || true
  )

  [ "$SEALED" -eq 0 ] ||
    fail "Vault continua sellado despues de aplicar las claves de unseal."

fi

# ---------------------------------------------------------------------------
# Esperar Vault completamente listo
# ---------------------------------------------------------------------------

log "Esperando a que Vault quede completamente desellado"

READY=0

for _ in $(seq 1 30); do

  STATUS_JSON=$(vault status -format=json 2>/dev/null || true)

  SEALED=$(
    printf '%s' "$STATUS_JSON" |
      grep -c \
        '"sealed"[[:space:]]*:[[:space:]]*true' || true
  )

  INITIALIZED=$(
    printf '%s' "$STATUS_JSON" |
      grep -c \
        '"initialized"[[:space:]]*:[[:space:]]*true' || true
  )

  if [ "$INITIALIZED" -eq 1 ] &&
     [ "$SEALED" -eq 0 ]; then

    READY=1
    break
  fi

  sleep 1
done

[ "$READY" -eq 1 ] ||
  fail "Vault no quedo inicializado y desellado dentro del tiempo esperado."

# ---------------------------------------------------------------------------
# Root token
# ---------------------------------------------------------------------------

ROOT_TOKEN="${FACTUCORE_VAULT_ROOT_TOKEN:-}"

if [ -z "$ROOT_TOKEN" ] && [ -f "$INIT_FILE" ]; then

  ROOT_TOKEN=$(
    grep -o \
      '"root_token"[[:space:]]*:[[:space:]]*"[^"]*"' \
      "$INIT_FILE" |
      head -1 |
      cut -d '"' -f 4
  )

fi

[ -n "$ROOT_TOKEN" ] ||
  fail "No se encontro el root token."

export VAULT_TOKEN="$ROOT_TOKEN"

# ---------------------------------------------------------------------------
# KV v2
# ---------------------------------------------------------------------------

log "Verificando KV v2"

if ! vault secrets list -format=json | grep -q '"secret/"'; then

  log "Habilitando KV v2 en secret/"

  vault secrets enable -path=secret kv-v2

fi

# ---------------------------------------------------------------------------
# Generador de secretos
# ---------------------------------------------------------------------------

generate_secret() {
  cat /proc/sys/kernel/random/uuid | tr -d '-'
}

# ---------------------------------------------------------------------------
# Credenciales PostgreSQL
# ---------------------------------------------------------------------------

FACTURADOR_PASSWORD="${FACTUCORE_POSTGRES_FACTURADOR_PASSWORD:-}"
AUTH_PASSWORD="${FACTUCORE_POSTGRES_AUTH_PASSWORD:-}"
NOTIFICACIONES_PASSWORD="${FACTUCORE_POSTGRES_NOTIFICACIONES_PASSWORD:-}"

# ---------------------------------------------------------------------------
# Credencial PostgreSQL - Facturador
# ---------------------------------------------------------------------------

if vault kv get secret/factucore/postgresql/facturador >/dev/null 2>&1; then

  log "Credencial PostgreSQL de Facturador ya existe; no se sobrescribe."

else

  [ -n "$FACTURADOR_PASSWORD" ] ||
    FACTURADOR_PASSWORD=$(generate_secret)

  log "Creando credencial PostgreSQL de Facturador"

  vault kv put \
    secret/factucore/postgresql/facturador \
    username="usr_factucore" \
    password="$FACTURADOR_PASSWORD" \
    >/dev/null

fi

# ---------------------------------------------------------------------------
# Credencial PostgreSQL - Auth Server
# ---------------------------------------------------------------------------

if vault kv get secret/factucore/postgresql/auth >/dev/null 2>&1; then

  log "Credencial PostgreSQL de Auth Server ya existe; no se sobrescribe."

else

  [ -n "$AUTH_PASSWORD" ] ||
    AUTH_PASSWORD=$(generate_secret)

  log "Creando credencial PostgreSQL de Auth Server"

  vault kv put \
    secret/factucore/postgresql/auth \
    username="usr_auth" \
    password="$AUTH_PASSWORD" \
    >/dev/null

fi

# ---------------------------------------------------------------------------
# Credencial PostgreSQL - Notificaciones
# ---------------------------------------------------------------------------

if vault kv get secret/factucore/postgresql/notificaciones >/dev/null 2>&1; then

  log "Credencial PostgreSQL de Notificaciones ya existe; no se sobrescribe."

else

  [ -n "$NOTIFICACIONES_PASSWORD" ] ||
    NOTIFICACIONES_PASSWORD=$(generate_secret)

  log "Creando credencial PostgreSQL de Notificaciones"

  vault kv put \
    secret/factucore/postgresql/notificaciones \
    username="usr_notificaciones" \
    password="$NOTIFICACIONES_PASSWORD" \
    >/dev/null

fi

# ---------------------------------------------------------------------------
# Credencial administrador Keycloak
# ---------------------------------------------------------------------------

KEYCLOAK_PASSWORD="${FACTUCORE_KEYCLOAK_ADMIN_PASSWORD:-}"

if vault kv get secret/factucore/keycloak >/dev/null 2>&1; then

  log "Credencial de Keycloak ya existe; no se sobrescribe."

else

  [ -n "$KEYCLOAK_PASSWORD" ] ||
    KEYCLOAK_PASSWORD=$(generate_secret)

  log "Creando credencial de Keycloak"

  vault kv put \
    secret/factucore/keycloak \
    password="$KEYCLOAK_PASSWORD" \
    >/dev/null

fi

# ---------------------------------------------------------------------------
# Policy del Vault Agent
# ---------------------------------------------------------------------------

log "Configurando policy del Vault Agent"

[ -f "$POLICY_FILE" ] ||
  fail "No existe la policy: $POLICY_FILE"

vault policy write factucore-agent "$POLICY_FILE" >/dev/null

# ---------------------------------------------------------------------------
# Token persistente del Vault Agent
# ---------------------------------------------------------------------------

AGENT_TOKEN=""

if [ -f "$AGENT_TOKEN_FILE" ]; then

  AGENT_TOKEN=$(
    tr -d '\r\n' < "$AGENT_TOKEN_FILE"
  )

fi

if [ -n "$AGENT_TOKEN" ]; then

  if ! VAULT_TOKEN="$AGENT_TOKEN" vault token lookup >/dev/null 2>&1; then

    log "Token persistente invalido; se generara uno nuevo."

    AGENT_TOKEN=""

  fi

fi

if [ -z "$AGENT_TOKEN" ]; then

  log "Creando token del Vault Agent"

  AGENT_TOKEN=$(
    vault token create \
      -policy=factucore-agent \
      -orphan \
      -format=json |
      grep -o \
        '"client_token"[[:space:]]*:[[:space:]]*"[^"]*"' |
      head -1 |
      cut -d '"' -f 4
  )

  [ -n "$AGENT_TOKEN" ] ||
    fail "Vault no devolvio un client token."

  printf '%s\n' "$AGENT_TOKEN" > "$AGENT_TOKEN_FILE"

else

  log "Token persistente del Vault Agent ya existe; se reutiliza."

fi

[ -s "$AGENT_TOKEN_FILE" ] ||
  fail "No existe un token persistente valido: $AGENT_TOKEN_FILE"

# ---------------------------------------------------------------------------
# Copiar token al volumen runtime
# ---------------------------------------------------------------------------

cp "$AGENT_TOKEN_FILE" "$AGENT_TOKEN_RUNTIME_FILE"

chown 100:100 "$AGENT_TOKEN_RUNTIME_FILE"
chmod 600 "$AGENT_TOKEN_RUNTIME_FILE"

[ -s "$AGENT_TOKEN_RUNTIME_FILE" ] ||
  fail "No se pudo copiar el token al volumen runtime."

# ---------------------------------------------------------------------------
# Validar acceso del Vault Agent
# ---------------------------------------------------------------------------

log "Validando acceso del Vault Agent"

VAULT_TOKEN="$AGENT_TOKEN" \
  vault kv get secret/factucore/postgresql/facturador >/dev/null

VAULT_TOKEN="$AGENT_TOKEN" \
  vault kv get secret/factucore/postgresql/auth >/dev/null

VAULT_TOKEN="$AGENT_TOKEN" \
  vault kv get secret/factucore/postgresql/notificaciones >/dev/null

VAULT_TOKEN="$AGENT_TOKEN" \
  vault kv get secret/factucore/keycloak >/dev/null

# ---------------------------------------------------------------------------
# Limpieza
# ---------------------------------------------------------------------------

rm -f /tmp/vault-status.json
rm -f /tmp/vault-unseal.json

log "BOOTSTRAP DE VAULT COMPLETADO CORRECTAMENTE"