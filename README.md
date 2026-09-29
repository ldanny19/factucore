# FactuCore

Motor empresarial de facturación electrónica para Ecuador, diseñado para integrar el ciclo de emisión de comprobantes electrónicos con el **Servicio de Rentas Internas (SRI)**.

FactuCore está orientado a ser un producto genérico, multiempresa, multidocumento, versionable y configurable. El núcleo no conoce la estructura interna de ERP/POS de los clientes: cada cliente transforma sus datos al contrato canónico de FactuCore.

## Estado actual

El proyecto se encuentra en construcción incremental sobre la rama `develop`.

Actualmente ya están definidos y/o implementados componentes de:

- arquitectura modular y hexagonal;
- Facturador;
- Notification;
- librería técnica de mensajería;
- PostgreSQL y Flyway;
- Keycloak/OAuth2/OIDC/JWT;
- Vault para secretos técnicos;
- Kafka como broker actual;
- configuración externa;
- Apache Camel XML DSL para workflow;
- integración base con servicios del SRI;
- firma electrónica;
- generación/validación XML;
- documentación OpenAPI/Swagger UI en Facturador.

Las capacidades funcionales se incorporan progresivamente; el README no considera como terminada una capacidad que todavía esté en desarrollo.

## Arquitectura

La arquitectura inicial evita una distribución innecesaria:

```text
                         ┌─────────────────────┐
                         │      Keycloak       │
                         │ OAuth2 / OIDC / JWT │
                         └──────────┬──────────┘
                                    │
                                    ▼
┌───────────────┐        ┌─────────────────────┐
│ ERP / POS     │───────▶│     Facturador      │
│ del cliente   │ JSON   │ Modular / Hexagonal │
└───────────────┘        └──────┬──────┬───────┘
                                │      │
                         ┌──────▼──┐ ┌─▼────────────┐
                         │PostgreSQL│ │    Vault     │
                         └─────────┘ └──────────────┘
                                │
                                ▼
                         ┌──────────────┐
                         │    Kafka     │
                         └──────┬───────┘
                                │
                                ▼
                         ┌─────────────────────┐
                         │    Notification     │
                         │ correo / plantillas │
                         └─────────────────────┘
```

**Facturador** y **Notification** son los microservicios iniciales. PostgreSQL, Keycloak, Vault y Kafka son infraestructura.

Dentro de Facturador, XML, XSD, validación, firma, SRI, autorización, RIDE/PDF, workflow, auditoría y evidencias permanecen en el mismo límite modular mientras no exista una razón arquitectónica real para separarlos.

## Principios de diseño

- Java 21 y Spring Boot 3.5.x.
- Arquitectura Hexagonal.
- DDD pragmático.
- SOLID, alta cohesión y bajo acoplamiento.
- Organización por dominio/feature/capacidad.
- Inversión de dependencias mediante puertos e interfaces.
- No se crean adapters específicos para SAP, Odoo, Dynamics u otros ERP.
- No se crean microservicios por cada capacidad interna.
- No se introducen Kafka, RabbitMQ, Redis, Elasticsearch, Kubernetes, CQRS, Event Sourcing u otras tecnologías distribuidas sin una necesidad real.
- Las comunicaciones internas del monolito modular no utilizan REST.
- Las configuraciones operativas deben permanecer externalizadas cuando corresponda.

## Contrato canónico

FactuCore utiliza el concepto de un contrato canónico de entrada y salida, independiente del ERP y extensible a múltiples tipos de comprobantes.

El contrato no se diseña como un espejo del XSD:

```text
JSON canónico
      │
      ▼
Validación
      │
      ▼
DocumentDefinition
      │
      ▼
Mapping
      │
      ▼
XML
      │
      ▼
XSD
```

El modelo conceptual precede al diseño definitivo de los campos del API. El contrato contempla como preocupaciones arquitectónicas versionamiento, extensibilidad, idempotencia, errores, estados, workflow y snapshot histórico.

## DocumentDefinition y XSD

El motor electrónico utiliza:

- `DocumentDefinition`;
- `DocumentDefinitionVersion`;
- definición del tipo y versión del documento;
- versión del XSD;
- elementos, atributos, tipos y restricciones;
- cardinalidad;
- mappings;
- reglas de negocio;
- JSON Schema y ejemplos cuando corresponda;
- vigencia y estado.

El XSD no se almacena dentro de cada comprobante. El documento conserva la referencia/metadata de la definición utilizada y el motor puede resolverla mediante un `DefinitionProvider` y cache cuando corresponda.

## Snapshot histórico

Un comprobante emitido conserva los valores necesarios para reconstruir históricamente la información utilizada durante la emisión, incluyendo emisor, receptor, establecimiento, punto de emisión, secuencial, detalles, impuestos, pagos, información adicional y demás datos necesarios para XML/RIDE.

Los identificadores de las entidades maestras pueden mantenerse para trazabilidad, pero la reconstrucción histórica no depende de los valores actuales de dichas entidades.

## Flujo de emisión

```text
JSON
  │
  ▼
Validación
  │
  ▼
DocumentDefinition
  │
  ▼
Mapping
  │
  ▼
XML
  │
  ▼
XSD
  │
  ▼
Firma electrónica
  │
  ▼
SRI
  │
  ▼
Autorización
  │
  ▼
RIDE / PDF
  │
  ▼
Notification
```

El workflow está diseñado para ser persistente, trazable, reanudable e idempotente, permitiendo reintentos y continuación desde el último estado válido.

## Facturador

**Versión actual: 1.0.0**

Facturador es el núcleo de emisión electrónica y concentra las capacidades relacionadas con:

- empresas;
- establecimientos;
- puntos de emisión;
- secuenciales;
- configuración;
- catálogos;
- comprobantes;
- DocumentDefinition;
- XML;
- XSD;
- validación;
- firma electrónica;
- integración SRI;
- autorización;
- RIDE/PDF;
- workflow;
- auditoría;
- evidencias.

### OpenAPI

Facturador incorpora **SpringDoc OpenAPI 2.9.1**, compatible con la línea Spring Boot 3.5.x utilizada actualmente. La dependencia genera la especificación OpenAPI y Swagger UI automáticamente para los endpoints REST existentes y futuros. urlSpringDoc OpenAPIhttps://springdoc.org/

Con el contexto actual de la aplicación:

- Swagger UI: `/factucore-api/swagger-ui.html`
- OpenAPI JSON: `/factucore-api/v3/api-docs`
- OpenAPI YAML: `/factucore-api/v3/api-docs.yaml`

La documentación se irá completando mediante anotaciones OpenAPI en los controllers y contratos conforme se implementen los casos de uso.

## Notification

**Versión actual: 1.0.0**

Microservicio responsable de:

- consumo de eventos de notificación;
- envío de correo;
- plantillas;
- adjuntos;
- configuración SMTP;
- reintentos mediante la infraestructura de mensajería;
- notificación de comprobantes autorizados.

La configuración operativa se mantiene externamente. Las credenciales sensibles, como la contraseña SMTP, son entregadas desde Vault mediante configtree.

## FactuCore Messaging

**Versión actual: 1.0.0**

Librería técnica compartida para mensajería. Su responsabilidad es infraestructura y no lógica de negocio.

Incluye:

- API genérica de publicación y consumo;
- selección de broker por configuración;
- Kafka;
- RabbitMQ;
- serialización del contrato de evento;
- consumidores;
- reintentos;
- DLQ/DLT cuando corresponda;
- auto-configuración Spring Boot;
- health checks dinámicos del broker seleccionado.

El broker configurado actualmente en el entorno es **Kafka**.

## Seguridad

La autenticación y autorización se delegan a Keycloak mediante:

- OAuth2;
- OpenID Connect;
- JWT;
- Spring Security;
- Resource Server.

FactuCore no implementa autenticación propia ni almacena contraseñas de usuarios.

## Secretos

Vault centraliza secretos técnicos y funcionales sensibles.

Entre ellos:

- credenciales de PostgreSQL;
- credencial administrativa de PostgreSQL;
- credencial administrativa de Keycloak;
- contraseña SMTP;
- contraseña del certificado de firma electrónica.

El archivo del certificado de firma permanece fuera de Vault; Vault administra su contraseña.

Los secretos no deben almacenarse en `.env`, código fuente, SQL, YAML versionado ni Dockerfiles.

## Persistencia

PostgreSQL es la infraestructura de persistencia.

Se utilizan:

- JPA/Hibernate;
- Flyway;
- `ddl-auto=validate`;
- migraciones versionadas;
- nombres de tablas y columnas en español.

Los estados de registro y los estados de workflow se mantienen conceptualmente separados:

- `ESTADO_REGISTRO` para vigencia del registro;
- `ESTADO_PROCESO` exclusivamente para workflow/proceso.

La auditoría utiliza:

```text
ESTADO_REGISTRO
USUARIO_CREACION
USUARIO_MODIFICACION
FECHA_CREACION
FECHA_MODIFICACION
OBSERVACION
```

## Firma electrónica

Facturador incorpora la base técnica para firma electrónica utilizando DSS.

La arquitectura contempla certificados PKCS#12 y perfiles de firma compatibles con los requerimientos aplicables del SRI. La implementación concreta se valida contra los XSD, especificaciones técnicas y servicios del SRI correspondientes a cada versión.

## Integración SRI

La integración con SRI se mantiene aislada como un adaptador externo.

El flujo contempla:

1. generación del comprobante;
2. validación;
3. firma;
4. envío al servicio de recepción;
5. consulta/gestión de recepción;
6. consulta de autorización;
7. persistencia de respuestas y evidencias;
8. actualización del workflow.

Las URLs de los ambientes de pruebas y producción se administran mediante configuración externa.

## Workflow

Apache Camel se utiliza como motor de workflow en Facturador.

Las rutas se externalizan mediante XML, por ejemplo:

```text
/app/config/camel/facturacion.xml
```

Esto permite evolucionar el flujo sin convertir toda la orquestación en código Java.

## Infraestructura local

La composición actual incluye:

- Vault;
- Vault Agent;
- PostgreSQL;
- Keycloak;
- Kafka;
- Facturador;
- Notification.

La configuración sensible se entrega mediante volúmenes/configtree y Vault Agent.

## Configuración externa

Los archivos de configuración operativa no dependen exclusivamente del repositorio.

En el entorno local se utilizan rutas externas bajo:

```text
C:\factucore\
├── certificados
├── documentos
├── facturador\config
├── notificacion\config
├── vault
├── postgres
├── logs
└── secrets
```

Esto permite separar configuración, secretos, certificados y datos persistentes del código fuente.

## Estructura relevante del repositorio

```text
factucore/
├── facturador/
├── notificacion/
├── factucore-messaging/
├── configuraciones/
│   ├── facturador/
│   └── notificacion/
├── compose/
│   ├── postgres/
│   └── vault/
├── docs/
└── README.md
```

## Versiones de módulos

| Módulo | Versión |
|---|---|
| Facturador | 1.0.0 |
| Notification | 1.0.0 |
| FactuCore Messaging | 1.0.0 |

Las versiones de los módulos son independientes y se incrementarán cuando cambie funcionalmente cada módulo.

## Documentación por módulo

Cada módulo mantiene su propio CHANGELOG:

- `facturador/CHANGELOG.md`
- `notificacion/CHANGELOG.md`
- `factucore-messaging/CHANGELOG.md`

Esto evita mezclar la evolución funcional de módulos con ciclos de liberación diferentes.

## Desarrollo

La rama de trabajo principal para esta etapa es `develop`.

Tecnologías base actuales:

- Java 21;
- Spring Boot 3.5.16;
- Maven;
- PostgreSQL 17;
- Hibernate/JPA;
- Flyway;
- Apache Camel 4.10.7;
- Spring Security;
- Keycloak 26.7.4;
- Apache Kafka 4.0.2;
- HashiCorp Vault 1.20;
- SpringDoc OpenAPI 2.9.1 en Facturador;
- EU DSS 6.5;
- Apache PDFBox 3.0.5.

## Principios para nuevas funcionalidades

Antes de agregar una funcionalidad se debe validar:

1. modelo conceptual;
2. bounded context y límite del módulo;
3. reglas de negocio;
4. versionamiento;
5. idempotencia;
6. errores y mensajes;
7. workflow;
8. snapshot histórico;
9. persistencia;
10. contrato API;
11. DocumentDefinition/XSD;
12. pruebas.

Las abstracciones y tecnologías nuevas deben introducirse solamente cuando exista una necesidad real.
