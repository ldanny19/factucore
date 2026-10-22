import type { Establecimiento, EstablecimientoDatos } from "./Establecimiento";

export interface EstablecimientoRepository {
  listar(idEmpresa: number): Promise<Establecimiento[]>;
  obtener(id: number): Promise<Establecimiento>;
  crear(datos: EstablecimientoDatos): Promise<Establecimiento>;
  actualizar(id: number, datos: EstablecimientoDatos): Promise<Establecimiento>;
  eliminar(id: number): Promise<void>;
}
