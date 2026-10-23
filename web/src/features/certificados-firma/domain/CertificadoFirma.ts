export interface CertificadoFirma {
  id: number;
  idEmpresa: number;
  nombreArchivo: string | null;
  rutaCertificado: string;
  fechaInicio: string | null;
  fechaFin: string | null;
  estadoRegistro: string;
  usuarioCreacion?: string | null;
  usuarioModificacion?: string | null;
  fechaCreacion?: string | null;
  fechaModificacion?: string | null;
  observacion?: string | null;
}

export interface CertificadoFirmaDatos {
  idEmpresa: number;
  nombreArchivo: string;
  rutaCertificado: string;
  fechaInicio: string;
  fechaFin: string | null;
}
