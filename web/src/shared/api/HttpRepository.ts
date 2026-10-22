import { apiRequest } from "./api-client";
import type { AdministracionRequest } from "./administracion-contracts";

export interface HttpRepositoryOptions<TDatos, TListQuery> {
  mapListQuery?: (query: TListQuery) => Record<string, string | number>;
  getIdEmpresa?: (id: number | null, datos: TDatos) => number;
  mapRequestData?: (datos: TDatos) => unknown;
}

function createRequest<TDatos>(idEmpresa: number, datos: TDatos): AdministracionRequest<TDatos> {
  const now = new Date();
  const pad = (value: number, length = 2) => String(value).padStart(length, "0");
  const fechaInicio =
    `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())} ` +
    `${pad(now.getHours())}:${pad(now.getMinutes())}:${pad(now.getSeconds())}.` +
    pad(now.getMilliseconds(), 3);

  return { fechaInicio, canal: "WEB", idEmpresa, datos };
}

export class HttpRepository<T, TDatos, TListQuery = void> {
  private readonly path: string;

  constructor(
    path: string,
    private readonly options: HttpRepositoryOptions<TDatos, TListQuery> = {},
  ) {
    this.path = path.replace(/^\/+|\/+$/g, "");
  }

  listar(query?: TListQuery): Promise<T[]> {
    if (query !== undefined && !this.options.mapListQuery) {
      throw new Error("No se configuró el mapeo de parámetros para listar.");
    }

    const params =
      query === undefined ? undefined : this.options.mapListQuery?.(query);

    return apiRequest<T[]>({ method: "GET", url: this.path, params });
  }

  obtener(id: number): Promise<T> {
    return apiRequest<T>({ method: "GET", url: `${this.path}/${id}` });
  }

  crear(datos: TDatos): Promise<T> {
    return apiRequest<T>({
      method: "POST",
      url: this.path,
      data: this.crearRequest(null, datos),
    });
  }

  actualizar(id: number, datos: TDatos): Promise<T> {
    return apiRequest<T>({
      method: "PUT",
      url: `${this.path}/${id}`,
      data: this.crearRequest(id, datos),
    });
  }

  eliminar(id: number): Promise<void> {
    return apiRequest<void>({ method: "DELETE", url: `${this.path}/${id}` });
  }

  private crearRequest(id: number | null, datos: TDatos): AdministracionRequest<unknown> {
    const idEmpresa = this.options.getIdEmpresa?.(id, datos) ?? id ?? 0;
    const requestData = this.options.mapRequestData
      ? this.options.mapRequestData(datos)
      : datos;

    return createRequest(idEmpresa, requestData);
  }
}
