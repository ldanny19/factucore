# Keycloak en FactuCore

## Responsabilidad

Keycloak proporciona identidad, autenticación y autorización mediante OAuth2/OIDC. No es un microservicio de negocio de FactuCore.

## Aprovisionamiento del realm de aplicación

El instalador `instalar-factucore.sh` solicita el nombre del realm y la contraseña permanente de `superApp`, inicia Keycloak y sus dependencias y llama a `infraestructura/keycloak/provisionar-realm.sh`.

El script crea solo los recursos faltantes: realm, rol `SUPERADMIN` y usuario `superApp`; también verifica la asignación del rol. El rol `SUPERADMIN` incluye permisos administrativos `query-users`, `view-users`, `manage-users`, `view-realm` y `manage-realm` del cliente `realm-management`, asignados exclusivamente en el realm de aplicación aprovisionado. Esto permite administrar usuarios, sus asignaciones y roles del realm FactuCore; `manage-realm` también permite modificar otras configuraciones de ese realm. No concede permisos sobre `master` ni administración global del servidor. Si el usuario ya existe, conserva su contraseña. La contraseña no se almacena en archivos ni se imprime en logs. La contraseña administrativa técnica del usuario `admin` se obtiene del secreto `keycloak_admin_password` montado desde Vault.

La contraseña de `superApp` es permanente. `SUPERADMIN` representa los privilegios funcionales de FactuCore y concede administración solo en el realm de aplicación aprovisionado.

## Despliegues

Docker Compose usa este script común. Kubernetes debe invocar el mismo proceso lógico desde un `Job`, proporcionando las credenciales técnicas mediante un Secret de Kubernetes.

No almacenar contraseñas, tokens ni exportaciones con credenciales en el repositorio.
