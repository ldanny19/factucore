window.FACTUCORE_CONFIG = {
  API_BASE_URL: "http://api.facturador.com:8080/facturador-api",
  OIDC_AUTHORITY: "http://api.facturador.com:8080/auth/realms/facturador",
  OIDC_CLIENT_ID: "facturador-app",
  OIDC_REDIRECT_URI: "http://localhost:3000/auth/callback",
  OIDC_POST_LOGOUT_REDIRECT_URI: "http://localhost:3000/login",
  SESSION_INACTIVITY_TIMEOUT_MINUTES: 15,
  SESSION_TOKEN_WARNING_MINUTES: 2,
  SESSION_PROMPT_TIMEOUT_SECONDS: 60,
};
