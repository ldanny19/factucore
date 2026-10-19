# Proxy inverso y entrada HTTP de FactuCore

## Responsabilidad

Nginx actúa como proxy inverso de entrada para las API REST que se publican externamente. No es un microservicio de negocio y no sustituye a Keycloak ni a la validación JWT que realiza Facturador.

## Ruta inicial

| Entrada pública | Destino |
|---|---|
| `/api/facturador/**` | `http://facturador:8080/factucore-api/**` |

Por ejemplo, `/api/facturador/v3/api-docs` se reenvía a `/factucore-api/v3/api-docs`.

Notification no se publica por este proxy en esta etapa: su flujo actual consume eventos de mensajería y no se agrega una ruta REST sin un contrato real.

## Trazabilidad HTTP

Nginx registra en el access log el identificador de petición `$request_id`, la ruta, el código HTTP, el tiempo total y el tiempo de respuesta del upstream. Propaga el identificador técnico mediante `X-Request-ID`.

Este identificador representa la petición HTTP en el proxy; no reemplaza la clave de idempotencia ni el identificador de negocio/transacción que gestiona Facturador. Facturador debe conservar su propia trazabilidad durante el workflow.

Los logs de acceso y error se envían a los flujos estándar del contenedor para que Docker pueda recogerlos.

## Seguridad

- Keycloak sigue siendo el proveedor OIDC y emisor de tokens.
- Nginx reenvía el encabezado `Authorization`; no valida JWT por sí solo con esta configuración.
- Facturador continúa validando el JWT con su configuración OAuth2 Resource Server.
- No se habilita CORS globalmente sin conocer los orígenes autorizados del frontend.
- El endpoint `/nginx-health` es únicamente una comprobación técnica de salud del proxy.

## Configuración

La configuración versionada está en `configuraciones/nginx/config/default.conf`. El instalador la copia a `FACTUCORE_HOME/nginx/config/default.conf` solo si el archivo no existe, sin sobrescribir la configuración local.

El puerto HTTP publicado se declara directamente en `compose/docker-compose.yml` como `8080:80`.

## Verificación manual

1. Ejecutar el instalador habitual para copiar la configuración de Nginx.
2. Validar Compose: `docker compose --env-file compose/.env -f compose/docker-compose.yml config --quiet`.
3. Levantar Nginx con el procedimiento habitual.
4. Consultar `http://localhost:8080/nginx-health`.
5. Enviar una petición a `http://localhost:8080/api/facturador/**` con el encabezado `Authorization: Bearer <token>`.
6. Revisar `docker compose logs nginx` y verificar el identificador de petición, el código HTTP y los tiempos.

Estas verificaciones deben ejecutarse en el entorno local. El cambio de código no implica que los contenedores hayan sido ejecutados o probados.
