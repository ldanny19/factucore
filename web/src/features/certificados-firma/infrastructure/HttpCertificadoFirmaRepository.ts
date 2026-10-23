import { getRuntimeConfig } from "../../../config/runtime-config";
import { apiRequest } from "../../../shared/api/api-client";
import { HttpRepository } from "../../../shared/api/HttpRepository";
import type { CertificadoFirma, CertificadoFirmaDatos } from "../domain/CertificadoFirma";
import type { CertificadoFirmaRepository } from "../domain/CertificadoFirmaRepository";

export function createHttpCertificadoFirmaRepository(): CertificadoFirmaRepository {
  const path = getRuntimeConfig().API_CERTIFICADOS_FIRMA_PATH.replace(/^\/+|\/+$/g, "");
  const repository = new HttpRepository<CertificadoFirma, CertificadoFirmaDatos, number>(
    path,
    {
      mapListQuery: (idEmpresa) => ({ idEmpresa }),
      getIdEmpresa: (_id, datos) => datos.idEmpresa,
    },
  );

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
