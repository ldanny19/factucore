#!/usr/bin/env bash
set -Eeuo pipefail
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
ENV_FILE="$ROOT_DIR/compose/.env"; COMPOSE_FILE="$ROOT_DIR/compose/docker-compose.yml"
fail() { printf "\nERROR: %s\n" "$1" >&2; exit 1; }
[ -f "$ENV_FILE" ] && [ -f "$COMPOSE_FILE" ] || fail "Falta la configuración Compose."
command -v docker >/dev/null 2>&1 || fail "Se requiere Docker."; docker compose version >/dev/null 2>&1 || fail "Docker Compose v2 no disponible."
REALM_NAME="${1:-}"; [ -n "$REALM_NAME" ] || fail "Debe indicarse el realm."
case "$REALM_NAME" in *[!a-zA-Z0-9._-]*|'') fail "Nombre de realm inválido." ;; esac
IFS= read -r APP_PASSWORD || fail "No se recibió la contraseña de superApp."; [ -n "$APP_PASSWORD" ] || fail "La contraseña no puede estar vacía."
ENV_FILE_NATIVE="$ENV_FILE"; COMPOSE_FILE_NATIVE="$COMPOSE_FILE"
if command -v cygpath >/dev/null 2>&1; then ENV_FILE_NATIVE="$(cygpath -w "$ENV_FILE")"; COMPOSE_FILE_NATIVE="$(cygpath -w "$COMPOSE_FILE")"; fi
COMPOSE=(docker compose --env-file "$ENV_FILE_NATIVE" -f "$COMPOSE_FILE_NATIVE")
READY=false; for attempt in $(seq 1 90); do status="$(docker inspect -f '{{.State.Health.Status}}' auth-server 2>/dev/null || true)"; if [ "$status" = healthy ]; then READY=true; break; fi; sleep 2; done
[ "$READY" = true ] || { "${COMPOSE[@]}" logs --tail=80 auth-server >&2 || true; fail "Keycloak no quedó disponible."; }
printf '%s\n%s\n' "$REALM_NAME" "$APP_PASSWORD" | MSYS_NO_PATHCONV=1 "${COMPOSE[@]}" exec -T auth-server /bin/sh -c '
  set -eu; IFS= read -r REALM_NAME; IFS= read -r APP_PASSWORD; ADMIN_PASSWORD=$(cat /vault/secrets/keycloak_admin_password); KCADM=/opt/keycloak/bin/kcadm.sh
  [ -n "$ADMIN_PASSWORD" ] || { echo "No se encontró la credencial administrativa en Vault." >&2; exit 1; }
  "$KCADM" config credentials --server http://127.0.0.1:8080 --realm master --user admin --password "$ADMIN_PASSWORD" >/dev/null
  if ! "$KCADM" get "realms/$REALM_NAME" >/dev/null 2>&1; then echo "Creando realm."; "$KCADM" create realms -s "realm=$REALM_NAME" -s enabled=true >/dev/null; else echo "Realm existente; se conserva."; fi
  if ! "$KCADM" get "roles/SUPERADMIN" -r "$REALM_NAME" >/dev/null 2>&1; then "$KCADM" create roles -r "$REALM_NAME" -s name=SUPERADMIN >/dev/null; fi
  SUPERADMIN_ID=$("$KCADM" get "roles/SUPERADMIN" -r "$REALM_NAME" 2>/dev/null | grep -oE "\"id\"[[:space:]]*:[[:space:]]*\"[^\"]+\"" | head -n 1 | cut -d ":" -f 2 | tr -d " \"" || true)
  [ -n "$SUPERADMIN_ID" ] || { echo "No fue posible obtener el ID del rol SUPERADMIN." >&2; exit 1; }
  for ADMIN_ROLE in query-users view-users manage-users view-realm manage-realm; do
    if ! "$KCADM" get "roles-by-id/$SUPERADMIN_ID/composites" -r "$REALM_NAME" 2>/dev/null | grep -Eq "\"name\"[[:space:]]*:[[:space:]]*\"$ADMIN_ROLE\""; then
      "$KCADM" add-roles -r "$REALM_NAME" --rname SUPERADMIN --cclientid realm-management --rolename "$ADMIN_ROLE" >/dev/null
    fi
  done
  CLIENT_ID=$("$KCADM" get clients -r "$REALM_NAME" -q "clientId=facturador-app" 2>/dev/null | grep -oE '"id"[[:space:]]*:[[:space:]]*"[^"]+"' | head -n 1 | cut -d ":" -f 2 | tr -d ' "' || true)
  if [ -z "$CLIENT_ID" ]; then
    echo "Creando cliente facturador-app para el portal FactuCore."
    "$KCADM" create clients -r "$REALM_NAME" \
      -s clientId=facturador-app \
      -s enabled=true \
      -s publicClient=true \
      -s directAccessGrantsEnabled=true \
      -s standardFlowEnabled=true \
      -s "redirectUris=[\"http://localhost:3000/*\",\"http://api.facturador.com:8080/*\",\"http://app.factucore.local/*\"]" \
      -s "webOrigins=[\"http://localhost:3000\",\"http://api.facturador.com:8080\",\"http://app.factucore.local\"]" >/dev/null
  else
    echo "Actualizando cliente facturador-app para habilitar el login del portal."
    "$KCADM" update "clients/$CLIENT_ID" -r "$REALM_NAME" \
      -s enabled=true \
      -s publicClient=true \
      -s directAccessGrantsEnabled=true \
      -s standardFlowEnabled=true \
      -s "redirectUris=[\"http://localhost:3000/*\",\"http://api.facturador.com:8080/*\",\"http://app.factucore.local/*\"]" \
      -s "webOrigins=[\"http://localhost:3000\",\"http://api.facturador.com:8080\",\"http://app.factucore.local\"]" >/dev/null
  fi
  USER_ID=$("$KCADM" get users -r "$REALM_NAME" -q "username=superApp" 2>/dev/null | grep -oE "\"id\"[[:space:]]*:[[:space:]]*\"[^\"]+\"" | head -n 1 | cut -d ":" -f 2 | tr -d " \"" || true)
  if [ -z "$USER_ID" ]; then
    echo "Creando usuario superApp."
    "$KCADM" create users -r "$REALM_NAME" -s username=superApp -s enabled=true >/dev/null
    "$KCADM" set-password -r "$REALM_NAME" --username superApp --new-password "$APP_PASSWORD" >/dev/null
    USER_ID=$("$KCADM" get users -r "$REALM_NAME" -q "username=superApp" 2>/dev/null | grep -oE "\"id\"[[:space:]]*:[[:space:]]*\"[^\"]+\"" | head -n 1 | cut -d ":" -f 2 | tr -d " \"" || true)
  else
    echo "superApp existente; se conserva su contraseña."
  fi
  [ -n "$USER_ID" ] || { echo "No fue posible obtener el ID del usuario superApp desde Keycloak." >&2; exit 1; }
  if ! "$KCADM" get-roles -r "$REALM_NAME" --uid "$USER_ID" 2>/dev/null | grep -Fq "SUPERADMIN"; then
    "$KCADM" add-roles -r "$REALM_NAME" --uid "$USER_ID" --rolename SUPERADMIN >/dev/null
  fi
  echo "Aprovisionamiento de Keycloak finalizado."
'
unset APP_PASSWORD
printf "\n==> Realm, permisos del rol SUPERADMIN y usuario superApp verificados.\n"
