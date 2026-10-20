export interface RuntimeConfig { API_BASE_URL: string; OIDC_AUTHORITY: string; OIDC_CLIENT_ID: string; OIDC_REDIRECT_URI: string; }
declare global { interface Window { FACTUCORE_CONFIG?: Partial<RuntimeConfig>; } }
const requiredKeys: (keyof RuntimeConfig)[] = ["API_BASE_URL", "OIDC_AUTHORITY", "OIDC_CLIENT_ID", "OIDC_REDIRECT_URI"];
export function getRuntimeConfig(): RuntimeConfig { const config = window.FACTUCORE_CONFIG; if (!config) throw new Error("No se cargó configuraciones/web/config/config.js."); const missing = requiredKeys.filter((key) => typeof config[key] !== "string" || config[key]!.trim().length === 0); if (missing.length) throw new Error(`Faltan propiedades de configuración: ${missing.join(", ")}.`); return config as RuntimeConfig; }
