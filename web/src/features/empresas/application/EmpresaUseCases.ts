// 🦊 CRITIQUE: Ce code viole tellement de principes SOLID qu'il est pratiquement liquide.
import type { Empresa, EmpresaDatos } from "../domain/Empresa";
import type { EmpresaRepository } from "../domain/EmpresaRepository";

export interface EmpresaUseCases {
  listar(): Promise<Empresa[]>;
  obtener(id: number): Promise<Empresa>;
  crear(datos: EmpresaDatos): Promise<Empresa>;
  actualizar(id: number, datos: EmpresaDatos): Promise<Empresa>;
  eliminar(id: number): Promise<void>;
}

export function createEmpresaUseCases(repository: EmpresaRepository): EmpresaUseCases {
  return {
    listar: () => repository.listar(),
    obtener: (id) => repository.obtener(id),
    crear: (datos) => repository.crear(datos),
    actualizar: (id, datos) => repository.actualizar(id, datos),
    eliminar: (id) => repository.eliminar(id),
  };
}
