# ============================================================
# ETAPA 1 - BUILD
# ============================================================

FROM maven:3.9.11-eclipse-temurin-21 AS build

WORKDIR /build

COPY pom.xml .

# Cache persistente de dependencias Maven
RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline -B

COPY src ./src

# Reutiliza el cache Maven
RUN --mount=type=cache,target=/root/.m2 \
    mvn clean package -DskipTests -B

# ============================================================
# ETAPA 2 - RUNTIME
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
    && chown -R factucore:factucore ${APP_HOME}
	

COPY --from=build /build/target/*.jar ${APP_HOME}/notificacion.jar

RUN chown factucore:factucore ${APP_HOME}/notificacion.jar

USER factucore

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/notificacion.jar"]