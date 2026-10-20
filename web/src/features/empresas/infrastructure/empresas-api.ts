import axios from "axios";
import { getRuntimeConfig } from "../../../config/runtime-config";

export interface Empresa {
  id: number;
  ruc: string;
  razonSocial: string;
  nombreComercial: string | null;
  direccionMatriz: string;
  obligadoContabilidad: "SI" | "NO";
  contribuyenteRimpe: string | null;
  estadoRegistro: "ACTIVO" | "INACTIVO" | "ELIMINADO" | string;
  usuarioCreacion?: string | null;
  usuarioModificacion?: string | null;
  fechaCreacion?: string | null;
  fechaModificacion?: string | null;
  observacion?: string | null;
}

export interface EmpresaDatos {
  ruc: string;
  razonSocial: string;
  nombreComercial: string | null;
  direccionMatriz: string;
  obligadoContabilidad: "SI" | "NO";
  contribuyenteRimpe: string | null;
}

interface AdministracionResponse<T> {
  exitoso: boolean;
  codigo: string;
  mensaje: string;
  datos: T;
}

function api() {
  const config = getRuntimeConfig();
  const token = sessionStorage.getItem("factucore.access_token");
  return axios.create({
    baseURL: config.API_BASE_URL.replace(/\\/$/, ""),
    headers: token ? { Authorization: \`Bearer \${token}\` } : {},
  });
}

function envelope<T>(response: { data: AdministracionResponse<T> }): T {
  if (!response.data.exitoso) {
    throw new Error(response.data.mensaje || response.data.codigo || "La operación no fue exitosa.");
  }
  return response.data.datos;
}

function requestMetadata() {
  const now = new Date();
  const pad = (value: number, length = 2) => String(value).padStart(length, "0");
  const fechaInicio = \`\${now.getFullYear()}-\${pad(now.getMonth() + 1)}-\${pad(now.getDate())} \${pad(now.getHours())}:\${pad(now.getMinutes())}:\${pad(now.getSeconds())}.\${pad(now.getMilliseconds(), 3)}\`;
  return { fechaInicio, canal: "WEB", idEmpresa: null };
}

export async function listarEmpresas(): Promise<Empresa[]> {
  return envelope(await api().get<AdministracionResponse<Empresa[]>>("/api/v1/empresas"));
}

export async function obtenerEmpresa(id: number): Promise<Empresa> {
  return envelope(await api().get<AdministracionResponse<Empresa>>(\`/api/v1/empresas/\${id}\`));
}

export async function crearEmpresa(datos: EmpresaDatos): Promise<Empresa> {
  return envelope(await api().post<AdministracionResponse<Empresa>>("/api/v1/empresas", {
    ...requestMetadata(),
    datos,
  }));
}

export async function actualizarEmpresa(id: number, datos: EmpresaDatos): Promise<Empresa> {
  return envelope(await api().put<AdministracionResponse<Empresa>>(\`/api/v1/empresas/\${id}\`, {
    ...requestMetadata(),
    datos,
  }));
}

export async function eliminarEmpresa(id: number): Promise<void> {
  envelope(await api().delete<AdministracionResponse<void>>(\`/api/v1/empresas/\${id}\`));
}
