export interface ConfiguracionEmpresa {
  id: number;
  idEmpresa: number;
  clave: string;
  valor: string | null;
  tipoDato: string;
  fechaVigenciaDesde: string;
  fechaVigenciaHasta: string | null;
  estadoRegistro: string;
  usuarioCreacion?: string | null;
  usuarioModificacion?: string | null;
  fechaCreacion?: string | null;
  fechaModificacion?: string | null;
  observacion?: string | null;
}

export interface ConfiguracionEmpresaDatos {
  idEmpresa: number;
  clave: string;
  valor: string | null;
  tipoDato: string;
  fechaVigenciaDesde: string;
  fechaVigenciaHasta: string | null;
}
