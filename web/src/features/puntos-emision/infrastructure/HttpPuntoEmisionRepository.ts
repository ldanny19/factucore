import { getRuntimeConfig } from "../../../config/runtime-config";
import { HttpRepository } from "../../../shared/api/HttpRepository";
import type { PuntoEmision, PuntoEmisionDatos } from "../domain/PuntoEmision";
import type { PuntoEmisionRepository } from "../domain/PuntoEmisionRepository";

export function createHttpPuntoEmisionRepository(): PuntoEmisionRepository {
  return new HttpRepository<PuntoEmision, PuntoEmisionDatos, number>(
    getRuntimeConfig().API_PUNTOS_EMISION_PATH,
    {
      mapListQuery: (idEstablecimiento) => ({ idEstablecimiento }),
      getIdEmpresa: (_id, datos) => datos.idEmpresa,
      mapRequestData: ({ idEstablecimiento, codigo, nombre }) => ({
        idEstablecimiento,
        codigo,
        nombre,
      }),
    },
  );
}
