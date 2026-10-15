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

# Docker Compose debe recibir rutas Windows para los archivos del host cuando se ejecuta
# desde Git Bash. Así MSYS_NO_PATHCONV=1 puede proteger las rutas Linux de los
# comandos dentro del contenedor sin corromper --env-file ni -f.
ENV_FILE_NATIVE="$ENV_FILE"
COMPOSE_FILE_NATIVE="$COMPOSE_FILE"
if command -v cygpath >/dev/null 2>&1; then
  ENV_FILE_NATIVE="$(cygpath -w "$ENV_FILE")"
  COMPOSE_FILE_NATIVE="$(cygpath -w "$COMPOSE_FILE")"
fi
COMPOSE=(docker compose --env-file "$ENV_FILE_NATIVE" -f "$COMPOSE_FILE_NATIVE")

# Se utiliza exclusivamente la estructura actual bajo FACTUCORE_HOME.
# Las rutas antiguas no forman parte del proceso de instalación ni de validación.

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
cp "$CATALOG_FILE" "$HOST_VAULT_CONFIG/secretos.yml"
cp "$ROOT_DIR/infraestructura/vault/scripts/bootstrap-vault.sh" "$HOST_VAULT_SCRIPTS/bootstrap-vault.sh"
chmod 644 "$HOST_VAULT_CONFIG/secretos.yml"
chmod 755 "$HOST_VAULT_SCRIPTS/bootstrap-vault.sh"

install_if_missing "$ROOT_DIR/infraestructura/vault/config/vault.hcl" "$HOST_VAULT_CONFIG/vault.hcl"
install_if_missing "$ROOT_DIR/infraestructura/vault/scripts/entrypoint.sh" "$HOST_VAULT_SCRIPTS/entrypoint.sh"
install_if_missing "$ROOT_DIR/infraestructura/vault/policy/factucore-agent.hcl" "$HOST_VAULT_POLICY/factucore-agent.hcl"
cp "$ROOT_DIR/infraestructura/vault/agent/config.hcl" "$HOST_VAULT_AGENT/config.hcl"
install_if_missing "$ROOT_DIR/infraestructura/vault/agent/entrypoint.sh" "$HOST_VAULT_AGENT/entrypoint.sh"
copy_tree_if_missing "$ROOT_DIR/infraestructura/postgres/init" "$HOST_POSTGRES_INIT"
chmod 644 "$HOST_VAULT_CONFIG/vault.hcl" "$HOST_VAULT_POLICY/factucore-agent.hcl" "$HOST_VAULT_AGENT/config.hcl" "$HOST_POSTGRES_INIT/01-crear-bases-servicios.sql"
chmod 755 "$HOST_VAULT_SCRIPTS/entrypoint.sh" "$HOST_VAULT_AGENT/entrypoint.sh"

wait_for_vault() {
  local attempt
  for attempt in $(seq 1 90); do
    if MSYS_NO_PATHCONV=1 "${COMPOSE[@]}" exec -T vault vault status -format=json >/dev/null 2>&1; then
      return 0
    fi
    sleep 2
  done
  "${COMPOSE[@]}" logs --tail=100 vault >&2 || true
  return 1
}

vault_secret_exists() {
  local secret_path="$1"
  local username="$2"
  MSYS_NO_PATHCONV=1 "${COMPOSE[@]}" exec -T --interactive=false vault sh -c '
    set -eu
    INIT_FILE="${FACTUCORE_VAULT_SECRETS_DIR:-/vault/secrets-persist}/vault-init.json"
    ROOT_TOKEN="${FACTUCORE_VAULT_ROOT_TOKEN:-}"
    if [ -z "$ROOT_TOKEN" ]; then
      ROOT_TOKEN=$(grep root_token "$INIT_FILE" | head -n 1 | cut -d "\"" -f 4)
    fi
    [ -n "$ROOT_TOKEN" ] || exit 1
    export VAULT_TOKEN="$ROOT_TOKEN"
    SECRET_PATH="$1"
    SECRET_USERNAME="$2"
    PASSWORD=$(vault kv get -field=password "$SECRET_PATH" 2>/dev/null) || exit 1
    [ -n "$PASSWORD" ] || exit 1
    if [ -n "$SECRET_USERNAME" ]; then
      STORED_USERNAME=$(vault kv get -field=username "$SECRET_PATH" 2>/dev/null) || exit 1
      [ "$STORED_USERNAME" = "$SECRET_USERNAME" ] || exit 1
    fi
  ' sh "$secret_path" "$username" >/dev/null 2>&1
}

store_secret() {
  local secret_path="$1"
  local username="$2"
  local password="$3"

  # Ejecuta Vault CLI dentro del contenedor y envía el valor únicamente por stdin.
  # No expone la contraseña como argumento ni la imprime en los logs.
  if [ -n "$username" ]; then
    printf '%s' "$password" |
      MSYS_NO_PATHCONV=1 "${COMPOSE[@]}" exec -T vault sh -c '
        set -eu
        INIT_FILE="${FACTUCORE_VAULT_SECRETS_DIR:-/vault/secrets-persist}/vault-init.json"
        ROOT_TOKEN="${FACTUCORE_VAULT_ROOT_TOKEN:-}"
        if [ -z "$ROOT_TOKEN" ]; then
          ROOT_TOKEN=$(grep root_token "$INIT_FILE" | head -n 1 | cut -d "\"" -f 4)
        fi
        [ -n "$ROOT_TOKEN" ] || { echo "No se pudo obtener el token administrativo de Vault." >&2; exit 1; }
        export VAULT_TOKEN="$ROOT_TOKEN"
        SECRET_PATH="$1"
        SECRET_USERNAME="$2"
        if PASSWORD_EXISTING=$(vault kv get -field=password "$SECRET_PATH" 2>/dev/null) && [ -n "$PASSWORD_EXISTING" ]; then
          if [ -z "$SECRET_USERNAME" ] || [ "$(vault kv get -field=username "$SECRET_PATH" 2>/dev/null)" = "$SECRET_USERNAME" ]; then
            printf "El secreto ya existe y es valido; se conserva: %s\n" "$SECRET_PATH"
            exit 0
          fi
        fi
        vault kv put "$SECRET_PATH" username="$SECRET_USERNAME" password=@/dev/stdin >/dev/null
        vault kv get -field=password "$SECRET_PATH" >/dev/null
      ' sh "$secret_path" "$username" >/dev/null
  else
    printf '%s' "$password" |
      MSYS_NO_PATHCONV=1 "${COMPOSE[@]}" exec -T vault sh -c '
        set -eu
        INIT_FILE="${FACTUCORE_VAULT_SECRETS_DIR:-/vault/secrets-persist}/vault-init.json"
        ROOT_TOKEN="${FACTUCORE_VAULT_ROOT_TOKEN:-}"
        if [ -z "$ROOT_TOKEN" ]; then
          ROOT_TOKEN=$(grep root_token "$INIT_FILE" | head -n 1 | cut -d "\"" -f 4)
        fi
        [ -n "$ROOT_TOKEN" ] || { echo "No se pudo obtener el token administrativo de Vault." >&2; exit 1; }
        export VAULT_TOKEN="$ROOT_TOKEN"
        SECRET_PATH="$1"
        if PASSWORD_EXISTING=$(vault kv get -field=password "$SECRET_PATH" 2>/dev/null) && [ -n "$PASSWORD_EXISTING" ]; then
          printf "El secreto ya existe y es valido; se conserva: %s\n" "$SECRET_PATH"
          exit 0
        fi
        vault kv put "$SECRET_PATH" password=@/dev/stdin >/dev/null
        vault kv get -field=password "$SECRET_PATH" >/dev/null
      ' sh "$secret_path" >/dev/null
  fi
}
ensure_required_secrets() {
  local key secret_path username description password confirm_password

  while IFS='|' read -r key secret_path username description; do
    case "$key" in ""|\#*) continue ;; esac

    if vault_secret_exists "$secret_path" "$username"; then
      printf "  [OK]     %s\n" "$secret_path"
      continue
    fi

    printf "  [FALTA]  %s\n" "$secret_path"
    while true; do
      printf '\n%s\n' "$description"
      IFS= read -r -s -p "Contrasena: " password </dev/tty
      printf '\n'
      [ -n "$password" ] || {
        printf 'La contrasena no puede estar vacia.\n'
        continue
      }

      IFS= read -r -s -p "Confirma la contrasena: " confirm_password </dev/tty
      printf '\n'
      if [ "$password" = "$confirm_password" ]; then
        break
      fi

      printf 'Las contrasenas no coinciden. Intentalo de nuevo.\n'
      unset password confirm_password
    done

    if ! store_secret "$secret_path" "$username" "$password"; then
      unset password confirm_password
      fail "No se pudo guardar el secreto $secret_path en Vault."
    fi
    unset password confirm_password

    if ! vault_secret_exists "$secret_path" "$username"; then
      fail "El secreto guardado no pudo verificarse en Vault: $secret_path"
    fi
    printf "  [OK]     %s (guardado y verificado)\n" "$secret_path"
  done < <(parse_secret_catalog)
}

CREAR_VAULT=false
if [ ! -f "$INIT_FILE" ]; then
  CREAR_VAULT=true
fi

if [ "$CREAR_VAULT" = true ]; then
  log "Preparando una instalacion nueva de Vault."
else
  log "Se detecto una instalacion existente de Vault; se conservaran los secretos existentes."
fi

log "Iniciando unicamente Vault para inicializarlo y dejarlo operativo."
"${COMPOSE[@]}" up -d vault
wait_for_vault || fail "Vault no quedo inicializado y desbloqueado. No se levantara el resto de la aplicacion."

log "Validando las credenciales definidas en secretos.yml."
printf "Almacenamiento persistente configurado: %s\n" "$VAULT_DATA_DIR"
ensure_required_secrets

log "Vault esta operativo y todos los secretos requeridos fueron verificados."
CURRENT_REALM="$(sed -n 's/^KEYCLOAK_REALM=//p' "$ENV_FILE" | tail -n 1 | tr -d '\r')"
CURRENT_REALM="${CURRENT_REALM:-facturador}"
printf "\nRealm de Keycloak para esta instalación [%s]: " "$CURRENT_REALM"
IFS= read -r REALM_NAME </dev/tty
REALM_NAME="${REALM_NAME:-$CURRENT_REALM}"
case "$REALM_NAME" in *[!a-zA-Z0-9._-]*|'') fail "Nombre de realm inválido." ;; esac
if grep -q '^KEYCLOAK_REALM=' "$ENV_FILE"; then sed -i "s|^KEYCLOAK_REALM=.*|KEYCLOAK_REALM=$REALM_NAME|" "$ENV_FILE"; else printf "\nKEYCLOAK_REALM=%s\n" "$REALM_NAME" >> "$ENV_FILE"; fi
while true; do
  IFS= read -r -s -p "Contraseña permanente para superApp: " SUPERAPP_PASSWORD </dev/tty; printf "\n"
  [ -n "$SUPERAPP_PASSWORD" ] || { printf "La contraseña no puede estar vacía.\n"; continue; }
  IFS= read -r -s -p "Confirma la contraseña de superApp: " SUPERAPP_PASSWORD_CONFIRM </dev/tty; printf "\n"
  if [ "$SUPERAPP_PASSWORD" = "$SUPERAPP_PASSWORD_CONFIRM" ]; then break; fi
  printf "Las contraseñas no coinciden. Inténtalo de nuevo.\n"; unset SUPERAPP_PASSWORD SUPERAPP_PASSWORD_CONFIRM
done
unset SUPERAPP_PASSWORD_CONFIRM
log "Iniciando Keycloak y sus dependencias para aprovisionar el realm."
"${COMPOSE[@]}" up -d auth-server
if ! printf '%s\n' "$SUPERAPP_PASSWORD" | bash "$ROOT_DIR/infraestructura/keycloak/provisionar-realm.sh" "$REALM_NAME"; then unset SUPERAPP_PASSWORD; fail "No fue posible aprovisionar Keycloak."; fi
unset SUPERAPP_PASSWORD
exec "$ROOT_DIR/levanta-aplicacion.sh"
