import type {
  Catalogo,
  CatalogoDatos,
  CatalogoItem,
  CatalogoItemDatos,
} from "./Catalogo";

export interface CatalogoRepository {
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
