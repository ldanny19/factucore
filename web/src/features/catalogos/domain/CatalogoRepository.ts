import type { Catalogo, CatalogoItem } from "./Catalogo";

export interface CatalogoRepository {
  listar(): Promise<Catalogo[]>;
  listarItems(catalogoId: number): Promise<CatalogoItem[]>;
}
