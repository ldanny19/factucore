# API Gateway de FactuCore

## Responsabilidad

Spring Cloud Gateway es el punto de entrada HTTP para las API REST que se expongan al frontend. No sustituye Keycloak, no implementa autenticación propia y no reemplaza las reglas de autorización de Facturador.

## Ruta inicial

| Entrada del Gateway | Destino |
|---|---|
| `/api/facturador/**` | `http://facturador:8080/factucore-api/**` |

El Gateway elimina los dos primeros segmentos (`/api/facturador`) y agrega el contexto `/factucore-api`. Por ejemplo, `/api/facturador/v3/api-docs` se reenvía a `/factucore-api/v3/api-docs`.

Notification no tiene una ruta REST configurada en esta etapa: su flujo documentado actual consume eventos de mensajería. No se inventa un endpoint REST sin un contrato real.

## Seguridad

- Keycloak sigue siendo el proveedor OIDC y emisor de tokens.
- El Gateway conserva el encabezado `Authorization` al reenviar la solicitud.
- Facturador continúa validando el JWT con la configuración OAuth2 Resource Server existente.
- No se cambia el issuer ni el JWK Set URI automáticamente. El valor de `iss` del token debe coincidir exactamente con el issuer configurado en Facturador.
- CORS no se habilita globalmente por defecto. Configurarlo cuando se conozca el origen del frontend y solo en el punto necesario.

## Configuración

La configuración versionada está en `configuraciones/gateway/config/application.yml`. El instalador la copia a `FACTUCORE_HOME/gateway/config` únicamente cuando el archivo externo no existe.

Variables relevantes:

- `GATEWAY_PORT`: puerto publicado en el host (por defecto `8080`).
- `FACTUCORE_GATEWAY_FACTURADOR_URI`: destino interno del servicio Facturador (por defecto `http://facturador:8080`).

## Verificación manual

1. Construir el JAR del Gateway con Maven desde `gateway/`.
2. Preparar la imagen `factucore-gateway` con la versión definida por `FACTUCORE_VERSION`.
3. Validar Compose con `docker compose --env-file compose/.env -f compose/docker-compose.yml config --quiet`.
4. Levantar los servicios con el procedimiento habitual.
5. Consultar `http://localhost:8080/actuator/health`.
6. Abrir Swagger UI directamente en `http://localhost:8081/factucore-api/swagger-ui.html`.
7. Enviar una solicitud protegida por el Gateway con `Authorization: Bearer <token>` a la ruta `/api/facturador/**`. Verificar que sin token se rechaza el acceso y con un token válido se alcanza la validación de negocio de Facturador.

Estas verificaciones deben ejecutarse en el entorno local antes de considerar validado el despliegue. El cambio de código por sí solo no implica que los contenedores hayan sido ejecutados o probados.
