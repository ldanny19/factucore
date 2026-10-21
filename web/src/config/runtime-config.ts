export interface RuntimeConfig {
  API_BASE_URL: string;
  OIDC_AUTHORITY: string;
  OIDC_CLIENT_ID: string;
  OIDC_REDIRECT_URI: string;
  OIDC_POST_LOGOUT_REDIRECT_URI: string;
  SESSION_INACTIVITY_TIMEOUT_MINUTES: number;
  SESSION_TOKEN_WARNING_MINUTES: number;
  SESSION_PROMPT_TIMEOUT_SECONDS: number;
}

declare global {
  interface Window {
    FACTUCORE_CONFIG?: Partial<RuntimeConfig>;
  }
}

const requiredStringKeys: (keyof RuntimeConfig)[] = [
  "API_BASE_URL",
  "OIDC_AUTHORITY",
  "OIDC_CLIENT_ID",
  "OIDC_REDIRECT_URI",
  "OIDC_POST_LOGOUT_REDIRECT_URI",
];

const requiredNumberKeys: (keyof RuntimeConfig)[] = [
  "SESSION_INACTIVITY_TIMEOUT_MINUTES",
  "SESSION_TOKEN_WARNING_MINUTES",
  "SESSION_PROMPT_TIMEOUT_SECONDS",
];

export function getRuntimeConfig(): RuntimeConfig {
  const config = window.FACTUCORE_CONFIG;
  if (!config) throw new Error("No se cargó configuraciones/web/config/config.js.");

  const missingStrings = requiredStringKeys.filter(
    (key) => typeof config[key] !== "string" || config[key]!.trim().length === 0,
  );
  const invalidNumbers = requiredNumberKeys.filter(
    (key) => typeof config[key] !== "number" || !Number.isFinite(config[key]) || config[key]! <= 0,
  );
  const missing = [...missingStrings, ...invalidNumbers];

  if (missing.length) {
    throw new Error(`Faltan propiedades válidas de configuración: ${missing.join(", ")}.`);
  }

  return config as RuntimeConfig;
}
