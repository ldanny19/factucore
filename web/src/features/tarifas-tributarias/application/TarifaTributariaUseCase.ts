import type {
  TarifaTributaria,
  TarifaTributariaDatos,
  TarifaTributariaFiltro,
} from "../domain/TarifaTributaria";
import type { TarifaTributariaRepository } from "../domain/TarifaTributariaRepository";

export interface TarifaTributariaUseCase {
  listar(filtro: TarifaTributariaFiltro): Promise<TarifaTributaria[]>;
  obtenerVigente(
    filtro: TarifaTributariaFiltro & { fecha: string },
  ): Promise<TarifaTributaria | null>;
  obtener(id: number): Promise<TarifaTributaria>;
  crear(datos: TarifaTributariaDatos): Promise<TarifaTributaria>;
  actualizar(id: number, datos: TarifaTributariaDatos): Promise<TarifaTributaria>;
  inactivar(id: number): Promise<void>;
  reactivar(id: number): Promise<void>;
  eliminar(id: number): Promise<void>;
}

export function createTarifaTributariaUseCases(
  repository: TarifaTributariaRepository,
): TarifaTributariaUseCase {
  return {
    listar: (filtro) => repository.listar(filtro),
    obtenerVigente: (filtro) => repository.obtenerVigente(filtro),
    obtener: (id) => repository.obtener(id),
    crear: (datos) => repository.crear(datos),
    actualizar: (id, datos) => repository.actualizar(id, datos),
    inactivar: (id) => repository.inactivar(id),
    reactivar: (id) => repository.reactivar(id),
    eliminar: (id) => repository.eliminar(id),
  };
}
