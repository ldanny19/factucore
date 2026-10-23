export const APP_ROLES = ["SUPERADMIN", "ADMIN", "VENDEDOR"] as const;

export type AppRole = (typeof APP_ROLES)[number];

const ROLE_PRIORITY: AppRole[] = ["SUPERADMIN", "ADMIN", "VENDEDOR"];

const PATH_ROLES: Record<string, readonly AppRole[]> = {
  "/app": APP_ROLES,
  "/app/empresas": ["SUPERADMIN", "ADMIN"],
  "/app/establecimientos": ["SUPERADMIN", "ADMIN"],
  "/app/puntos-emision": ["SUPERADMIN", "ADMIN"],
  "/app/secuenciales": ["SUPERADMIN", "ADMIN"],
  "/app/certificados-firma": ["SUPERADMIN", "ADMIN"],
  "/app/configuracion": ["SUPERADMIN", "ADMIN"],
  "/app/configuracion/empresa": ["SUPERADMIN", "ADMIN"],
  "/app/tarifas-tributarias": ["SUPERADMIN", "ADMIN"],
  "/app/catalogos-tributarios": ["SUPERADMIN", "ADMIN"],
  "/app/definiciones-documentales": ["SUPERADMIN"],
  "/app/mapeos-xsd": ["SUPERADMIN"],
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
  "/app/accesos": ["SUPERADMIN"],
  "/app/accesos/usuarios": ["SUPERADMIN"],
  "/app/accesos/roles": ["SUPERADMIN"],
  "/app/accesos/permisos": ["SUPERADMIN"],
  "/app/accesos/asignacion-roles": ["SUPERADMIN"],
};

interface JwtPayload {
  exp?: number;
  realm_access?: { roles?: string[] };
}

export interface AccessContext {
  token: string | null;
  roles: AppRole[];
  permissions: string[];
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
export function getAccessContext(): AccessContext {
  const storedToken = typeof window !== "undefined"
    ? window.sessionStorage.getItem("factucore.access_token")
    : null;
  const token = storedToken;
  if (!token) return { token: null, roles: [], permissions: [], isTokenValid: false };

  const payload = decodePayload(token);
  const isTokenValid = Boolean(
    payload && typeof payload.exp === "number" && payload.exp * 1000 > Date.now(),
  );
  if (!payload || !isTokenValid) {
    return { token: null, roles: [], permissions: [], isTokenValid: false };
  }

  const realmRoles = payload.realm_access?.roles ?? [];
  const assigned = new Set(realmRoles);
  const roles = ROLE_PRIORITY.filter((role) => assigned.has(role));
  const realmAssigned = new Set(realmRoles);
  const permissions = ["GENERATE_COMPROBANTE", "REPROCESS_COMPROBANTE"]
    .filter((permission) => realmAssigned.has(permission));
  return { token, roles, permissions, isTokenValid: true };
}

export function canReprocessComprobante(context: AccessContext): boolean {
  return context.isTokenValid && context.roles.length > 0
    && context.permissions.includes("REPROCESS_COMPROBANTE");
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
