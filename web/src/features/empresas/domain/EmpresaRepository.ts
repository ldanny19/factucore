import type { Empresa, EmpresaDatos } from "./Empresa";

export interface EmpresaRepository {
  listar(): Promise<Empresa[]>;
  obtener(id: number): Promise<Empresa>;
  crear(datos: EmpresaDatos): Promise<Empresa>;
  actualizar(id: number, datos: EmpresaDatos): Promise<Empresa>;
  eliminar(id: number): Promise<void>;
}
