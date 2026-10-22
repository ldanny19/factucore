export interface Establecimiento{
  id: number;
  idEmpresa: number;
  codigo: string;
  nombre: string;
  direccion: string;
  estadoRegistro: "ACTIVO" | "INACTIVO" | "ELIMINADO" | string;
  usuarioCreacion?: string | null;
  usuarioModificacion?: string | null;
  fechaCreacion?: string | null;
  fechaModificacion?: string | null;
  observacion?: string | null;
}

export interface EstablecimientoDatos{
  idEmpresa: number;
  codigo: string;
  nombre: string;
  direccion: string;
}
