export interface TarifaTributaria {
  id: number;
  idCatalogoItem: number;
  codigoImpuesto: string;
  codigoPorcentaje: string;
  tarifa: number;
  fechaVigenciaDesde: string;
  fechaVigenciaHasta: string | null;
  estadoRegistro: string;
  observacion: string | null;
  usuarioCreacion?: string | null;
  usuarioModificacion?: string | null;
  fechaCreacion?: string | null;
  fechaModificacion?: string | null;
}

export interface TarifaTributariaDatos {
  idCatalogoItem: number;
  codigoImpuesto: string;
  codigoPorcentaje: string;
  tarifa: number;
  fechaVigenciaDesde: string;
  fechaVigenciaHasta: string | null;
  observacion: string;
}

export interface TarifaTributariaFiltro {
  codigoImpuesto?: string;
  codigoPorcentaje?: string;
}
