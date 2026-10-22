import { getRuntimeConfig } from "../../../config/runtime-config";
import { apiRequest } from "../../../shared/api/api-client";
import { HttpRepository } from "../../../shared/api/HttpRepository";
import type {
  ConfiguracionEmpresa,
  ConfiguracionEmpresaDatos,
} from "../domain/ConfiguracionEmpresa";
import type { ConfiguracionEmpresaRepository } from "../domain/ConfiguracionEmpresaRepository";

export function createHttpConfiguracionEmpresaRepository(): ConfiguracionEmpresaRepository {
  const path = getRuntimeConfig().API_CONFIGURACIONES_EMPRESA_PATH.replace(/^\/+|\/+$/g, "");
  const repository = new HttpRepository<
    ConfiguracionEmpresa,
    ConfiguracionEmpresaDatos,
    number
  >(path, {
    mapListQuery: (idEmpresa) => ({ idEmpresa }),
    getIdEmpresa: (_id, datos) => datos.idEmpresa,
  });

  return {
    listar: (idEmpresa) => repository.listar(idEmpresa),
    obtener: (id) => repository.obtener(id),
    crear: (datos) => repository.crear(datos),
    actualizar: (id, datos) => repository.actualizar(id, datos),
    eliminar: (id) => repository.eliminar(id),
    inactivar: (id) =>
      apiRequest<void>({ method: "PATCH", url: `${path}/${id}/inactivar` }),
    reactivar: (id) =>
      apiRequest<void>({ method: "PATCH", url: `${path}/${id}/reactivar` }),
  };
}
