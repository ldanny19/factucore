export interface Catalogo {
  id: number;
  codigo: string;
  nombre: string;
  descripcion: string | null;
  estadoRegistro: string;
}

export interface CatalogoItem {
  id: number;
  idCatalogo: number;
  codigo: string;
  nombre: string;
  descripcion: string | null;
  orden: number | null;
  estadoRegistro: string;
}
