import type { Catalogo, CatalogoItem } from "../domain/Catalogo";
import type { CatalogoRepository } from "../domain/CatalogoRepository";

export interface CatalogoUseCase {
  listar(): Promise<Catalogo[]>;
  listarItems(catalogoId: number): Promise<CatalogoItem[]>;
}

export function createCatalogoUseCases(repository: CatalogoRepository): CatalogoUseCase {
  return {
    listar: () => repository.listar(),
    listarItems: (catalogoId) => repository.listarItems(catalogoId),
  };
}
