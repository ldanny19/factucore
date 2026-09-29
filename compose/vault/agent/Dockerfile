FROM hashicorp/vault:1.20

USER root

RUN addgroup -S factucore && adduser -S -G factucore -h /home/factucore factucore \
    && mkdir -p /vault/agent /vault/secrets \
    && chown -R factucore:factucore /vault/agent /vault/secrets

USER factucore
