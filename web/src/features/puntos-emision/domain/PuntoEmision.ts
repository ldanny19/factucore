export interface PuntoEmision {
  id: number;
  idEstablecimiento: number;
  codigo: string;
  nombre: string;
  estadoRegistro: string;
  usuarioCreacion?: string | null;
  usuarioModificacion?: string | null;
  fechaCreacion?: string | null;
  fechaModificacion?: string | null;
  observacion?: string | null;
}

export interface PuntoEmisionDatos {
  idEmpresa: number;
  idEstablecimiento: number;
  codigo: string;
  nombre: string;
}
