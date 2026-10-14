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

for command in docker sed grep yq; do
  command -v "$command" >/dev/null 2>&1 || fail "Se requiere el comando '$command'."
done
yq --version 2>&1 | grep -q 'version v4\.' || fail "Se requiere yq v4 (mikefarah/yq)."
docker compose version >/dev/null 2>&1 || fail "Docker Compose v2 no esta disponible."

[ -f "$ENV_FILE" ] || fail "No existe $ENV_FILE. Copia compose/.env.example a compose/.env y configura FACTUCORE_HOME y FACTUCORE_VERSION."
[ -f "$COMPOSE_FILE" ] || fail "No existe $COMPOSE_FILE."
[ -f "$CATALOG_FILE" ] || fail "No existe el catalogo de secretos $CATALOG_FILE."
yq -e '.secretos | type == "!!map"' "$CATALOG_FILE" >/dev/null || fail "El catalogo YAML no contiene un mapa valido en la propiedad secretos."

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
HOST_POSTGRES_INIT="$HOST_HOME/postgres/init"
HOST_FACTURADOR_CONFIG="$HOST_HOME/facturador/config"
HOST_FACTURADOR_JASPER="$HOST_HOME/facturador/jasper"
HOST_FACTURADOR_DOCUMENTOS="$HOST_HOME/facturador/documentos"
HOST_FACTURADOR_CERTIFICADOS="$HOST_HOME/facturador/certificados"
HOST_FACTURADOR_IMAGENES="$HOST_HOME/facturador/imagenes"
HOST_FACTURADOR_LOGS="$HOST_HOME/facturador/logs"
HOST_NOTIFICACION_CONFIG="$HOST_HOME/notificacion/config"
HOST_NOTIFICACION_LOGS="$HOST_HOME/notificacion/logs"
HOST_MESSAGING_CONFIG="$HOST_HOME/messaging/config"
RUNTIME_CATALOG_FILE="$HOST_VAULT_CONFIG/secretos.conf"

COMPOSE=(docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE")

# Evitar que un cambio de rutas cree una base de datos o un Vault vacíos
# mientras los datos existentes permanecen en las ubicaciones anteriores.
OLD_POSTGRES_DATA="$HOST_HOME/data/postgres"
OLD_SECRETS_DIR="$HOST_HOME/secrets"
if [ -d "$OLD_POSTGRES_DATA" ] && find "$OLD_POSTGRES_DATA" -mindepth 1 -print -quit 2>/dev/null | grep -q .; then
  if [ ! -d "$HOST_HOME/postgres/data" ] || ! find "$HOST_HOME/postgres/data" -mindepth 1 -print -quit 2>/dev/null | grep -q .; then
    fail "Se detectaron datos PostgreSQL en la ruta anterior $OLD_POSTGRES_DATA. Detén Compose y migra esa carpeta a $HOST_HOME/postgres/data antes de continuar."
  fi
fi
if [ -d "$OLD_SECRETS_DIR" ] && find "$OLD_SECRETS_DIR" -mindepth 1 -print -quit 2>/dev/null | grep -q .; then
  if [ ! -d "$HOST_HOME/vault/secrets" ] || ! find "$HOST_HOME/vault/secrets" -mindepth 1 -print -quit 2>/dev/null | grep -q .; then
    fail "Se detectaron archivos de inicialización/secretos en la ruta anterior $OLD_SECRETS_DIR. Detén Compose y migra esa carpeta a $HOST_HOME/vault/secrets antes de continuar."
  fi
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
  "$HOST_HOME/postgres/data" "$HOST_POSTGRES_INIT" "$HOST_HOME/kafka/data" \
  "$HOST_FACTURADOR_CONFIG" "$HOST_FACTURADOR_JASPER" "$HOST_FACTURADOR_DOCUMENTOS" \
  "$HOST_FACTURADOR_CERTIFICADOS" "$HOST_FACTURADOR_IMAGENES" "$HOST_FACTURADOR_LOGS" \
  "$HOST_NOTIFICACION_CONFIG" "$HOST_NOTIFICACION_LOGS" "$HOST_MESSAGING_CONFIG"

chmod 700 "$HOST_HOME/vault/secrets" "$HOST_HOME/vault/agent-token"

copy_tree_if_missing "$ROOT_DIR/configuraciones/facturador/config" "$HOST_FACTURADOR_CONFIG"
copy_tree_if_missing "$ROOT_DIR/configuraciones/facturador/jasper" "$HOST_FACTURADOR_JASPER"
copy_tree_if_missing "$ROOT_DIR/configuraciones/notificacion/config" "$HOST_NOTIFICACION_CONFIG"
copy_tree_if_missing "$ROOT_DIR/configuraciones/messaging/config" "$HOST_MESSAGING_CONFIG"
yq -r '.secretos | to_entries[] | [.key, .value.ruta, (.value.usuario // ""), .value.descripcion] | join("|")' "$CATALOG_FILE" > "$RUNTIME_CATALOG_FILE.tmp"
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
