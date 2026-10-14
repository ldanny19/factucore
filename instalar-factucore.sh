#!/usr/bin/env bash
set -Eeuo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
COMPOSE_FILE="$ROOT_DIR/compose/docker-compose.yml"
ENV_FILE="$ROOT_DIR/compose/.env"
CATALOG_FILE="$ROOT_DIR/infraestructura/vault/config/secretos.yml"

log() {
  printf '\n==> %s\n' "$1"
}

fail() {
  printf '\nERROR: %s\n' "$1" >&2
  exit 1
}

for command in docker sed grep awk; do
  command -v "$command" >/dev/null 2>&1 || fail "Se requiere el comando '$command'."
done
docker compose version >/dev/null 2>&1 || fail "Docker Compose v2 no esta disponible."

[ -f "$ENV_FILE" ] || fail "No existe $ENV_FILE. Copia compose/.env.example a compose/.env y configura FACTUCORE_HOME y FACTUCORE_VERSION."
[ -f "$COMPOSE_FILE" ] || fail "No existe $COMPOSE_FILE."
[ -f "$CATALOG_FILE" ] || fail "No existe el catalogo de secretos $CATALOG_FILE."

# Parser estricto para el subconjunto YAML utilizado por el catálogo.
# El YAML permanece como fuente única; no se requiere instalar un parser adicional.
parse_secret_catalog() {
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
      if ((first == "\"" && last == "\"") || (first == "\047" && last == "\047"))
        value = substr(value, 2, length(value) - 2)
      return value
    }
    function emit_entry() {
      if (key == "") return
      if (ruta == "" || descripcion == "" || ruta ~ /[|]/ || usuario ~ /[|]/ || descripcion ~ /[|]/) {
        printf "Catálogo inválido en la entrada %s: ruta y descripcion son obligatorias y los campos no pueden contener |.\n", key > "/dev/stderr"
        invalid = 1
        return
      }
      printf "%s|%s|%s|%s\n", key, ruta, usuario, descripcion
      entries++
    }
    BEGIN { in_root = 0; key = ""; ruta = ""; usuario = ""; descripcion = ""; entries = 0; invalid = 0 }
    /^[[:space:]]*#/ || /^[[:space:]]*$/ { next }
    /^secretos:[[:space:]]*$/ { if (in_root) invalid = 1; in_root = 1; next }
    !in_root {
      printf "Catálogo inválido: se esperaba la propiedad raíz secretos.\n" > "/dev/stderr"
      invalid = 1
      next
    }
    /^  [A-Za-z0-9_-]+:[[:space:]]*$/ {
      emit_entry()
      key = $0
      sub(/^  /, "", key)
      sub(/:[[:space:]]*$/, "", key)
      ruta = ""; usuario = ""; descripcion = ""
      next
    }
    /^    (ruta|usuario|descripcion):/ {
      if (key == "") { invalid = 1; next }
      line = $0
      sub(/^    /, "", line)
      separator = index(line, ":")
      field = substr(line, 1, separator - 1)
      value = unquote(substr(line, separator + 1))
      if (field == "ruta") ruta = value
      else if (field == "usuario") usuario = value
      else if (field == "descripcion") descripcion = value
      next
    }
    {
      printf "Catálogo inválido: formato YAML no admitido en línea %d.\n", NR > "/dev/stderr"
      invalid = 1
    }
    END {
      emit_entry()
      if (entries == 0) invalid = 1
      if (invalid) exit 1
    }
  ' "$CATALOG_FILE"
}

parse_secret_catalog >/dev/null || fail "El catálogo YAML de secretos no tiene el formato admitido."

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
if command -v cygpath >/dev/null 2>&1; then
  HOST_HOME="$(cygpath -u "$HOST_HOME")"
fi

INIT_FILE="$HOST_HOME/vault/secrets/vault-init.json"
VAULT_DATA_DIR="$HOST_HOME/vault/data"
HOST_VAULT_CONFIG="$HOST_HOME/vault/config"
HOST_VAULT_SCRIPTS="$HOST_HOME/vault/scripts"
HOST_VAULT_POLICY="$HOST_HOME/vault/policy"
HOST_VAULT_AGENT="$HOST_HOME/vault/agent"
HOST_POSTGRES_INIT="$HOST_HOME/base_datos/postgres/init"
HOST_FACTURADOR_CONFIG="$HOST_HOME/facturador/config"
HOST_FACTURADOR_JASPER="$HOST_HOME/facturador/jasper"
HOST_FACTURADOR_DOCUMENTOS="$HOST_HOME/facturador/documentos"
HOST_FACTURADOR_CERTIFICADOS="$HOST_HOME/facturador/certificados"
HOST_FACTURADOR_IMAGENES="$HOST_HOME/facturador/imagenes"
HOST_FACTURADOR_LOGS="$HOST_HOME/facturador/logs"
HOST_NOTIFICACION_CONFIG="$HOST_HOME/notificacion/config"
HOST_NOTIFICACION_LOGS="$HOST_HOME/notificacion/logs"
HOST_MESSAGING_CONFIG="$HOST_HOME/mensajeria/config"
RUNTIME_CATALOG_FILE="$HOST_VAULT_CONFIG/secretos.conf"

COMPOSE=(docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE")

# Detectar únicamente instalaciones anteriores realmente inicializadas.
# Las carpetas antiguas vacías o con archivos auxiliares no deben bloquear una instalación.
OLD_POSTGRES_DATA="$HOST_HOME/data/postgres"
PREVIOUS_POSTGRES_DATA="$HOST_HOME/postgres/data"
OLD_KAFKA_DATA="$HOST_HOME/kafka/data"
OLD_MESSAGING_CONFIG="$HOST_HOME/messaging/config"
OLD_SECRETS_DIR="$HOST_HOME/secrets"
TARGET_POSTGRES_DATA="$HOST_HOME/base_datos/postgres/data"
TARGET_KAFKA_DATA="$HOST_HOME/mensajeria/kafka/data"

# PostgreSQL: PG_VERSION identifica un clúster inicializado.
if { [ -f "$OLD_POSTGRES_DATA/PG_VERSION" ] || [ -f "$PREVIOUS_POSTGRES_DATA/PG_VERSION" ]; } \
  && [ ! -f "$TARGET_POSTGRES_DATA/PG_VERSION" ]; then
  fail "Se detectó un clúster PostgreSQL inicializado en una ruta anterior. Para conservar sus datos, detén Compose y migra el clúster a $TARGET_POSTGRES_DATA antes de continuar."
fi

# Kafka (KRaft): meta.properties identifica almacenamiento inicializado.
if [ -d "$OLD_KAFKA_DATA" ] \
  && find "$OLD_KAFKA_DATA" -name meta.properties -type f -print -quit 2>/dev/null | grep -q . \
  && ! find "$TARGET_KAFKA_DATA" -name meta.properties -type f -print -quit 2>/dev/null | grep -q .; then
  fail "Se detectó almacenamiento Kafka inicializado en $OLD_KAFKA_DATA. Para conservar sus datos, migra ese almacenamiento a $TARGET_KAFKA_DATA antes de continuar."
fi

# Configuración local antigua: solo bloquear si existen archivos de configuración
# y la ubicación nueva todavía no contiene ninguno. No bloquear por carpetas vacías.
if [ -d "$OLD_MESSAGING_CONFIG" ] \
  && find "$OLD_MESSAGING_CONFIG" -type f -print -quit 2>/dev/null | grep -q . \
  && ! find "$HOST_HOME/mensajeria/config" -type f -print -quit 2>/dev/null | grep -q .; then
  fail "Se detectaron archivos de configuración de mensajería en $OLD_MESSAGING_CONFIG. Cópialos a $HOST_HOME/mensajeria/config antes de continuar para conservar la configuración local."
fi

# Secretos de inicialización de Vault: solo proteger el archivo conocido.
if [ -f "$OLD_SECRETS_DIR/vault-init.json" ] \
  && [ ! -f "$HOST_HOME/vault/secrets/vault-init.json" ]; then
  fail "Se detectó el archivo de inicialización de Vault en $OLD_SECRETS_DIR. Para conservar el acceso a los secretos existentes, cópialo a $HOST_HOME/vault/secrets antes de continuar."
fi

install_if_missing() {
  local source_file="$1"
  local destination_file="$2"
  if [ ! -f "$destination_file" ]; then
    cp "$source_file" "$destination_file"
    chmod 644 "$destination_file"
  fi
}

copy_tree_if_missing() {
  local source_dir="$1"
  local destination_dir="$2"
  local source_file relative_path destination_file

  [ -d "$source_dir" ] || return 0
  mkdir -p "$destination_dir"

  while IFS= read -r -d "" source_file; do
    relative_path="${source_file#"$source_dir"/}"
    destination_file="$destination_dir/$relative_path"
    if [ ! -e "$destination_file" ]; then
      mkdir -p "$(dirname "$destination_file")"
      cp "$source_file" "$destination_file"
    fi
  done < <(find "$source_dir" -type f -print0)
}

# Cada capacidad mantiene su configuracion y persistencia bajo FACTUCORE_HOME.
# Los archivos de configuracion se copian solo si faltan; nunca se sobrescriben
# configuraciones locales ni datos existentes.
mkdir -p \
  "$HOST_HOME/vault/data" "$HOST_HOME/vault/secrets" \
  "$HOST_HOME/vault/runtime-secrets" "$HOST_HOME/vault/agent-token" \
  "$HOST_VAULT_CONFIG" "$HOST_VAULT_SCRIPTS" "$HOST_VAULT_POLICY" "$HOST_VAULT_AGENT" \
  "$HOST_HOME/base_datos/postgres/data" "$HOST_POSTGRES_INIT" "$HOST_HOME/mensajeria/kafka/data" \
  "$HOST_FACTURADOR_CONFIG" "$HOST_FACTURADOR_JASPER" "$HOST_FACTURADOR_DOCUMENTOS" \
  "$HOST_FACTURADOR_CERTIFICADOS" "$HOST_FACTURADOR_IMAGENES" "$HOST_FACTURADOR_LOGS" \
  "$HOST_NOTIFICACION_CONFIG" "$HOST_NOTIFICACION_LOGS" "$HOST_MESSAGING_CONFIG"

chmod 700 "$HOST_HOME/vault/secrets" "$HOST_HOME/vault/agent-token"

copy_tree_if_missing "$ROOT_DIR/configuraciones/facturador/config" "$HOST_FACTURADOR_CONFIG"
copy_tree_if_missing "$ROOT_DIR/configuraciones/facturador/jasper" "$HOST_FACTURADOR_JASPER"
copy_tree_if_missing "$ROOT_DIR/configuraciones/notificacion/config" "$HOST_NOTIFICACION_CONFIG"
copy_tree_if_missing "$ROOT_DIR/configuraciones/messaging/config" "$HOST_MESSAGING_CONFIG"
parse_secret_catalog > "$RUNTIME_CATALOG_FILE.tmp"
[ -s "$RUNTIME_CATALOG_FILE.tmp" ] || fail "El catalogo YAML no contiene secretos."
mv "$RUNTIME_CATALOG_FILE.tmp" "$RUNTIME_CATALOG_FILE"
cp "$CATALOG_FILE" "$HOST_VAULT_CONFIG/secretos.yml"
cp "$ROOT_DIR/infraestructura/vault/scripts/bootstrap-vault.sh" "$HOST_VAULT_SCRIPTS/bootstrap-vault.sh"
cp "$ROOT_DIR/infraestructura/vault/scripts/configurar-secretos.sh" "$HOST_VAULT_SCRIPTS/configurar-secretos.sh"
chmod 644 "$RUNTIME_CATALOG_FILE" "$HOST_VAULT_CONFIG/secretos.yml"
chmod 755 "$HOST_VAULT_SCRIPTS/bootstrap-vault.sh" "$HOST_VAULT_SCRIPTS/configurar-secretos.sh"

install_if_missing "$ROOT_DIR/infraestructura/vault/config/vault.hcl" "$HOST_VAULT_CONFIG/vault.hcl"
install_if_missing "$ROOT_DIR/infraestructura/vault/scripts/entrypoint.sh" "$HOST_VAULT_SCRIPTS/entrypoint.sh"
install_if_missing "$ROOT_DIR/infraestructura/vault/policy/factucore-agent.hcl" "$HOST_VAULT_POLICY/factucore-agent.hcl"
install_if_missing "$ROOT_DIR/infraestructura/vault/agent/config.hcl" "$HOST_VAULT_AGENT/config.hcl"
install_if_missing "$ROOT_DIR/infraestructura/vault/agent/entrypoint.sh" "$HOST_VAULT_AGENT/entrypoint.sh"
install_if_missing "$ROOT_DIR/infraestructura/postgres/init/01-crear-bases-servicios.sql" "$HOST_POSTGRES_INIT/01-crear-bases-servicios.sql"
chmod 644 "$HOST_VAULT_CONFIG/vault.hcl" "$HOST_VAULT_POLICY/factucore-agent.hcl" "$HOST_VAULT_AGENT/config.hcl" "$HOST_POSTGRES_INIT/01-crear-bases-servicios.sql"
chmod 755 "$HOST_VAULT_SCRIPTS/entrypoint.sh" "$HOST_VAULT_AGENT/entrypoint.sh"

wait_for_vault() {
  local attempt
  for attempt in $(seq 1 90); do
    if "${COMPOSE[@]}" exec -T vault vault status -format=json >/dev/null 2>&1; then
      return 0
    fi
    sleep 2
  done
  "${COMPOSE[@]}" logs --tail=100 vault >&2 || true
  return 1
}

verify_existing_secrets() {
  local key secret_path username description
  while IFS='|' read -r key secret_path username description; do
    case "$key" in
      ""|\#*) continue ;;
    esac
    if ! "${COMPOSE[@]}" exec -T vault vault kv get "$secret_path" >/dev/null 2>&1; then
      fail "Falta el secreto $secret_path. No se modificaron credenciales; se requiere una recuperacion controlada."
    fi
  done < "$RUNTIME_CATALOG_FILE"
}

# Si existe almacenamiento pero no el archivo de inicializacion, no asumir
# que es una instalacion nueva: podria ser una instalacion incompleta.
if [ -f "$INIT_FILE" ]; then
  log "Se detecto una instalacion existente de Vault. No se solicitaran ni cambiaran credenciales."
  "${COMPOSE[@]}" up -d vault
  wait_for_vault || fail "Vault no quedo listo. Se detuvo la instalacion sin modificar secretos."
  verify_existing_secrets
  "${COMPOSE[@]}" up -d
  log "Infraestructura FactuCore iniciada reutilizando los secretos existentes."
  exit 0
fi

if [ -d "$VAULT_DATA_DIR" ] && find "$VAULT_DATA_DIR" -mindepth 1 -print -quit 2>/dev/null | grep -q .; then
  fail "Existe almacenamiento de Vault, pero falta $INIT_FILE. No se pediran credenciales ni se reinicializara Vault; se requiere recuperacion controlada."
fi

log "Instalacion nueva: se solicitaran las contrasenas definidas en el catalogo."
log "Los usuarios tecnicos y las rutas se toman de infraestructura/vault/config/secretos.yml."
log "Las contrasenas no se mostraran en pantalla ni se guardaran en archivos."

PASSWORDS=()
while IFS='|' read -r key secret_path username description; do
  case "$key" in
    ""|\#*) continue ;;
  esac

  while true; do
    printf '\n%s\n' "$description"
    IFS= read -r -s -p "Contrasena: " password
    printf '\n'
    [ -n "$password" ] || { printf 'La contrasena no puede estar vacia.\n'; continue; }
    IFS= read -r -s -p "Confirma la contrasena: " confirm_password
    printf '\n'
    if [ "$password" = "$confirm_password" ]; then
      break
    fi
    printf 'Las contrasenas no coinciden. Intentalo de nuevo.\n'
    unset password confirm_password
  done

  PASSWORDS+=("$password")
  unset password confirm_password
done < "$RUNTIME_CATALOG_FILE"

log "Iniciando unicamente Vault para inicializar y preparar el almacenamiento."
"${COMPOSE[@]}" up -d vault
wait_for_vault || fail "Vault no quedo listo. Revisa los logs; no se desplegaron los demas servicios."

log "Almacenando las credenciales en las rutas declaradas en el catalogo."
if ! printf '%s\n' "${PASSWORDS[@]}" | "${COMPOSE[@]}" exec -T vault /vault/scripts/configurar-secretos.sh; then
  fail "No se pudieron almacenar todos los secretos. No se desplegaran los demas servicios; revisa el estado de Vault antes de reintentar."
fi
unset PASSWORDS

log "Iniciando los servicios de FactuCore."
"${COMPOSE[@]}" up -d

log "Instalacion inicial completada. Las credenciales quedaron almacenadas en Vault."
