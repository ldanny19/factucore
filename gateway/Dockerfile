FROM eclipse-temurin:21-jre

ARG APP_VERSION=1.0.0

ENV APP_HOME=/app

WORKDIR ${APP_HOME}

RUN apt-get update \
    && apt-get install -y --no-install-recommends wget \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system factucore \
    && useradd --system \
       --gid factucore \
       --home-dir ${APP_HOME} \
       --no-create-home \
       --shell /usr/sbin/nologin \
       factucore \
    && mkdir -p ${APP_HOME}/config \
    && chown -R factucore:factucore ${APP_HOME}

COPY target/gateway-${APP_VERSION}.jar ${APP_HOME}/gateway.jar

RUN chown factucore:factucore ${APP_HOME}/gateway.jar

USER factucore

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/gateway.jar", "--spring.config.additional-location=file:/app/config/"]
