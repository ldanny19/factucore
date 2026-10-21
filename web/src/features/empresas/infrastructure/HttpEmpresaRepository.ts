import { apiRequest } from "../../../shared/api/api-client";
import type { AdministracionRequest } from "../../../shared/api/administracion-contracts";
import type { Empresa, EmpresaDatos } from "../domain/Empresa";
import type { EmpresaRepository } from "../domain/EmpresaRepository";

function createRequest<T>(datos: T): AdministracionRequest<T> {
  const now = new Date();
  const pad = (value: number, length = 2) => String(value).padStart(length, "0");
  const fechaInicio = `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())} ${pad(now.getHours())}:${pad(now.getMinutes())}:${pad(now.getSeconds())}.${pad(now.getMilliseconds(), 3)}`;

  return {
    fechaInicio,
    canal: "WEB",
    idEmpresa: null,
    datos,
  };
}

export class HttpEmpresaRepository implements EmpresaRepository {
  listar(): Promise<Empresa[]> {
    return apiRequest<Empresa[]>({ method: "GET", url: "/api/v1/empresas" });
  }

  obtener(id: number): Promise<Empresa> {
    return apiRequest<Empresa>({ method: "GET", url: `/api/v1/empresas/${id}` });
  }

  crear(datos: EmpresaDatos): Promise<Empresa> {
    return apiRequest<Empresa>({
      method: "POST",
      url: "/api/v1/empresas",
      data: createRequest(datos),
    });
  }

  actualizar(id: number, datos: EmpresaDatos): Promise<Empresa> {
    return apiRequest<Empresa>({
      method: "PUT",
      url: `/api/v1/empresas/${id}`,
      data: createRequest(datos),
    });
  }

  eliminar(id: number): Promise<void> {
    return apiRequest<void>({ method: "DELETE", url: `/api/v1/empresas/${id}` });
  }
}
