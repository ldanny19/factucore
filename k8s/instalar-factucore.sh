#!/usr/bin/env bash
set -Eeuo pipefail

K8S_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$K8S_DIR/.." && pwd)"
CATALOG_FILE="$ROOT_DIR/infraestructura/vault/config/secretos.yml"
CHART_FILE="$K8S_DIR/Chart.yaml"
ENVIRONMENT="${1:-develop}"
ENV_FILE="$K8S_DIR/env/$ENVIRONMENT.yaml"
NAMESPACE_FILE="$K8S_DIR/namespaces/$ENVIRONMENT.yaml"
RELEASE_NAME="factucore"
NAMESPACE=""
BOOTSTRAP_HELM_ARGS=()

log() {
  printf '\n==> %s\n' "$1"
}

fail() {
  printf '\nERROR: %s\n' "$1" >&2
  exit 1
}

for command in helm kubectl awk grep head cut sleep seq; do
  command -v "$command" >/dev/null 2>&1 || fail "Se requiere el comando '$command'."
done

case "$ENVIRONMENT" in
  develop|prod) ;;
  *) fail "Entorno inválido '$ENVIRONMENT'. Usa develop o prod." ;;
esac

[ -f "$CATALOG_FILE" ] || fail "No existe el catálogo de secretos: $CATALOG_FILE."
[ -f "$ENV_FILE" ] || fail "No existe el archivo de valores: $ENV_FILE."
[ -f "$NAMESPACE_FILE" ] || fail "No existe el manifiesto del namespace: $NAMESPACE_FILE."
[ -f "$CHART_FILE" ] || fail "No existe el chart principal: $CHART_FILE."

NAMESPACE="$(awk '$1 == "name:" { print $2; exit }' "$NAMESPACE_FILE")"
[ -n "$NAMESPACE" ] || fail "No se pudo obtener el nombre del namespace de $NAMESPACE_FILE."

if ! printf '%s' "$NAMESPACE" | grep -Eq '^[a-z0-9]([-a-z0-9]*[a-z0-9])?$'; then
  fail "El nombre del namespace no es válido: $NAMESPACE."
fi

log "Creando o validando el namespace $NAMESPACE."
kubectl apply -f "$NAMESPACE_FILE"

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
        printf "Catálogo inválido en la entrada %s.\n", key > "/dev/stderr"
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
      printf "Catálogo inválido: formato no admitido en línea %d.\n", NR > "/dev/stderr"
      invalid = 1
    }
    END {
      emit_entry()
      if (entries == 0) invalid = 1
      if (invalid) exit 1
    }
  ' "$CATALOG_FILE"
}

parse_secret_catalog >/dev/null || fail "El catálogo de secretos no tiene el formato admitido."

chart_module_names() {
  awk '
    /^[[:space:]]+condition:[[:space:]]*[A-Za-z0-9_-]+\.enabled[[:space:]]*$/ {
      value = $0
      sub(/^[[:space:]]*condition:[[:space:]]*/, "", value)
      sub(/\.enabled[[:space:]]*$/, "", value)
      print value
    }
  ' "$CHART_FILE"
}

build_bootstrap_helm_args() {
  local agent_enabled="$1"
  local module

  BOOTSTRAP_HELM_ARGS=(--set vault.enabled=true --set "vault.agent.enabled=$agent_enabled")
  while IFS= read -r module; do
    [ -n "$module" ] || continue
    [ "$module" = vault ] && continue
    BOOTSTRAP_HELM_ARGS+=(--set "$module.enabled=false")
  done < <(chart_module_names)
}

kubectl_path() {
  if command -v cygpath >/dev/null 2>&1; then
    cygpath -w "$1"
  else
    printf '%s\n' "$1"
  fi
}

apply_configmap() {
  local name="$1"
  shift
  local source_spec key source_path native_source
  local configmap_args=()

  for source_spec in "$@"; do
    if [[ "$source_spec" == @* ]]; then
      source_path="${source_spec#@}"
      [ -d "$source_path" ] || fail "No existe la carpeta de configuración para $name: $source_path."
      native_source="$(kubectl_path "$source_path")"
      configmap_args+=(--from-file="$native_source")
    else
      key="${source_spec%%=*}"
      source_path="${source_spec#*=}"
      [ "$key" != "$source_spec" ] || fail "Fuente inválida para el ConfigMap $name: $source_spec."
      [ -f "$source_path" ] || fail "No existe el archivo de configuración para $name: $source_path."
      native_source="$(kubectl_path "$source_path")"
      configmap_args+=(--from-file="$key=$native_source")
    fi
  done

  MSYS_NO_PATHCONV=1 kubectl create configmap "$name" \
    --namespace "$NAMESPACE" \
    --dry-run=client \
    -o yaml \
    "${configmap_args[@]}" |
    MSYS_NO_PATHCONV=1 kubectl apply \
      --server-side \
      --force-conflicts \
      --field-manager=factucore-installer \
      -f -
}

apply_component_configmaps() {
  log "Publicando configuraciones desde infraestructura/ y configuraciones/."

  apply_configmap cm-vault \
    "vault.hcl=$ROOT_DIR/infraestructura/vault/config/vault.hcl" \
    "secretos.yml=$ROOT_DIR/infraestructura/vault/config/secretos.yml" \
    "entrypoint.sh=$ROOT_DIR/infraestructura/vault/scripts/entrypoint.sh" \
    "bootstrap-vault.sh=$ROOT_DIR/infraestructura/vault/scripts/bootstrap-vault.sh" \
    "factucore-agent.hcl=$ROOT_DIR/infraestructura/vault/policy/factucore-agent.hcl"
  apply_configmap cm-vault-agent \
    "config.hcl=$ROOT_DIR/infraestructura/vault/agent/config.hcl"
  apply_configmap cm-db-factucore-init \
    "01-crear-bases-servicios.sql=$ROOT_DIR/infraestructura/postgres/init/01-crear-bases-servicios.sql"
  apply_configmap cm-facturador-config \
    "application.yml=$ROOT_DIR/configuraciones/facturador/config/application.yml" \
    "log4j2-spring.xml=$ROOT_DIR/configuraciones/facturador/config/log4j2-spring.xml" \
    "messages.properties=$ROOT_DIR/configuraciones/facturador/config/messages.properties" \
    "messages_es_EC.properties=$ROOT_DIR/configuraciones/facturador/config/messages_es_EC.properties" \
    "workflow-facturacion.xml=$ROOT_DIR/configuraciones/facturador/config/workflow/facturacion.xml"
  apply_configmap cm-facturador-jasper \
    "@$ROOT_DIR/configuraciones/facturador/jasper"
  apply_configmap cm-notificacion-config \
    "application.yml=$ROOT_DIR/configuraciones/notificacion/config/application.yml" \
    "messages.properties=$ROOT_DIR/configuraciones/notificacion/config/messages.properties" \
    "messages_es_EC.properties=$ROOT_DIR/configuraciones/notificacion/config/messages_es_EC.properties" \
    "plantillas-comprobante-autorizado.html=$ROOT_DIR/configuraciones/notificacion/config/plantillas/comprobante-autorizado.html"
  apply_configmap cm-messaging-config \
    "message.properties=$ROOT_DIR/configuraciones/messaging/config/message.properties"
  apply_configmap cm-nginx-config \
    "default.conf=$ROOT_DIR/configuraciones/nginx/config/default.conf"
  apply_configmap cm-web-config \
    "@$ROOT_DIR/configuraciones/web/config"
}

log "Actualizando los paquetes locales de los componentes Helm."
helm dependency update "$K8S_DIR" ||
  fail "No se pudieron actualizar los charts de los componentes."

apply_component_configmaps

manifest_has_resource() {
  local manifest="$1"
  local resource_kind="$2"
  local resource_name="$3"

  printf '%s\n' "$manifest" |
    awk -v target_kind="$resource_kind" -v target_name="$resource_name" '
      /^---[[:space:]]*$/ {
        kind = ""
        in_metadata = 0
        next
      }
      /^kind:[[:space:]]/ {
        kind = $2
        next
      }
      /^metadata:[[:space:]]*$/ {
        in_metadata = 1
        next
      }
      in_metadata && /^  name:[[:space:]]/ {
        name = $2
        in_metadata = 0
        if (kind == target_kind && name == target_name)
          found = 1
      }
      END { exit !found }
    '
}

FINAL_MANIFEST="$(helm template "$RELEASE_NAME" "$K8S_DIR" \
  --values "$ENV_FILE" \
  --namespace "${NAMESPACE}")" ||
  fail "No se pudieron evaluar los módulos habilitados en $ENV_FILE."

VAULT_ENABLED=false
DATABASE_ENABLED=false
AUTH_ENABLED=false
KAFKA_ENABLED=false
FACTURADOR_ENABLED=false
NOTIFICACION_ENABLED=false
APIGATEWAY_ENABLED=false
WEB_ENABLED=false
if manifest_has_resource "$FINAL_MANIFEST" StatefulSet sts-vault; then
  VAULT_ENABLED=true
fi
if manifest_has_resource "$FINAL_MANIFEST" StatefulSet db-factucore; then
  DATABASE_ENABLED=true
fi
if manifest_has_resource "$FINAL_MANIFEST" Deployment auth-server; then
  AUTH_ENABLED=true
fi
if manifest_has_resource "$FINAL_MANIFEST" StatefulSet kafka; then
  KAFKA_ENABLED=true
fi
if manifest_has_resource "$FINAL_MANIFEST" Deployment dp-facturador; then
  FACTURADOR_ENABLED=true
fi
if manifest_has_resource "$FINAL_MANIFEST" Deployment dp-notificacion; then
  NOTIFICACION_ENABLED=true
fi
if manifest_has_resource "$FINAL_MANIFEST" Deployment dp-apigateway; then
  APIGATEWAY_ENABLED=true
fi
if manifest_has_resource "$FINAL_MANIFEST" Deployment dp-web; then
  WEB_ENABLED=true
fi

# Compose starts declared dependencies automatically. Apply the same dependency
# closure when a Kubernetes component is selected on its own.
if [ "$APIGATEWAY_ENABLED" = true ]; then
  NOTIFICACION_ENABLED=true
  FACTURADOR_ENABLED=true
fi
if [ "$NOTIFICACION_ENABLED" = true ]; then
  FACTURADOR_ENABLED=true
  AUTH_ENABLED=true
  KAFKA_ENABLED=true
  DATABASE_ENABLED=true
fi
if [ "$FACTURADOR_ENABLED" = true ]; then
  AUTH_ENABLED=true
  KAFKA_ENABLED=true
  DATABASE_ENABLED=true
fi
if [ "$AUTH_ENABLED" = true ]; then
  DATABASE_ENABLED=true
fi
if [ "$AUTH_ENABLED" = true ] || [ "$DATABASE_ENABLED" = true ] || [ "$KAFKA_ENABLED" = true ]; then
  VAULT_ENABLED=true
fi

SELECTED_MODULES=()
DEPLOYED_MODULES=()
append_module_if_enabled() {
  local module="$1"
  local enabled="$2"
  if [ "$enabled" = true ]; then
    SELECTED_MODULES+=("$module")
  fi
  return 0
}

append_deployed_module() {
  DEPLOYED_MODULES+=("$1")
}

helm_deploy_selection() {
  local vault_enabled="$1"
  local agent_enabled="$2"
  shift 2
  local module
  local -a helm_args=()

  helm_args=(--set "vault.enabled=$vault_enabled" --set "vault.agent.enabled=$agent_enabled")
  while IFS= read -r module; do
    [ -n "$module" ] || continue
    [ "$module" = vault ] && continue
    if printf '%s\n' "$@" | grep -Fxq "$module"; then
      helm_args+=(--set "$module.enabled=true")
    else
      helm_args+=(--set "$module.enabled=false")
    fi
  done < <(chart_module_names)

  helm upgrade --install "$RELEASE_NAME" "$K8S_DIR" \
    --values "$ENV_FILE" \
    --namespace "$NAMESPACE" \
    --reset-values \
    "${helm_args[@]}"
  apply_component_configmaps
}

append_module_if_enabled basedatos "$DATABASE_ENABLED"
append_module_if_enabled kafka "$KAFKA_ENABLED"
append_module_if_enabled auth "$AUTH_ENABLED"
append_module_if_enabled facturador "$FACTURADOR_ENABLED"
append_module_if_enabled notificacion "$NOTIFICACION_ENABLED"
append_module_if_enabled apigateway "$APIGATEWAY_ENABLED"
append_module_if_enabled web "$WEB_ENABLED"

log "Módulos solicitados o requeridos por dependencias: ${SELECTED_MODULES[*]:-(ninguno)}."

vault_secret_exists() {
  local secret_path="$1"
  local username="$2"

  MSYS_NO_PATHCONV=1 kubectl exec --namespace "$NAMESPACE" sts-vault-0 -- sh -c '
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

  if [ -n "$username" ]; then
    printf '%s' "$password" |
      MSYS_NO_PATHCONV=1 kubectl exec --stdin --namespace "$NAMESPACE" sts-vault-0 -- sh -c '
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
          if [ "$(vault kv get -field=username "$SECRET_PATH" 2>/dev/null)" = "$SECRET_USERNAME" ]; then
            printf "El secreto ya existe y es válido; se conserva: %s\n" "$SECRET_PATH"
            exit 0
          fi
        fi
        vault kv put "$SECRET_PATH" username="$SECRET_USERNAME" password=@/dev/stdin >/dev/null
        vault kv get -field=password "$SECRET_PATH" >/dev/null
      ' sh "$secret_path" "$username" >/dev/null
  else
    printf '%s' "$password" |
      MSYS_NO_PATHCONV=1 kubectl exec --stdin --namespace "$NAMESPACE" sts-vault-0 -- sh -c '
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
          printf "El secreto ya existe y es válido; se conserva: %s\n" "$SECRET_PATH"
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
      IFS= read -r -s -p "Contraseña: " password </dev/tty
      printf '\n'
      [ -n "$password" ] || {
        printf 'La contraseña no puede estar vacía.\n'
        continue
      }

      IFS= read -r -s -p "Confirma la contraseña: " confirm_password </dev/tty
      printf '\n'
      if [ "$password" = "$confirm_password" ]; then
        break
      fi

      printf 'Las contraseñas no coinciden. Inténtalo de nuevo.\n'
      unset password confirm_password
    done

    if ! store_secret "$secret_path" "$username" "$password"; then
      unset password confirm_password
      fail "No se pudo guardar el secreto $secret_path en Vault."
    fi
    unset password confirm_password

    vault_secret_exists "$secret_path" "$username" ||
      fail "No se pudo verificar el secreto guardado en Vault: $secret_path."
    printf "  [OK]     %s (guardado y verificado)\n" "$secret_path"
  done < <(parse_secret_catalog)
}

wait_for_vault_bootstrap() {
  local attempt
  for attempt in $(seq 1 60); do
    if MSYS_NO_PATHCONV=1 kubectl exec --namespace "$NAMESPACE" sts-vault-0 -- \
        test -s /vault/agent-token/vault_agent_token.txt >/dev/null 2>&1; then
      return 0
    fi
    sleep 2
  done
  fail "Vault no completó el bootstrap ni generó el token del Agent."
}

if [ "$VAULT_ENABLED" = true ]; then
  RELEASE_EXISTS="$(helm list --filter "^$RELEASE_NAME$" \
    --namespace "$NAMESPACE" --short)"

  if [ -z "$RELEASE_EXISTS" ]; then
    log "Instalando únicamente Vault; los consumidores aún no se desplegarán."
    build_bootstrap_helm_args false
    helm upgrade --install "$RELEASE_NAME" "$K8S_DIR" \
      --values "$ENV_FILE" \
      --namespace "$NAMESPACE" \
      --reset-values \
      "${BOOTSTRAP_HELM_ARGS[@]}"
    apply_component_configmaps
  elif ! MSYS_NO_PATHCONV=1 kubectl get statefulset sts-vault \
      --namespace "$NAMESPACE" >/dev/null 2>&1; then
    PREVIOUS_MANIFEST="$(helm get manifest "$RELEASE_NAME" --namespace "$NAMESPACE")" ||
      fail "No se pudo inspeccionar el manifiesto del release Helm existente."

    if manifest_has_resource "$PREVIOUS_MANIFEST" Deployment auth-server ||
      manifest_has_resource "$PREVIOUS_MANIFEST" StatefulSet kafka ||
      manifest_has_resource "$PREVIOUS_MANIFEST" Deployment dp-facturador ||
      manifest_has_resource "$PREVIOUS_MANIFEST" Deployment dp-notificacion ||
      manifest_has_resource "$PREVIOUS_MANIFEST" Deployment dp-apigateway ||
      manifest_has_resource "$PREVIOUS_MANIFEST" Deployment dp-web; then
      fail "El release existe sin Vault y tiene otros módulos declarados. No se cambiarán sus valores automáticamente; revisa helm get manifest antes de recuperar."
    fi

    ACTIVE_WORKLOADS="$(kubectl get deployments,statefulsets \
      --namespace "$NAMESPACE" -o name)" ||
      fail "No se pudo comprobar si hay workloads activos en el namespace."
    [ -z "$ACTIVE_WORKLOADS" ] ||
      fail "El release existe sin Vault, pero hay workloads activos. No se modificarán; revísalos antes de recuperar."

    EXTRA_PVCS="$(kubectl get persistentvolumeclaims --namespace "$NAMESPACE" \
      --no-headers -o custom-columns=NAME:.metadata.name |
      grep -Ev '^(pvc-vault-(data|secrets|agent-token|runtime-secrets)|datos-db-factucore-[0-9]+)$' || true)"
    [ -z "$EXTRA_PVCS" ] ||
      fail "Se encontraron PVCs ajenas al bootstrap de Vault/PostgreSQL. No se cambiará el release: $EXTRA_PVCS"

    DATABASE_PVC_EXISTS=false
    if MSYS_NO_PATHCONV=1 kubectl get pvc datos-db-factucore-0 \
      --namespace "$NAMESPACE" >/dev/null 2>&1; then
      DATABASE_PVC_EXISTS=true
    fi

    log "Recuperando primero Vault en el release existente; se preservarán las PVCs permitidas."
    build_bootstrap_helm_args false
    helm upgrade --install "$RELEASE_NAME" "$K8S_DIR" \
      --values "$ENV_FILE" \
      --namespace "$NAMESPACE" \
      --reset-values \
      "${BOOTSTRAP_HELM_ARGS[@]}"
    apply_component_configmaps
    if [ "$DATABASE_PVC_EXISTS" = true ] &&
      ! MSYS_NO_PATHCONV=1 kubectl get pvc datos-db-factucore-0 \
        --namespace "$NAMESPACE" >/dev/null 2>&1; then
      fail "La recuperación no encontró la PVC existente de PostgreSQL; se detiene antes de desplegar la base."
    fi
  else
    log "Reutilizando Vault existente sin actualizar consumidores antes de cargar las credenciales."
  fi

  log "Esperando a que Vault quede inicializado y desellado."
  MSYS_NO_PATHCONV=1 kubectl rollout status statefulset/sts-vault \
    --namespace "$NAMESPACE" --timeout=5m
  wait_for_vault_bootstrap

  log "Validando y solicitando las credenciales faltantes del catálogo."
  ensure_required_secrets

  log "Iniciando Vault Agent después de guardar las credenciales."
  helm upgrade --install "$RELEASE_NAME" "$K8S_DIR" \
    --namespace "$NAMESPACE" \
    --reuse-values \
    --set vault.enabled=true \
    --set vault.agent.enabled=true
  apply_component_configmaps

  log "Esperando a que Vault Agent renderice todos los secretos."
  MSYS_NO_PATHCONV=1 kubectl rollout status deployment/dp-vault-agent \
    --namespace "$NAMESPACE" --timeout=5m
fi

if [ "$DATABASE_ENABLED" = true ] || [ "$KAFKA_ENABLED" = true ]; then
  [ "$VAULT_ENABLED" = true ] || fail "PostgreSQL y Kafka requieren Vault según el flujo definido en Compose."
  if [ "$DATABASE_ENABLED" = true ]; then
    append_deployed_module basedatos
  fi
  if [ "$KAFKA_ENABLED" = true ]; then
    append_deployed_module kafka
  fi
  log "Desplegando PostgreSQL y Kafka después de Vault y Vault Agent."
  helm_deploy_selection "$VAULT_ENABLED" "$VAULT_ENABLED" "${DEPLOYED_MODULES[@]}"

  if [ "$DATABASE_ENABLED" = true ]; then
    log "Esperando a que PostgreSQL esté disponible."
    MSYS_NO_PATHCONV=1 kubectl rollout status statefulset/db-factucore \
      --namespace "$NAMESPACE" --timeout=10m
  fi
  if [ "$KAFKA_ENABLED" = true ]; then
    log "Esperando a que Kafka esté disponible."
    MSYS_NO_PATHCONV=1 kubectl rollout status statefulset/kafka \
      --namespace "$NAMESPACE" --timeout=10m
  fi
fi

if [ "$AUTH_ENABLED" = true ]; then
  append_deployed_module auth
  log "Desplegando Auth después de PostgreSQL y Vault Agent."
  helm_deploy_selection "$VAULT_ENABLED" "$VAULT_ENABLED" "${DEPLOYED_MODULES[@]}"
  MSYS_NO_PATHCONV=1 kubectl rollout status deployment/auth-server \
    --namespace "$NAMESPACE" --timeout=10m
fi

if [ "$FACTURADOR_ENABLED" = true ]; then
  append_deployed_module facturador
  log "Desplegando Facturador después de PostgreSQL, Kafka y Auth."
  helm_deploy_selection "$VAULT_ENABLED" "$VAULT_ENABLED" "${DEPLOYED_MODULES[@]}"
  MSYS_NO_PATHCONV=1 kubectl rollout status deployment/dp-facturador \
    --namespace "$NAMESPACE" --timeout=10m
fi

if [ "$NOTIFICACION_ENABLED" = true ]; then
  append_deployed_module notificacion
  log "Desplegando Notificación después de Facturador, Kafka y Auth."
  helm_deploy_selection "$VAULT_ENABLED" "$VAULT_ENABLED" "${DEPLOYED_MODULES[@]}"
  MSYS_NO_PATHCONV=1 kubectl rollout status deployment/dp-notificacion \
    --namespace "$NAMESPACE" --timeout=10m
fi

if [ "$APIGATEWAY_ENABLED" = true ] || [ "$WEB_ENABLED" = true ]; then
  if [ "$APIGATEWAY_ENABLED" = true ]; then
    append_deployed_module apigateway
  fi
  if [ "$WEB_ENABLED" = true ]; then
    append_deployed_module web
  fi
  log "Desplegando el frontend y el API Gateway después de sus dependencias."
  helm_deploy_selection "$VAULT_ENABLED" "$VAULT_ENABLED" "${DEPLOYED_MODULES[@]}"
  if [ "$APIGATEWAY_ENABLED" = true ]; then
    MSYS_NO_PATHCONV=1 kubectl rollout status deployment/dp-apigateway \
      --namespace "$NAMESPACE" --timeout=10m
  fi
  if [ "$WEB_ENABLED" = true ]; then
    MSYS_NO_PATHCONV=1 kubectl rollout status deployment/dp-web \
      --namespace "$NAMESPACE" --timeout=10m
  fi
fi

if [ "${#DEPLOYED_MODULES[@]}" -eq 0 ]; then
  log "Aplicando la selección final de módulos del entorno $ENVIRONMENT."
  helm_deploy_selection "$VAULT_ENABLED" "$VAULT_ENABLED"
fi

log "Instalación de FactuCore completada para el entorno $ENVIRONMENT."
