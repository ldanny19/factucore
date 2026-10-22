import { getRuntimeConfig } from "../../../config/runtime-config";
import { apiRequest } from "../../../shared/api/api-client";
import {
  HttpRepository,
  normalizarEstadoRegistro,
} from "../../../shared/api/HttpRepository";
import type {
  TarifaTributaria,
  TarifaTributariaDatos,
  TarifaTributariaFiltro,
} from "../domain/TarifaTributaria";
import type { TarifaTributariaRepository } from "../domain/TarifaTributariaRepository";

export function createHttpTarifaTributariaRepository(): TarifaTributariaRepository {
  const path = getRuntimeConfig().API_TARIFAS_TRIBUTARIAS_PATH.replace(/^\/+|\/+$/g, "");
  const repository = new HttpRepository<
    TarifaTributaria,
    TarifaTributariaDatos,
    TarifaTributariaFiltro
  >(path, {
    mapListQuery: (filtro) => ({
      codigoImpuesto: filtro.codigoImpuesto,
      ...(filtro.codigoPorcentaje ? { codigoPorcentaje: filtro.codigoPorcentaje } : {}),
    }),
    getIdEmpresa: () => 0,
    mapRequestData: (datos) => datos,
  });

  return {
    listar: (filtro) => repository.listar(filtro),
    obtenerVigente: (filtro) =>
      apiRequest<TarifaTributaria | null>({
        method: "GET",
        url: `${path}/vigente`,
        params: filtro,
      }).then((tarifa) => (tarifa ? normalizarEstadoRegistro(tarifa) : null)),
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
