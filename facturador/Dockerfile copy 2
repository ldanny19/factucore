FROM maven:3.9.11-eclipse-temurin-21 AS builder
ENV APP_WORKSPACE=/app/workspace
WORKDIR ${APP_WORKSPACE}

RUN --mount=type=bind,source=factucore-messaging,target=/tmp/messaging,ro \
    --mount=type=cache,target=/tmp/messaging/target \
    --mount=type=cache,target=/root/.m2 \
    cd /tmp/messaging && \
    mvn dependency:go-offline && \
    mvn install -DskipTests

COPY facturador/pom.xml .
RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline

COPY facturador/src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn clean package -DskipTests


FROM eclipse-temurin:21-jre-alpine
ENV APP_HOME=/app

RUN apk add --no-cache wget \
    && addgroup -S factucore \
    && adduser -S -G factucore -h ${APP_HOME} -s /sbin/nologin -D factucore \
    && mkdir -p ${APP_HOME}/config ${APP_HOME}/logs ${APP_HOME}/certificados ${APP_HOME}/jasper ${APP_HOME}/documentos \
    && chown -R factucore:factucore ${APP_HOME}

COPY --chown=factucore:factucore --from=builder /app/workspace/target/*.jar ${APP_HOME}/facturador.jar

USER factucore

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/facturador.jar", "--spring.config.additional-location=file:/app/config/", "--logging.config=file:/app/config/log4j2-spring.xml"]
