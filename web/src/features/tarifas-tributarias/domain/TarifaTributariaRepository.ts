import type {
  TarifaTributaria,
  TarifaTributariaDatos,
  TarifaTributariaFiltro,
} from "./TarifaTributaria";

export interface TarifaTributariaRepository {
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
