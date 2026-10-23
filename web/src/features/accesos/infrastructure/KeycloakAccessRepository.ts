import { oidcConfig } from "../../../shared/infrastructure/auth/oidc-config";
import { getAccessContext } from "../../../shared/security/authorization";
import type { RolKeycloak, UsuarioKeycloak } from "../domain/Acceso";

interface RolKeycloakApi extends RolKeycloak {
  composite?: boolean;
  clientRole?: boolean;
  containerId?: string;
}

const FACTUCORE_REALM = "facturador";

export class KeycloakAccessError extends Error {
  constructor(
    message: string,
    readonly status: number,
  ) {
    super(message);
    this.name = "KeycloakAccessError";
  }
}

function getAdminRealmUrl(): string {
  const authority = new URL(oidcConfig.authority);
  const match = authority.pathname.match(/^(.*)\/realms\/([^/]+)\/?$/);
  if (!match) {
    throw new Error("La URL de autoridad OIDC no contiene un realm válido.");
  }
  if (decodeURIComponent(match[2]) !== FACTUCORE_REALM) {
    throw new Error("La administración de accesos está limitada al realm de FactuCore.");
  }
  return `${authority.origin}${match[1]}/admin/realms/${match[2]}`;
}

function validarUsuario(valor: unknown): UsuarioKeycloak {
  if (
    typeof valor !== "object" ||
    valor === null ||
    !("id" in valor) ||
    typeof valor.id !== "string" ||
    !("username" in valor) ||
    typeof valor.username !== "string" ||
    !("enabled" in valor) ||
    typeof valor.enabled !== "boolean"
  ) {
    throw new Error("Keycloak devolvió un usuario con un formato inesperado.");
  }
  return valor as UsuarioKeycloak;
}

function validarRol(valor: unknown): RolKeycloakApi {
  if (
    typeof valor !== "object" ||
    valor === null ||
    !("id" in valor) ||
    typeof valor.id !== "string" ||
    !("name" in valor) ||
    typeof valor.name !== "string"
  ) {
    throw new Error("Keycloak devolvió un rol con un formato inesperado.");
  }
  return valor as RolKeycloakApi;
}

async function solicitar<T>(ruta: string, init: RequestInit = {}): Promise<T | undefined> {
  const token = getAccessContext().token;
  if (!token) throw new Error("Inicia sesión nuevamente para administrar accesos.");

  const response = await fetch(`${getAdminRealmUrl()}${ruta}`, {
    ...init,
    headers: {
      Accept: "application/json",
      Authorization: `Bearer ${token}`,
      ...(init.body ? { "Content-Type": "application/json" } : {}),
      ...init.headers,
    },
  });

  if (!response.ok) {
    const detalle = await response.text();
    const motivo = detalle.trim().slice(0, 240);
    throw new KeycloakAccessError(
      motivo || `Keycloak rechazó la operación (HTTP ${response.status}).`,
      response.status,
    );
  }

  if (response.status === 204) return undefined;
  const texto = await response.text();
  if (!texto) return undefined;
  try {
    return JSON.parse(texto) as T;
  } catch {
    throw new Error("Keycloak devolvió una respuesta que no es JSON válido.");
  }
}

export const keycloakAccessRepository = {
  async listarUsuarios(busqueda: string, first: number, max: number): Promise<UsuarioKeycloak[]> {
    const parametros = new URLSearchParams({ first: String(first), max: String(max) });
    if (busqueda.trim()) parametros.set("search", busqueda.trim());
    const resultado = await solicitar<unknown[]>(`/users?${parametros.toString()}`);
    if (!Array.isArray(resultado)) throw new Error("Keycloak devolvió una lista de usuarios inválida.");
    return resultado.map(validarUsuario);
  },

  async crearUsuario(usuario: Pick<UsuarioKeycloak, "username" | "firstName" | "lastName" | "email">) {
    await solicitar("/users", {
      method: "POST",
      body: JSON.stringify({
        ...usuario,
        enabled: true,
        emailVerified: false,
        requiredActions: ["UPDATE_PASSWORD"],
      }),
    });
  },

  async actualizarUsuario(usuario: UsuarioKeycloak) {
    await solicitar(`/users/${encodeURIComponent(usuario.id)}`, {
      method: "PUT",
      body: JSON.stringify(usuario),
    });
  },

  async enviarEnlaceDefinirContrasena(idUsuario: string) {
    const parametros = new URLSearchParams({
      client_id: oidcConfig.client_id,
      redirect_uri: oidcConfig.redirect_uri,
    });
    await solicitar(
      `/users/${encodeURIComponent(idUsuario)}/execute-actions-email?${parametros.toString()}`,
      { method: "PUT", body: JSON.stringify(["UPDATE_PASSWORD", "VERIFY_EMAIL"]) },
    );
  },

  async listarRoles(): Promise<RolKeycloakApi[]> {
    const resultado = await solicitar<unknown[]>("/roles");
    if (!Array.isArray(resultado)) throw new Error("Keycloak devolvió una lista de roles inválida.");
    return resultado.map(validarRol);
  },

  async crearRol(nombre: string, descripcion: string) {
    await solicitar("/roles", {
      method: "POST",
      body: JSON.stringify({ name: nombre, description: descripcion }),
    });
  },

  async eliminarRol(rol: RolKeycloak) {
    await solicitar(`/roles-by-id/${encodeURIComponent(rol.id)}`, { method: "DELETE" });
  },

  async listarRolesUsuario(idUsuario: string): Promise<RolKeycloakApi[]> {
    const resultado = await solicitar<unknown[]>(
      `/users/${encodeURIComponent(idUsuario)}/role-mappings/realm`,
    );
    if (!Array.isArray(resultado)) throw new Error("Keycloak devolvió una asignación de roles inválida.");
    return resultado.map(validarRol);
  },

  async asignarRolesUsuario(idUsuario: string, roles: RolKeycloakApi[]) {
    await solicitar(`/users/${encodeURIComponent(idUsuario)}/role-mappings/realm`, {
      method: "POST",
      body: JSON.stringify(roles),
    });
  },

  async retirarRolesUsuario(idUsuario: string, roles: RolKeycloakApi[]) {
    await solicitar(`/users/${encodeURIComponent(idUsuario)}/role-mappings/realm`, {
      method: "DELETE",
      body: JSON.stringify(roles),
    });
  },

  async listarRolesCompuestos(idRol: string): Promise<RolKeycloakApi[]> {
    const resultado = await solicitar<unknown[]>(
      `/roles-by-id/${encodeURIComponent(idRol)}/composites`,
    );
    if (!Array.isArray(resultado)) throw new Error("Keycloak devolvió los permisos del rol de forma inválida.");
    return resultado.map(validarRol);
  },

  async asignarPermisoRol(idRol: string, permiso: RolKeycloakApi) {
    await solicitar(`/roles-by-id/${encodeURIComponent(idRol)}/composites`, {
      method: "POST",
      body: JSON.stringify([permiso]),
    });
  },

  async retirarPermisoRol(idRol: string, permiso: RolKeycloakApi) {
    await solicitar(`/roles-by-id/${encodeURIComponent(idRol)}/composites`, {
      method: "DELETE",
      body: JSON.stringify([permiso]),
    });
  },
};
