FROM postgres:17

USER root

RUN addgroup --system factucore && adduser --system --ingroup factucore --home /home/factucore --shell /usr/sbin/nologin factucore \
    && mkdir -p /var/lib/postgresql/data \
    && chown -R factucore:factucore /var/lib/postgresql

USER factucore
