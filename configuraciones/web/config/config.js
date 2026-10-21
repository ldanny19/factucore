window.FACTUCORE_CONFIG = {
  API_BASE_URL: "http://api.facturador.com:8080/facturador-api",
  API_VERSION: "v1",
  API_PATH_PREFIX: "/api",
  API_EMPRESAS_PATH: "/empresas",
  OIDC_AUTHORITY: "http://api.facturador.com:8080/auth/realms/facturador",
  OIDC_CLIENT_ID: "facturador-app",
  OIDC_REDIRECT_URI: "http://localhost:3000/auth/callback",
  OIDC_POST_LOGOUT_REDIRECT_URI: "http://localhost:3000/login",
  SESSION_INACTIVITY_TIMEOUT_MINUTES: 15,
  SESSION_TOKEN_WARNING_MINUTES: 2,
  SESSION_PROMPT_TIMEOUT_SECONDS: 60,
  MESSAGES: {
    "config.missing": "No se cargó la configuración externa de FactuCore.",
    "config.invalid": "La configuración externa contiene propiedades inválidas.",
    "api.operationFailed": "La operación no fue exitosa.",
    "common.operationFailed": "No fue posible completar la operación.",
    "empresa.ruc.invalid": "El RUC debe contener exactamente 13 dígitos.",
    "empresa.requiredFields": "Completa la razón social y la dirección de la matriz.",
    "empresa.id.invalid": "Ingresa un ID de empresa válido.",
    "empresa.created": "La empresa se creó correctamente.",
    "empresa.updated": "La empresa se actualizó correctamente.",
    "empresa.found": "Empresa {id} encontrada.",
    "empresa.deleted": "La empresa {name} fue eliminada lógicamente.",
    "empresa.empty": "No hay empresas registradas.",
    "empresa.listDescription": "{count} registro(s). Se incluyen empresas activas e inactivas, no las eliminadas."
  }
};
