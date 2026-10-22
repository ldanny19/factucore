import type { PuntoEmision, PuntoEmisionDatos } from "../domain/PuntoEmision";
import type { PuntoEmisionRepository } from "../domain/PuntoEmisionRepository";

export interface PuntoEmisionUseCase {
  listar(idEstablecimiento: number): Promise<PuntoEmision[]>;
  obtener(id: number): Promise<PuntoEmision>;
  crear(datos: PuntoEmisionDatos): Promise<PuntoEmision>;
  actualizar(id: number, datos: PuntoEmisionDatos): Promise<PuntoEmision>;
  eliminar(id: number): Promise<void>;
}

export function createPuntoEmisionUseCases(
  repository: PuntoEmisionRepository,
): PuntoEmisionUseCase {
  return {
    listar: (idEstablecimiento) => repository.listar(idEstablecimiento),
    obtener: (id) => repository.obtener(id),
    crear: (datos) => repository.crear(datos),
    actualizar: (id, datos) => repository.actualizar(id, datos),
    eliminar: (id) => repository.eliminar(id),
  };
}
