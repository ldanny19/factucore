#!/bin/sh
set -eu

VAULT_ADDR="${VAULT_ADDR:-http://127.0.0.1:8200}"
export VAULT_ADDR

log() {
  printf '\n==> %s\n' "$1"
}

VAULT_PID=""

cleanup() {
  if [ -n "${VAULT_PID:-}" ] && kill -0 "$VAULT_PID" 2>/dev/null; then
    log "Deteniendo Vault"
    kill "$VAULT_PID" 2>/dev/null || true
    wait "$VAULT_PID" 2>/dev/null || true
  fi
}

trap cleanup INT TERM

log "Iniciando Vault Server"
vault server "$@" &
VAULT_PID=$!

log "Esperando a que el servidor de Vault este disponible"
READY=0
for _ in $(seq 1 60); do
  set +e
  vault status -format=json >/tmp/vault-status.json 2>/dev/null
  STATUS_RC=$?
  set -e

  if grep -q '"initialized"' /tmp/vault-status.json 2>/dev/null; then
    READY=1
    break
  fi

  if ! kill -0 "$VAULT_PID" 2>/dev/null; then
    wait "$VAULT_PID" || true
    exit 1
  fi

  sleep 2
done

[ "$READY" -eq 1 ] || {
  log "Vault no quedo disponible dentro del tiempo esperado"
  kill "$VAULT_PID" 2>/dev/null || true
  wait "$VAULT_PID" 2>/dev/null || true
  exit 1
}

log "Ejecutando configuracion inicial de Vault"
/vault/scripts/bootstrap-vault.sh

log "Vault quedo inicializado, desellado y configurado"
wait "$VAULT_PID"
