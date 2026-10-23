# Frontend FactuCore

## Requisitos

- Node.js compatible con Vite 7 (Node.js 20.19+ o 22.12+).
- npm.

## Ejecutar

Desde esta carpeta: `npm install` y luego `npm run dev`. Abre http://localhost:3000.

## Configuración pública de ejecución

La aplicación carga `configuraciones/web/config/config.js` antes de iniciar React. Los valores OIDC deben corresponder al realm y cliente público de Keycloak:

- `OIDC_AUTHORITY`: URL del realm, por ejemplo `https://keycloak.example/realms/facturador`.
- `OIDC_CLIENT_ID`: cliente público configurado para el frontend.
- `OIDC_REDIRECT_URI`: debe estar registrado como URI de redirección válida del cliente; la aplicación usa `/auth/callback`.
- `OIDC_POST_LOGOUT_REDIRECT_URI`: debe estar registrado como URI de salida válida.

El inicio de sesión conserva el formulario propio de FactuCore y envía las credenciales al proveedor de identidad configurado para validarlas; FactuCore no implementa autenticación ni almacena contraseñas. El usuario permanece en la interfaz de FactuCore. No incluyas client secrets, tokens ni claves privadas en `config.js` ni en el bundle.

## Administración de accesos

Las páginas de usuarios, roles, permisos y asignación de roles consumen la Admin REST API del realm `facturador` directamente desde el navegador, con el access token de la sesión. La aplicación valida que la autoridad OIDC corresponda a ese realm antes de enviar operaciones administrativas. No se permite seleccionar ni operar sobre otros realms desde este módulo.

Para que `SUPERADMIN` pueda administrar accesos, un administrador existente del realm debe configurar las autorizaciones administrativas del rol de FactuCore para las operaciones necesarias: consultar y administrar usuarios, administrar roles y gestionar asignaciones. Cuando la versión instalada lo permita, se recomienda usar permisos administrativos de alcance limitado a esas operaciones. No conceder permisos globales de servidor ni acceso al realm `master`; los roles administrativos amplios del realm deben evitarse salvo que se haya aprobado explícitamente ese alcance.

Después de configurar el rol `SUPERADMIN`, asignarlo al usuario administrador de FactuCore y cerrar/iniciar sesión para que Keycloak emita un token actualizado. El cliente del frontend también debe permitir las solicitudes CORS desde el origen publicado de FactuCore. Si falta alguna autorización o el origen no está permitido, la interfaz presenta un mensaje genérico sin exponer detalles de la integración.

El módulo y sus opciones de administración están restringidos en la interfaz al rol de realm `SUPERADMIN`, recibido en `realm_access.roles`. Los roles de cliente con el mismo nombre no habilitan el módulo. Esta restricción de navegación no sustituye la autorización del servidor de identidad ni las reglas de autorización del backend.

Los roles que FactuCore reconoce actualmente para sus módulos son `SUPERADMIN`, `ADMIN` y `VENDEDOR`. Crear roles adicionales en Keycloak no les concede automáticamente acceso a rutas de FactuCore.

## Configuración de mensajes

Los textos externos de la interfaz se mantienen en `configuraciones/web/config/mensajes.js`. En el entorno local utilizado por el servidor web deben copiarse también a `C:\factucore\web\config\mensajes.js`, conservando esa ubicación de montaje configurada para el proyecto.
