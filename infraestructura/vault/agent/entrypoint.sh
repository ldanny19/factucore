#!/bin/sh
set -eu

SECRETS_DIR="/vault/secrets"

if [ -d "$SECRETS_DIR" ]; then
    chmod 0755 "$SECRETS_DIR"
fi

exec vault "$@"
