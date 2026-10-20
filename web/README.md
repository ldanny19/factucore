# Frontend FactuCore — prueba inicial

## Requisitos
- Node.js compatible con Vite 7 (Node.js 20.19+ o 22.12+).
- npm.

## Ejecutar
Desde esta carpeta: `npm install` y luego `npm run dev`. Abre http://localhost:3000.

## Configuración pública
La aplicación carga `configuraciones/web/config/config.js` antes de iniciar React. Nunca pongas contraseñas, client secrets, tokens o claves privadas en ese archivo.

Los valores OIDC son de ejemplo y deben verificarse contra Keycloak. Login y Empresas son prototipos: todavía no hay autenticación real ni integración con la API.
