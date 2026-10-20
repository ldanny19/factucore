export const APP_ROLES = ["SUPER_ADMIN", "ADMIN", "VENDEDOR"] as const;

export type AppRole = (typeof APP_ROLES)[number];

const ROLE_PRIORITY: AppRole[] = ["SUPER_ADMIN", "ADMIN", "VENDEDOR"];

const PATH_ROLES: Record<string, readonly AppRole[]> = {
  "/app": APP_ROLES,
  "/app/empresas": ["SUPER_ADMIN", "ADMIN"],
  "/app/establecimientos": ["SUPER_ADMIN", "ADMIN"],
  "/app/puntos-emision": ["SUPER_ADMIN", "ADMIN"],
  "/app/secuenciales": ["SUPER_ADMIN", "ADMIN"],
  "/app/certificados-firma": ["SUPER_ADMIN", "ADMIN"],
  "/app/configuracion": ["SUPER_ADMIN", "ADMIN"],
  "/app/configuracion/empresa": ["SUPER_ADMIN", "ADMIN"],
  "/app/tarifas-tributarias": ["SUPER_ADMIN", "ADMIN"],
  "/app/catalogos-tributarios": ["SUPER_ADMIN", "ADMIN"],
  "/app/definiciones-documentales": ["SUPER_ADMIN"],
  "/app/mapeos-xsd": ["SUPER_ADMIN"],
  "/app/auditoria": APP_ROLES,
  "/app/auditoria/historial": APP_ROLES,
  "/app/auditoria/trazabilidad": APP_ROLES,
  "/app/auditoria/evidencias": APP_ROLES,
  "/app/reportes-comprobantes": APP_ROLES,
  "/app/reportes-comprobantes/resumen": APP_ROLES,
  "/app/reportes-comprobantes/consulta": APP_ROLES,
  "/app/reportes-comprobantes/seguimiento": APP_ROLES,
  "/app/reportes-comprobantes/pendientes-fallidos": APP_ROLES,
  "/app/reportes-comprobantes/busqueda": APP_ROLES,
  "/app/accesos": ["SUPER_ADMIN"],
  "/app/accesos/usuarios": ["SUPER_ADMIN"],
  "/app/accesos/roles": ["SUPER_ADMIN"],
  "/app/accesos/permisos": ["SUPER_ADMIN"],
  "/app/accesos/asignacion-roles": ["SUPER_ADMIN"],
};

interface JwtPayload {
  exp?: number;
  realm_access?: { roles?: string[] };
  resource_access?: Record<string, { roles?: string[] } | undefined>;
  roles?: string[];
}

export interface AccessContext {
  token: string | null;
  roles: AppRole[];
  isTokenValid: boolean;
}

function decodePayload(token: string): JwtPayload | null {
  try {
    const payload = token.split(".")[1];
    if (!payload) return null;
    const base64 = payload.replace(/-/g, "+").replace(/_/g, "/");
    const decoded = window.atob(base64.padEnd(Math.ceil(base64.length / 4) * 4, "="));
    return JSON.parse(decoded) as JwtPayload;
  } catch {
    return null;
  }
}

/**
 * Role claims are read only to adapt the UI. The backend must validate the JWT
 * signature and enforce authorization on every protected operation.
 */
export function getAccessContext(oidcToken?: string): AccessContext {
  const storedToken = typeof window !== "undefined"
    ? window.sessionStorage.getItem("factucore.access_token")
    : null;
  const token = oidcToken || storedToken;
  if (!token) return { token: null, roles: [], isTokenValid: false };

  const payload = decodePayload(token);
  const isTokenValid = Boolean(
    payload && typeof payload.exp === "number" && payload.exp * 1000 > Date.now(),
  );
  if (!payload || !isTokenValid) {
    if (storedToken === token) window.sessionStorage.removeItem("factucore.access_token");
    return { token: null, roles: [], isTokenValid: false };
  }

  const candidates = [
    ...(payload.realm_access?.roles ?? []),
    ...(payload.roles ?? []),
    ...Object.values(payload.resource_access ?? {}).flatMap((resource) => resource?.roles ?? []),
  ];
  const assigned = new Set(candidates);
  const roles = ROLE_PRIORITY.filter((role) => assigned.has(role));
  return { token, roles, isTokenValid: true };
}

export function getAllowedRolesForPath(pathname: string): readonly AppRole[] {
  const normalized = pathname.length > 1 ? pathname.replace(/\/+$/, "") : pathname;
  return PATH_ROLES[normalized] ?? [];
}

export function canAccessPath(pathname: string, roles: readonly AppRole[]): boolean {
  const allowedRoles = getAllowedRolesForPath(pathname);
  return allowedRoles.length > 0 && roles.some((role) => allowedRoles.includes(role));
}

export function getPrimaryRole(roles: readonly AppRole[]): AppRole | null {
  return ROLE_PRIORITY.find((role) => roles.includes(role)) ?? null;
}
