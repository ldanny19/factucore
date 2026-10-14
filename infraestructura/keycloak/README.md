# Keycloak en FactuCore

## Responsabilidad

Keycloak proporciona identidad, autenticación y autorización mediante OAuth2/OIDC. No es un microservicio de negocio de FactuCore.

## Realm de aplicación

El realm utilizado por defecto por las aplicaciones es `facturador`. Su nombre puede configurarse mediante `KEYCLOAK_REALM` en el entorno de despliegue Docker Compose.

El archivo `compose/docker-compose.yml` **no crea ni importa automáticamente** el realm de aplicación ni sus clientes, roles o usuarios de negocio. Estos elementos deben provisionarse mediante el procedimiento de configuración de Keycloak que se defina para el entorno.

## Administrador de Keycloak

La contraseña del administrador inicial se solicita durante la instalación nueva y se almacena en Vault. El administrador de bootstrap sirve para administrar Keycloak; no debe confundirse con un usuario de negocio del realm de aplicación.

No almacenar contraseñas, tokens ni exportaciones con credenciales en el repositorio.
