import type {
  Catalogo,
  CatalogoDatos,
  CatalogoItem,
  CatalogoItemDatos,
} from "../domain/Catalogo";
import type { CatalogoRepository } from "../domain/CatalogoRepository";

export interface CatalogoUseCase {
  listar(): Promise<Catalogo[]>;
  obtener(id: number): Promise<Catalogo>;
  crear(datos: CatalogoDatos): Promise<Catalogo>;
  actualizar(id: number, datos: CatalogoDatos): Promise<Catalogo>;
  inactivar(id: number): Promise<void>;
  reactivar(id: number): Promise<void>;
  eliminar(id: number): Promise<void>;
  listarItems(catalogoId: number): Promise<CatalogoItem[]>;
  obtenerItem(id: number): Promise<CatalogoItem>;
  crearItem(datos: CatalogoItemDatos): Promise<CatalogoItem>;
  actualizarItem(id: number, datos: CatalogoItemDatos): Promise<CatalogoItem>;
  inactivarItem(id: number): Promise<void>;
  reactivarItem(id: number): Promise<void>;
  eliminarItem(id: number): Promise<void>;
}

export function createCatalogoUseCases(repository: CatalogoRepository): CatalogoUseCase {
  return {
    listar: () => repository.listar(),
    obtener: (id) => repository.obtener(id),
    crear: (datos) => repository.crear(datos),
    actualizar: (id, datos) => repository.actualizar(id, datos),
    inactivar: (id) => repository.inactivar(id),
    reactivar: (id) => repository.reactivar(id),
    eliminar: (id) => repository.eliminar(id),
    listarItems: (catalogoId) => repository.listarItems(catalogoId),
    obtenerItem: (id) => repository.obtenerItem(id),
    crearItem: (datos) => repository.crearItem(datos),
    actualizarItem: (id, datos) => repository.actualizarItem(id, datos),
    inactivarItem: (id) => repository.inactivarItem(id),
    reactivarItem: (id) => repository.reactivarItem(id),
    eliminarItem: (id) => repository.eliminarItem(id),
  };
}
