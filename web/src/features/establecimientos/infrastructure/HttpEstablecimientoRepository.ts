import { getRuntimeConfig } from "../../../config/runtime-config";
import { HttpRepository } from "../../../shared/api/HttpRepository";
import type {
  Establecimiento,
  EstablecimientoDatos,
} from "../domain/Establecimiento";
import type { EstablecimientoRepository } from "../domain/EstablecimientoRepository";

export function createHttpEstablecimientoRepository(): EstablecimientoRepository {
  return new HttpRepository<Establecimiento, EstablecimientoDatos, number>(
    getRuntimeConfig().API_ESTABLECIMIENTOS_PATH,
    {
      mapListQuery: (idEmpresa) => ({ idEmpresa }),
      getIdEmpresa: (_id, datos) => datos.idEmpresa,
      mapRequestData: (datos) => ({
        idEmpresa: datos.idEmpresa,
        codigo: datos.codigo,
        nombre: datos.nombre,
        direccion: datos.direccion,
      }),
    },
  );
}
