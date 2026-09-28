# ============================================================
# RUNTIME
# ============================================================

FROM eclipse-temurin:21-jre

ENV APP_HOME=/app

WORKDIR ${APP_HOME}

RUN groupadd --system factucore \
    && useradd --system \
       --gid factucore \
       --home-dir ${APP_HOME} \
       --no-create-home \
       --shell /usr/sbin/nologin \
       factucore

RUN mkdir -p \
      ${APP_HOME}/config \
      ${APP_HOME}/logs \
      ${APP_HOME}/certificados \
      ${APP_HOME}/jasper \
      ${APP_HOME}/documentos \
    && chown -R factucore:factucore ${APP_HOME}

# El JAR debe existir previamente en target/.
# Docker solo empaqueta el artefacto generado por Maven.
COPY target/facturador-1.0.0.jar ${APP_HOME}/facturador.jar

RUN chown factucore:factucore ${APP_HOME}/facturador.jar

USER factucore

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "/app/facturador.jar", "--spring.config.additional-location=file:/app/config/", "--logging.config=file:/app/config/log4j2-spring.xml"]
