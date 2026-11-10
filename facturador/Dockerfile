
FROM eclipse-temurin:21-jre-alpine

ENV APP_HOME=/app

RUN apk add --no-cache wget \
    && addgroup -S factucore \
    && adduser -S -G factucore -h ${APP_HOME} -s /sbin/nologin -D factucore \
    && mkdir -p ${APP_HOME}/config \
                ${APP_HOME}/logs \
                ${APP_HOME}/certificados \
                ${APP_HOME}/jasper \
                ${APP_HOME}/documentos \
    && chown -R factucore:factucore ${APP_HOME}

ARG JAR_FILE
ARG JAR_FILE_NAME

COPY --chown=factucore:factucore ${JAR_FILE} ${APP_HOME}/${JAR_FILE_NAME}

ENV APP_JAR=${JAR_FILE_NAME}

USER factucore

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "exec java -jar /app/$APP_JAR --spring.config.additional-location=file:/app/config/ --logging.config=file:/app/config/log4j2-spring.xml"]
