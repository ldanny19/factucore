FROM keycloak/keycloak:26.7.4-0

USER root

RUN groupadd --system factucore && useradd --system --gid factucore --home-dir /home/factucore --shell /usr/sbin/nologin factucore \
    && mkdir -p /home/factucore /opt/keycloak/data \
    && chown -R factucore:factucore /home/factucore /opt/keycloak/data

USER factucore
