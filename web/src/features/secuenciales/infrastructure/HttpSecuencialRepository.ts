import { getRuntimeConfig } from "../../../config/runtime-config";
import { apiRequest } from "../../../shared/api/api-client";
import { HttpRepository } from "../../../shared/api/HttpRepository";
import type { Secuencial, SecuencialDatos } from "../domain/Secuencial";
import type { SecuencialRepository } from "../domain/SecuencialRepository";

export function createHttpSecuencialRepository(): SecuencialRepository {
  const path = getRuntimeConfig().API_SECUENCIALES_PATH.replace(/^\/+|\/+$/g, "");
  const repository = new HttpRepository<Secuencial, SecuencialDatos>(path, {
    getIdEmpresa: (_id, datos) => datos.idEmpresa,
    mapRequestData: ({ idPuntoEmision, codigoDocumento, ultimoSecuencial }) => ({
      idPuntoEmision,
      codigoDocumento,
      ultimoSecuencial,
    }),
  });

  return {
    listar: () => repository.listar(),
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
