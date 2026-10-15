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

INITIALIZED_BEFORE_BOOTSTRAP="$INITIALIZED"

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
# Migracion de paths anteriores a la estructura canonica.
# Conserva los valores existentes y no sobrescribe destinos ya creados.
# ---------------------------------------------------------------------------

migrate_secret() {
  OLD_PATH="$1"
  NEW_PATH="$2"

  if vault kv get "$NEW_PATH" >/dev/null 2>&1; then
    log "El secreto destino ya existe; se conserva: $NEW_PATH"
    return
  fi

  if ! vault kv get "$OLD_PATH" >/dev/null 2>&1; then
    return
  fi

  log "Migrando secreto existente: $OLD_PATH -> $NEW_PATH"

  USERNAME=$(vault kv get -field=username "$OLD_PATH" 2>/dev/null || true)
  PASSWORD=$(vault kv get -field=password "$OLD_PATH" 2>/dev/null || true)

  if [ -n "$USERNAME" ] && [ -n "$PASSWORD" ]; then
    vault kv put "$NEW_PATH" username="$USERNAME" password="$PASSWORD" >/dev/null
  elif [ -n "$PASSWORD" ]; then
    vault kv put "$NEW_PATH" password="$PASSWORD" >/dev/null
  else
    fail "No se pudo migrar el secreto $OLD_PATH; no se modifico el origen."
  fi
}

migrate_secret "secret/factucore/postgresql/admin" "secret/factucore/basedatos/admin"
migrate_secret "secret/factucore/postgresql/facturador" "secret/factucore/basedatos/facturador"
migrate_secret "secret/factucore/postgresql/auth" "secret/factucore/basedatos/auth"
migrate_secret "secret/factucore/postgresql/notificaciones" "secret/factucore/basedatos/notificaciones"
migrate_secret "secret/factucore/notificacion/smtp" "secret/factucore/mail"

# ---------------------------------------------------------------------------
# Verificar secretos requeridos sin generar ni sobrescribir credenciales.
# La instalacion inicial los carga mediante instalar-factucore.sh.
# ---------------------------------------------------------------------------

REQUIRED_SECRET_PATHS=$(
  awk '
    function trim(value) {
      sub(/^[[:space:]]+/, "", value)
      sub(/[[:space:]]+$/, "", value)
      return value
    }
    function unquote(value, first, last) {
      value = trim(value)
      first = substr(value, 1, 1)
      last = substr(value, length(value), 1)
      if ((first == "\\\"" && last == "\\\"") || (first == "\\047" && last == "\\047"))
        value = substr(value, 2, length(value) - 2)
      return value
    }
    function emit_entry() {
      if (key == "") return
      if (ruta == "") {
        printf "Catalogo YAML invalido: falta ruta para %s.\\n", key > "/dev/stderr"
        invalid = 1
        return
      }
      print ruta
      entries++
    }
    BEGIN { in_root = 0; key = ""; ruta = ""; entries = 0; invalid = 0 }
    /^[[:space:]]*#/ || /^[[:space:]]*$/ { next }
    /^secretos:[[:space:]]*$/ { if (in_root) invalid = 1; in_root = 1; next }
    !in_root {
      printf "Catalogo YAML invalido: se esperaba la propiedad raiz secretos.\\n" > "/dev/stderr"
      invalid = 1
      next
    }
    /^  [A-Za-z0-9_-]+:[[:space:]]*$/ {
      emit_entry()
      key = $0
      sub(/^  /, "", key)
      sub(/:[[:space:]]*$/, "", key)
      ruta = ""
      next
    }
    /^    ruta:/ {
      line = $0
      sub(/^    ruta:[[:space:]]*/, "", line)
      ruta = unquote(line)
      next
    }
    /^    (usuario|descripcion):/ { next }
    {
      printf "Catalogo YAML invalido: formato no admitido en linea %d.\\n", NR > "/dev/stderr"
      invalid = 1
    }
    END {
      emit_entry()
      if (entries == 0) invalid = 1
      if (invalid) exit 1
    }
  ' /vault/config/secretos.yml
)

[ -n "$REQUIRED_SECRET_PATHS" ] ||
  fail "No se pudo leer el catalogo central de secretos /vault/config/secretos.yml."

MISSING_SECRETS=""

for SECRET_PATH in $REQUIRED_SECRET_PATHS; do
  if ! vault kv get "$SECRET_PATH" >/dev/null 2>&1; then
    MISSING_SECRETS="$MISSING_SECRETS $SECRET_PATH"
  fi
done

if [ -n "$MISSING_SECRETS" ]; then
  if [ "$INITIALIZED_BEFORE_BOOTSTRAP" -eq 1 ]; then
    log "Vault ya estaba inicializado. Se conservaran los secretos existentes y se difiere al instalador la solicitud de los secretos faltantes:$MISSING_SECRETS"
  else
    log "Instalacion nueva: se difiere la carga de secretos al instalador."
  fi
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

  else

    if ! VAULT_TOKEN="$AGENT_TOKEN" vault kv get secret/factucore/mail >/dev/null 2>&1 ||
       ! VAULT_TOKEN="$AGENT_TOKEN" vault kv get secret/factucore/firma >/dev/null 2>&1; then

      log "Token persistente sin permisos actualizados; se generara uno nuevo."

      AGENT_TOKEN=""

    fi

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
# Validar acceso del Vault Agent cuando ya existen todos los secretos.
# En una instalacion nueva, el instalador los cargara inmediatamente despues.
# ---------------------------------------------------------------------------

if [ -z "$MISSING_SECRETS" ]; then
  log "Validando acceso del Vault Agent"

  for SECRET_PATH in $REQUIRED_SECRET_PATHS; do
    VAULT_TOKEN="$AGENT_TOKEN" vault kv get "$SECRET_PATH" >/dev/null
  done
else
  log "Validacion de secretos diferida hasta completar la instalacion inicial."
fi

# ---------------------------------------------------------------------------
# Limpieza
# ---------------------------------------------------------------------------

rm -f /tmp/vault-status.json
rm -f /tmp/vault-unseal.json

log "BOOTSTRAP DE VAULT COMPLETADO CORRECTAMENTE"