# FactuCore

**FactuCore** es un motor empresarial de facturación electrónica para Ecuador. Su propósito es centralizar la emisión de comprobantes electrónicos y la integración con el Servicio de Rentas Internas (SRI) mediante contratos API canónicos, configuración externa y límites de responsabilidad explícitos.

> **Estado:** versión 1.0.0. Este README describe la solución y el despliegue del repositorio en la rama `develop`. Las capacidades concretas deben contrastarse con el código y la configuración de cada módulo; la presencia de una tecnología en la arquitectura no implica que todas las funcionalidades de negocio estén completas.

## Contenido

- [Objetivo y alcance](#objetivo-y-alcance)
- [Arquitectura](#arquitectura)
- [Flujo de emisión](#flujo-de-emisión)
- [Principios de diseño](#principios-de-diseño)
- [Tecnologías](#tecnologías)
- [Estructura del repositorio](#estructura-del-repositorio)
- [Requisitos](#requisitos)
- [Instalación y ejecución](#instalación-y-ejecución)
- [Configuración externa](#configuración-externa)
- [API y autenticación](#api-y-autenticación)
- [Persistencia y seguridad](#persistencia-y-seguridad)
- [Desarrollo y validación](#desarrollo-y-validación)
- [Versionado y documentación](#versionado-y-documentación)
- [Documentación relacionada](#documentación-relacionada)

## Objetivo y alcance

FactuCore evoluciona el facturador existente hacia un motor más genérico, configurable y reutilizable, sin acoplar el núcleo a la estructura de un ERP o POS particular.

El producto contempla capacidades para administrar empresas y sus datos de emisión, procesar documentos electrónicos, generar y validar XML, firmar comprobantes, comunicarse con el SRI, gestionar autorizaciones, producir RIDE/PDF, mantener evidencias y coordinar notificaciones.

La disponibilidad de cada operación debe verificarse en los endpoints, casos de uso y pruebas de la versión desplegada. Las decisiones futuras no deben confundirse con funcionalidades ya disponibles.

## Arquitectura

FactuCore mantiene separados los servicios de negocio y la infraestructura. Los microservicios iniciales son **Facturador** y **Notification**. PostgreSQL, Keycloak, Vault, Kafka y Nginx son componentes de infraestructura, no microservicios de negocio.

```text
Cliente / ERP / POS
       |
       | Contrato JSON canónico
       v
Nginx (API Gateway)
       |--------------------------> Keycloak (OIDC / OAuth2 / JWT)
       |
       +--------------------------> Facturador
       |                              |-- PostgreSQL
       |                              |-- Vault / secretos
       |                              |-- XML, XSD y firma
       |                              |-- Integración SRI
       |                              |-- Workflow y evidencias
       |
       +--------------------------> Notification
                                      |-- PostgreSQL
                                      |-- Kafka
                                      |-- SMTP / plantillas / adjuntos
```

Facturador conserva dentro de su límite las capacidades documentales y del SRI: definición documental, mapping, XML/XSD, validación, firma electrónica, recepción y autorización, RIDE/PDF, workflow, auditoría y evidencias. No se separan en microservicios independientes sin una necesidad arquitectónica demostrable.

### Contrato canónico

El cliente adapta sus propios datos al contrato de FactuCore. El motor no interpreta modelos internos de SAP, Odoo, Dynamics ni de otros ERP/POS.

```text
ERP/POS del cliente
       |
Transformación del cliente
       |
JSON canónico de FactuCore
       |
API REST
       |
Validación -> Definición documental -> Mapping -> XML -> XSD
```

El contrato canónico no debe ser un espejo 1:1 del XSD. Su evolución debe considerar versionamiento, extensibilidad, idempotencia, errores, estados, workflow y trazabilidad.

### Definiciones documentales y XSD

El diseño contempla `DocumentDefinition` y `DocumentDefinitionVersion` para identificar el tipo y la versión documental, su XSD, restricciones, cardinalidad, mappings y reglas aplicables. El XSD no se copia dentro de cada comprobante: el documento conserva la referencia a la definición utilizada y el motor resuelve la versión correspondiente.

### Evidencias e historia

Las evidencias del comprobante pueden incluir XML generado, firmado o autorizado, respuestas del SRI y RIDE/PDF según corresponda. Deben conservarse asociadas al comprobante y permitir trazabilidad de la representación emitida. La reconstrucción histórica no debe depender de que los datos actuales de las entidades maestras sigan siendo iguales a los utilizados durante la emisión.

## Flujo de emisión

El flujo conceptual es:

```text
JSON canónico
  -> Validación
  -> DocumentDefinition
  -> Mapping
  -> XML
  -> Validación XSD
  -> Firma electrónica
  -> Recepción SRI
  -> Autorización
  -> RIDE / PDF
  -> Notification
```

El workflow debe ser trazable y persistente, admitir reintentos y permitir continuar desde el último estado válido sin repetir innecesariamente etapas completadas.

## Principios de diseño

- Java 21 y Spring Boot 3.5.x en el backend de FactuCore.
- Arquitectura hexagonal y DDD pragmático.
- Organización por dominio, capacidad o funcionalidad.
- SOLID, alta cohesión, bajo acoplamiento e inversión de dependencias.
- Contratos explícitos entre módulos; no utilizar REST para comunicación interna dentro de un mismo módulo de negocio.
- PostgreSQL como infraestructura de persistencia; no como microservicio.
- Flyway como mecanismo de evolución del esquema; no usar `ddl-auto=update` en producción.
- No crear adapters específicos de ERP/POS.
- No introducir microservicios, patrones ni infraestructura distribuida sin una necesidad concreta.
- No hardcodear mensajes visibles: resolverlos mediante los catálogos de mensajes configurados.
- Mantener secretos, certificados, datos persistentes y configuración operativa fuera del código versionado.
- Configurar lo que cambia; codificar el comportamiento real del dominio.

## Tecnologías

Las versiones de referencia declaradas para el entorno del proyecto incluyen:

| Componente | Tecnología / versión de referencia |
|---|---|
| Backend | Java 21, Spring Boot 3.5.x, Maven |
| Persistencia | PostgreSQL 17, JPA/Hibernate, Flyway |
| Seguridad | Spring Security, Keycloak 26.7.4, OAuth2/OIDC/JWT |
| Workflow | Apache Camel 4.10.7 |
| Mensajería | Kafka 4.0.2 mediante FactuCore Messaging |
| Secretos | HashiCorp Vault 1.20 y Vault Agent |
| API | SpringDoc OpenAPI 2.9.1 en Facturador |
| Firma electrónica | EU DSS 6.5 |
| PDF | Apache PDFBox 3.0.5 |
| Entrada HTTP | Nginx como API Gateway |
| Frontend | React, TypeScript, Vite y Material UI |

Estas versiones son referencias de la configuración documentada y deben confirmarse contra los POM y archivos de despliegue de la revisión que se vaya a instalar.

## Estructura del repositorio

La organización principal de la rama `develop` incluye:

```text
factucore/
├── auditoria/                   # Capacidades de auditoría
├── facturador/                  # Motor de facturación electrónica
├── notificacion/                # Servicio de notificaciones
├── factucore-messaging/         # Infraestructura compartida de mensajería
├── web/                         # Frontend React y TypeScript
├── configuraciones/
│   ├── facturador/              # Configuración, workflow, Jasper y XSD
│   ├── messaging/               # Catálogos de mensajes de mensajería
│   ├── nginx/                   # Configuración del gateway
│   ├── notificacion/            # Configuración y plantillas
│   └── web/                     # Configuración pública de ejecución web
├── compose/                     # Docker Compose y archivos de construcción
├── infraestructura/             # Recursos de infraestructura y Vault
├── instalacion/                 # Guía de instalación y operación
├── docs/                        # Documentación técnica
├── instalar-factucore.sh        # Instalador del entorno
├── AGENTS.md                    # Instrucciones para agentes de desarrollo
└── README.md
```

Consulta el árbol real de la rama antes de asumir que un directorio o archivo existe en otra revisión.

## Requisitos

Para el despliegue de referencia se necesita:

- Git Bash para ejecutar el instalador y los comandos documentados.
- Docker Engine o Docker Desktop.
- Docker Compose v2.
- Acceso al repositorio y a las imágenes de los servicios.
- Espacio de disco y permisos para la carpeta externa definida por `FACTUCORE_HOME`.

Para compilar los módulos Java se requiere JDK 21 y Maven, ya sea instalado localmente o disponible en el entorno de compilación. Para desarrollar el frontend se necesita Node.js/npm compatibles con la versión declarada en `web/package.json`.

## Instalación y ejecución

La guía detallada se encuentra en [instalacion/README.md](instalacion/README.md). Los pasos siguientes son un resumen; sigue la guía completa para preparar secretos y revisar instalaciones existentes.

### 1. Preparar configuración

Desde la raíz del repositorio, crea `compose/.env` a partir de `compose/.env.example` si aún no existe y configura las variables requeridas para el entorno. No copies secretos reales a archivos versionados.

Verifica la rama y la ubicación:

```bash
git branch --show-current
pwd
```

### 2. Ejecutar el instalador

```bash
bash ./instalar-factucore.sh
```

El instalador prepara las carpetas externas y copia configuraciones de referencia cuando no existen. Revisa sus mensajes y detente ante avisos sobre datos heredados, Vault existente o secretos faltantes; no elimines datos ni reinicialices servicios para intentar resolverlos sin investigar primero.

### 3. Validar Docker Compose

```bash
docker compose --env-file compose/.env -f compose/docker-compose.yml config --quiet
```

### 4. Iniciar servicios

```bash
docker compose --env-file compose/.env -f compose/docker-compose.yml up -d
docker compose --env-file compose/.env -f compose/docker-compose.yml ps
```

Para revisar logs:

```bash
docker compose --env-file compose/.env -f compose/docker-compose.yml logs --tail=100
```

Para detener los servicios sin eliminar los datos persistentes:

```bash
docker compose --env-file compose/.env -f compose/docker-compose.yml down
```

No utilices `down -v` como operación rutinaria. Antes de eliminar volúmenes o datos persistentes, confirma el alcance y la existencia de un respaldo válido.

## Configuración externa

La variable `FACTUCORE_HOME`, definida en `compose/.env`, determina la raíz externa donde se mantienen los datos persistentes y la configuración operativa. Las rutas de los volúmenes deben resolverse a partir de esta variable.

La estructura externa se organiza por responsabilidad e incluye, según los servicios habilitados:

- `vault/`: configuración, datos, políticas y secretos operativos de Vault.
- `base_datos/postgres/`: datos e inicialización de PostgreSQL.
- `mensajeria/`: datos y configuración de mensajería.
- `facturador/`: configuración, documentos generados, certificados, reportes, imágenes y logs.
- `notificacion/`: configuración y logs.
- `nginx/`: configuración efectiva del gateway.
- `web/config/`: configuración pública de ejecución del frontend.

Los nombres y rutas efectivos deben revisarse en `compose/docker-compose.yml` y en la guía de instalación.

### Seguridad de la configuración

- No guardar contraseñas, tokens, claves privadas ni contraseñas de certificados en el repositorio.
- No incluir secretos en `.env`, YAML versionado, SQL, Dockerfiles ni `config.js` público.
- El archivo del certificado de firma debe mantenerse fuera del repositorio; Vault puede administrar su contraseña.
- Proteger las carpetas que contienen secretos renderizados por Vault Agent.
- No versionar datos persistentes, documentos generados ni logs operativos.

## API y autenticación

Facturador publica documentación OpenAPI mediante SpringDoc. Con el contexto de aplicación `/facturador-api`, las rutas configuradas de referencia son:

- Swagger UI: `/facturador-api/swagger-ui.html`
- OpenAPI JSON: `/facturador-api/v3/api-docs`
- OpenAPI YAML: `/facturador-api/v3/api-docs.yaml`

En el despliegue de referencia, Nginx expone la entrada HTTP pública en el puerto `8080` y enruta las peticiones a Facturador y Notification. La configuración pública de Keycloak utiliza el prefijo `/auth`; internamente Keycloak escucha en la raíz y Nginx gestiona ese prefijo.

La autenticación y validación de tokens se delegan a Keycloak y Spring Security mediante OAuth2, OIDC y JWT. No se implementa un sistema de autenticación propio. Los endpoints y permisos disponibles deben verificarse en la documentación OpenAPI y la configuración desplegada.

## Persistencia y seguridad

- PostgreSQL es la infraestructura de datos.
- JPA/Hibernate gestiona la persistencia y Flyway controla las migraciones.
- Los nombres de tablas y columnas de negocio siguen las convenciones en español.
- `ESTADO_REGISTRO` representa la vigencia del registro; `ESTADO_PROCESO` se reserva para procesos y workflow.
- Los campos de auditoría estándar son `ESTADO_REGISTRO`, `USUARIO_CREACION`, `USUARIO_MODIFICACION`, `FECHA_CREACION`, `FECHA_MODIFICACION` y `OBSERVACION`.
- Los secuenciales deben ser seguros frente a concurrencia y manejarse transaccionalmente.
- Los mensajes visibles se resuelven mediante catálogos de mensajes, no mediante textos hardcodeados en el código.

## Desarrollo y validación

Antes de modificar el código:

1. Comprende el requerimiento y revisa la implementación existente.
2. Identifica módulo, dominio, caso de uso, contratos y dependencias afectadas.
3. Evalúa impacto sobre persistencia, Flyway, seguridad, workflow, SRI, evidencias y frontend.
4. Mantén compatibilidad con V1 salvo que un cambio aprobado requiera explícitamente otra cosa.
5. Implementa pruebas apropiadas y ejecuta las comprobaciones disponibles.
6. Informa qué comandos se ejecutaron y cuáles fueron sus resultados; no afirmes que una compilación o prueba pasó si no se ejecutó.

Para validar Compose:

```bash
docker compose --env-file compose/.env -f compose/docker-compose.yml config --quiet
```

Los comandos Maven y npm concretos deben tomarse de los POM y de `web/package.json` de la revisión actual. No se asume que las herramientas estén instaladas ni que el build haya sido validado sin ejecutarlo.

## Versionado y documentación

La versión funcional documentada de los componentes principales es:

| Componente | Versión |
|---|---|
| Facturador | 1.0.0 |
| Notification | 1.0.0 |
| FactuCore Messaging | 1.0.0 |

Cada módulo mantiene su historial en el CHANGELOG correspondiente, cuando existe:

- [Facturador CHANGELOG](facturador/CHANGELOG.md)
- [Notification CHANGELOG](notificacion/CHANGELOG.md)
- [FactuCore Messaging CHANGELOG](factucore-messaging/CHANGELOG.md)

Las versiones de módulos pueden evolucionar independientemente cuando sus ciclos de liberación lo requieran. Los cambios de contrato o arquitectura deben documentar su impacto y mantener trazabilidad de las decisiones.

## Documentación relacionada

- [Guía de instalación y operación](instalacion/README.md)
- [Instrucciones del agente de desarrollo](AGENTS.md)
- [Configuración de Docker Compose](compose/docker-compose.yml)
- [Configuración del gateway](configuraciones/nginx/config/default.conf)
- [Configuración de ejecución del frontend](configuraciones/web/config/config.js)

---

**Principio rector:** FactuCore debe ser genérico, configurable y extensible para múltiples empresas y documentos, sin introducir abstracciones o complejidad distribuida que no respondan a una necesidad real.
