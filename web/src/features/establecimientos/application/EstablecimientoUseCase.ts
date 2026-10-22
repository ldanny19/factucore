import type { Establecimiento, EstablecimientoDatos } from "../domain/Establecimiento";
import type { EstablecimientoRepository } from "../domain/EstablecimientoRepository";

export interface EstablecimientoUseCase {
  listar(idEmpresa: number): Promise<Establecimiento[]>;
  obtener(id: number): Promise<Establecimiento>;
  crear(datos: EstablecimientoDatos): Promise<Establecimiento>;
  actualizar(id: number, datos: EstablecimientoDatos): Promise<Establecimiento>;
  eliminar(id: number): Promise<void>;
}

export function serviceEstablecimientoUseCase(repository: EstablecimientoRepository): EstablecimientoUseCase {
  return {
    listar: (idEmpresa) => repository.listar(idEmpresa),
    obtener: (id) => repository.obtener(id),
    crear: (datos) => repository.crear(datos),
    actualizar: (id, datos) => repository.actualizar(id, datos),
    eliminar: (id) => repository.eliminar(id),
  };
}
