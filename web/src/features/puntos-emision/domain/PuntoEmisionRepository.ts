import type { PuntoEmision, PuntoEmisionDatos } from "./PuntoEmision";

export interface PuntoEmisionRepository {
  listar(idEstablecimiento: number): Promise<PuntoEmision[]>;
  obtener(id: number): Promise<PuntoEmision>;
  crear(datos: PuntoEmisionDatos): Promise<PuntoEmision>;
  actualizar(id: number, datos: PuntoEmisionDatos): Promise<PuntoEmision>;
  eliminar(id: number): Promise<void>;
}
