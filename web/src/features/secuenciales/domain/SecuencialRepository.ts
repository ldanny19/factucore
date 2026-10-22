import type { Secuencial, SecuencialDatos } from "./Secuencial";

export interface SecuencialRepository {
  listar(): Promise<Secuencial[]>;
  obtener(id: number): Promise<Secuencial>;
  crear(datos: SecuencialDatos): Promise<Secuencial>;
  actualizar(id: number, datos: SecuencialDatos): Promise<Secuencial>;
  inactivar(id: number): Promise<void>;
  reactivar(id: number): Promise<void>;
  eliminar(id: number): Promise<void>;
}
