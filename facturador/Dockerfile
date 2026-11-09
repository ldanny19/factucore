
FROM maven:3.9.11-eclipse-temurin-21 as builder
ENV APP_WORKSPACE=/app
WORKDIR ${APP_WORKSPACE}
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package  -DskipTests


FROM eclipse-temurin:21-jre 
ENV APP_HOME=/app
RUN apt-get update \
    && apt-get install -y --no-install-recommends wget \
    && rm -rf /var/lib/apt/lists/*

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
COPY --from=builder ${APP_WORKSPACE}/target/facturador-*.jar ${APP_HOME}/facturador.jar

RUN chown factucore:factucore ${APP_HOME}/facturador.jar

USER factucore

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/facturador.jar", "--spring.config.additional-location=file:/app/config/", "--logging.config=file:/app/config/log4j2-spring.xml"]
