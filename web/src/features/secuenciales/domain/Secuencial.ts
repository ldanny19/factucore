export interface Secuencial {
  id: number;
  idPuntoEmision: number;
  codigoDocumento: string;
  ultimoSecuencial: number;
  estadoRegistro: string;
  usuarioCreacion?: string | null;
  usuarioModificacion?: string | null;
  fechaCreacion?: string | null;
  fechaModificacion?: string | null;
  observacion?: string | null;
}

export interface SecuencialDatos {
  idEmpresa: number;
  idPuntoEmision: number;
  codigoDocumento: string;
  ultimoSecuencial: number;
}
