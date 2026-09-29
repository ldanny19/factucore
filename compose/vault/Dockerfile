FROM hashicorp/vault:1.20

USER root

RUN addgroup -S factucore && adduser -S -G factucore -h /home/factucore factucore \
    && mkdir -p /vault/data /vault/config /vault/scripts /vault/policy /vault/agent-token \
    && chown -R factucore:factucore /vault/data /vault/config /vault/scripts /vault/policy /vault/agent-token

USER factucore
