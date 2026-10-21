export interface RuntimeConfig {
  API_BASE_URL: string;
  API_VERSION: string;
  API_PATH_PREFIX: string;
  API_EMPRESAS_PATH: string;
  OIDC_AUTHORITY: string;
  OIDC_CLIENT_ID: string;
  OIDC_REDIRECT_URI: string;
  OIDC_POST_LOGOUT_REDIRECT_URI: string;
  SESSION_INACTIVITY_TIMEOUT_MINUTES: number;
  SESSION_TOKEN_WARNING_MINUTES: number;
  SESSION_PROMPT_TIMEOUT_SECONDS: number;
  MESSAGES: Record<string, string>;
}

declare global {
  interface Window {
    FACTUCORE_CONFIG?: Partial<RuntimeConfig>;
  }
}

const requiredStringKeys: (keyof RuntimeConfig)[] = [
  "API_BASE_URL", "API_VERSION", "API_PATH_PREFIX", "API_EMPRESAS_PATH",
  "OIDC_AUTHORITY", "OIDC_CLIENT_ID", "OIDC_REDIRECT_URI", "OIDC_POST_LOGOUT_REDIRECT_URI",
];

const requiredNumberKeys: (keyof RuntimeConfig)[] = [
  "SESSION_INACTIVITY_TIMEOUT_MINUTES", "SESSION_TOKEN_WARNING_MINUTES", "SESSION_PROMPT_TIMEOUT_SECONDS",
];

export function getRuntimeConfig(): RuntimeConfig {
  const config = window.FACTUCORE_CONFIG;
  if (!config) throw new Error("config.missing");

  const missingStrings = requiredStringKeys.filter(
    (key) => typeof config[key] !== "string" || config[key]!.trim().length === 0,
  );
  const invalidNumbers = requiredNumberKeys.filter(
    (key) => typeof config[key] !== "number" || !Number.isFinite(config[key]) || config[key]! <= 0,
  );
  if (missingStrings.length || invalidNumbers.length || !config.MESSAGES || typeof config.MESSAGES !== "object") {
    throw new Error("config.invalid");
  }
  return config as RuntimeConfig;
}
