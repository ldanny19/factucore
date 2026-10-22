export interface RuntimeConfig {
  API_BASE_URL: string;
  API_PATH_PREFIX: string;
  API_VERSION: string;
  API_EMPRESAS_PATH: string;
  API_ESTABLECIMIENTOS_PATH: string;
  API_PUNTOS_EMISION_PATH: string;
  API_SECUENCIALES_PATH: string;
  API_TARIFAS_TRIBUTARIAS_PATH: string;
  API_MAPEOS_XSD_PATH: string;
  API_DOCUMENTOS_XSD_PATH: string;
  API_CONFIGURACIONES_EMPRESA_PATH: string;
  API_COMPROBANTES_PATH: string;
  API_CERTIFICADOS_FIRMA_PATH: string;
  API_CATALOGOS_PATH: string;
  API_CATALOGO_ITEMS_PATH: string;
  CERTIFICADO_STORAGE_PATH: string;
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
  "API_PATH_PREFIX",
  "API_VERSION",
  "API_EMPRESAS_PATH",
  "API_ESTABLECIMIENTOS_PATH",
  "API_PUNTOS_EMISION_PATH",
  "API_SECUENCIALES_PATH",
  "API_TARIFAS_TRIBUTARIAS_PATH",
  "API_MAPEOS_XSD_PATH",
  "API_DOCUMENTOS_XSD_PATH",
  "API_CONFIGURACIONES_EMPRESA_PATH",
  "API_COMPROBANTES_PATH",
  "API_CERTIFICADOS_FIRMA_PATH",
  "API_CATALOGOS_PATH",
  "API_CATALOGO_ITEMS_PATH",
  "CERTIFICADO_STORAGE_PATH",
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
  if (!config) throw new Error("La configuración externa de FactuCore no está disponible.");

  const missingStrings = requiredStringKeys.filter(
    (key) => typeof config[key] !== "string" || config[key]!.trim().length === 0,
  );
  const invalidNumbers = requiredNumberKeys.filter(
    (key) => typeof config[key] !== "number" || !Number.isFinite(config[key]) || config[key]! <= 0,
  );
  const invalidSessionLimits =
    typeof config.SESSION_INACTIVITY_TIMEOUT_MINUTES === "number" &&
    config.SESSION_INACTIVITY_TIMEOUT_MINUTES > 15;
  const storagePath = config.CERTIFICADO_STORAGE_PATH;
  const invalidCertificateStoragePath =
    typeof storagePath !== "string" ||
    !storagePath.startsWith("/") ||
    storagePath.includes("\\") ||
    storagePath.split("/").some((segment) => segment === "." || segment === "..");

  if (
    missingStrings.length ||
    invalidNumbers.length ||
    invalidSessionLimits ||
    invalidCertificateStoragePath
  ) {
    throw new Error("La configuración externa de FactuCore contiene propiedades inválidas.");
  }
  return config as RuntimeConfig;
}
