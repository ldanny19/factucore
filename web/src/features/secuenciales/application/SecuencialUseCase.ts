import type { Secuencial, SecuencialDatos } from "../domain/Secuencial";
import type { SecuencialRepository } from "../domain/SecuencialRepository";

export interface SecuencialUseCase {
  listar(): Promise<Secuencial[]>;
  obtener(id: number): Promise<Secuencial>;
  crear(datos: SecuencialDatos): Promise<Secuencial>;
  actualizar(id: number, datos: SecuencialDatos): Promise<Secuencial>;
  inactivar(id: number): Promise<void>;
  reactivar(id: number): Promise<void>;
  eliminar(id: number): Promise<void>;
}

export function createSecuencialUseCases(repository: SecuencialRepository): SecuencialUseCase {
  return {
    listar: () => repository.listar(),
    obtener: (id) => repository.obtener(id),
    crear: (datos) => repository.crear(datos),
    actualizar: (id, datos) => repository.actualizar(id, datos),
    inactivar: (id) => repository.inactivar(id),
    reactivar: (id) => repository.reactivar(id),
    eliminar: (id) => repository.eliminar(id),
  };
}
