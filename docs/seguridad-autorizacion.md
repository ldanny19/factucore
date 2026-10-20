# Autorización de la API FactuCore

Facturador valida los JWT emitidos por Keycloak y aplica autorización centralizada a todas las rutas REST bajo `/api/v1/**`. Los roles se administran en Keycloak como roles de realm y el backend consume únicamente los incluidos en `realm_access.roles`.

## Roles y asignaciones

| Rol de realm | Asignación | Permiso |
| --- | --- | --- |
| `GENERATE_COMPROBANTE` | Cuenta de servicio del cliente `api-sifa-integrador` | Crear comprobantes mediante `POST /api/v1/comprobantes`. No permite reprocesar ni administrar. |
| `REPROCESS_COMPROBANTE` | Rol compuesto asociado a `SUPERADMIN`, `ADMIN` y `VENDEDOR` | Reprocesar comprobantes existentes cuyo flujo no terminó correctamente. |
| `SUPERADMIN` | Usuarios web | Acceso administrativo a los endpoints protegidos, excepto las operaciones exclusivas de generación y reprocesamiento. |
| `ADMIN` | Usuarios web | Acceso administrativo a los endpoints protegidos, excepto las operaciones exclusivas de generación y reprocesamiento. |
| `VENDEDOR` | Usuarios web | No administra recursos ni crea comprobantes por la API; puede reprocesar si el token incluye `REPROCESS_COMPROBANTE`. |

## Matriz de autorización aplicada

- `POST /api/v1/comprobantes`: exclusivamente `GENERATE_COMPROBANTE`.
- `POST /api/v1/comprobantes/{comprobanteId}/reprocesar`: exclusivamente `REPROCESS_COMPROBANTE`.
- Cualquier otro método y endpoint bajo `/api/v1/**`: `SUPERADMIN` o `ADMIN`.
- Salud y documentación OpenAPI enumeradas en `SeguridadConfig`: públicas.
- `OPTIONS`: permitido para preflight; la respuesta CORS sigue limitada a los orígenes configurados.

Estas reglas centrales cubren todos los controladores actuales bajo `/api/v1/**`; un controlador nuevo bajo ese prefijo queda protegido por defecto.

## CORS

Los orígenes permitidos se externalizan mediante `FACTUCORE_CORS_ALLOWED_ORIGINS`, con valores separados por coma. El valor predeterminado permite el frontend local y el frontend publicado en `api.facturador.com:3000`. En otros entornos, definir explícitamente la variable con los orígenes que correspondan.

## Consideraciones operativas

1. Los cinco roles deben ser roles de realm y aparecer en `realm_access.roles` del token cuando corresponda.
2. Asignar `GENERATE_COMPROBANTE` a la cuenta de servicio de `api-sifa-integrador`.
3. Asociar `REPROCESS_COMPROBANTE` como rol compuesto a `SUPERADMIN`, `ADMIN` y `VENDEDOR`.
4. Obtener un JWT nuevo después de cambiar las asignaciones de roles.
5. Un token ausente o inválido produce `401`; un token válido sin el rol requerido produce `403`.
