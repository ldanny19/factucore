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
  USER_EXISTS=$("$KCADM" get users -r "$REALM_NAME" -q "username=superApp" 2>/dev/null || true)
  if ! printf "%s" "$USER_EXISTS" | grep -q "superApp"; then "$KCADM" create users -r "$REALM_NAME" -s username=superApp -s enabled=true >/dev/null; "$KCADM" set-password -r "$REALM_NAME" --username superApp --new-password "$APP_PASSWORD" >/dev/null; else echo "superApp existente; se conserva su contraseña."; fi
  if ! "$KCADM" get-roles -r "$REALM_NAME" --uusername superApp 2>/dev/null | grep -Fq "SUPERADMIN"; then "$KCADM" add-roles -r "$REALM_NAME" --uusername superApp --rolename SUPERADMIN >/dev/null; fi
  echo "Aprovisionamiento de Keycloak finalizado."
'
unset APP_PASSWORD
printf "\n==> Realm, rol SUPERADMIN y usuario superApp verificados.\n"
