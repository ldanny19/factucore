export interface EmpresaReporte {
  id: number;
  ruc: string;
  razonSocial: string;
}

export type GrupoEstadoReporte = "" | "PENDIENTES" | "FALLIDOS";

export interface FiltrosReporteComprobantes {
  idEmpresa: number;
  fechaDesde: string;
  fechaHasta: string;
  idEstablecimiento?: number;
  idPuntoEmision?: number;
  codigoDocumento: string;
  estadoProceso: string;
  estadoSri: string;
  busqueda: string;
  grupoEstado: GrupoEstadoReporte;
}

export interface ComprobanteReporte {
  id: number;
  idTransaccion: string;
  idEmpresa: number;
  razonSocialEmisor: string;
  rucEmisor: string;
  idEstablecimiento: number;
  idPuntoEmision: number;
  codigoDocumento: string;
  tipoDocumento: string;
  numeroComprobante: string;
  claveAcceso: string;
  fechaEmision: string;
  identificacionReceptor: string | null;
  razonSocialReceptor: string | null;
  estadoProceso: string;
  estadoSri: string | null;
  numeroAutorizacion: string | null;
  fechaAutorizacion: string | null;
  codigoError: string | null;
  mensajeError: string | null;
}

export interface EnvioNotificacion {
  id: number;
  idEnvioOrigen: number | null;
  destinatario: string;
  estado: "PENDIENTE" | "EN_PROCESO" | "ENVIADA" | "FALLIDA";
  numeroIntentos: number;
  usuarioSolicitud: string | null;
  fechaRecepcion: string;
  fechaEnvio: string | null;
}

export interface PaginaComprobantes {
  resultados: ComprobanteReporte[];
  pagina: number;
  tamano: number;
  totalElementos: number;
  totalPaginas: number;
}

export interface ResumenComprobantes {
  total: number;
  autorizados: number;
  pendientes: number;
  noAutorizados: number;
  conError: number;
  otros: number;
  porDia: ComprobanteReporteDia[];
}

export interface ComprobanteReporteDia {
  fecha: string;
  total: number;
  autorizados: number;
  pendientes: number;
  noAutorizados: number;
  conError: number;
  otros: number;
}

export interface EventoAuditoriaComprobante {
  etapa: string;
  resultado: string;
  estadoAnterior: string | null;
  estadoNuevo: string | null;
  codigoError: string | null;
  mensajeError: string | null;
  fechaInicio: string;
  fechaFin: string | null;
  intento: number;
}

export interface DetalleComprobanteReporte {
  comprobante: ComprobanteReporte;
  auditoria: EventoAuditoriaComprobante[];
}
