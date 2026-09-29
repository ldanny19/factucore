FROM apache/kafka:4.0.2

USER root

RUN groupadd --system factucore && useradd --system --gid factucore --home-dir /home/factucore --shell /usr/sbin/nologin factucore \
    && mkdir -p /home/factucore \
    && chown -R factucore:factucore /home/factucore

USER factucore
