#!/bin/sh
set -eu

SECRETS_DIR="/vault/secrets"
VAULT_UID="$(id -u vault)"
VAULT_GID="$(id -g vault)"

echo "==> Preparando permisos de ${SECRETS_DIR}"
mkdir -p "${SECRETS_DIR}"
chown "${VAULT_UID}:${VAULT_GID}" "${SECRETS_DIR}"
chmod 0755 "${SECRETS_DIR}"

echo "==> Iniciando Vault Agent como usuario vault"
exec su-exec "${VAULT_UID}:${VAULT_GID}" vault "$@"
