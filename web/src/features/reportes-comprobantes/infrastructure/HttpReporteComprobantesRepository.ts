import { getRuntimeConfig } from "../../../config/runtime-config";
import { apiRequest, apiRequestRaw } from "../../../shared/api/api-client";
import type {
  DetalleComprobanteReporte,
  EmpresaReporte,
  FiltrosReporteComprobantes,
  PaginaComprobantes,
  ResumenComprobantes,
} from "../domain/ReporteComprobantes";

export interface ReporteComprobantesRepository {
  listarEmpresas(): Promise<EmpresaReporte[]>;
  buscar(filtros: FiltrosReporteComprobantes, pagina: number, tamano: number): Promise<PaginaComprobantes>;
  resumir(filtros: FiltrosReporteComprobantes): Promise<ResumenComprobantes>;
  obtenerDetalle(idEmpresa: number, id: number): Promise<DetalleComprobanteReporte>;
  exportarCsv(filtros: FiltrosReporteComprobantes): Promise<Blob>;
  reprocesar(id: number): Promise<void>;
}

export function createHttpReporteComprobantesRepository(): ReporteComprobantesRepository {
  const comprobantesPath = getRuntimeConfig().API_COMPROBANTES_PATH.replace(/^\/+|\/+$/g, "");
  const reportesPath = `${comprobantesPath}/reportes`;
  const parametros = (filtros: FiltrosReporteComprobantes) => ({
    ...filtros,
    idEstablecimiento: filtros.idEstablecimiento || undefined,
    idPuntoEmision: filtros.idPuntoEmision || undefined,
    codigoDocumento: filtros.codigoDocumento || undefined,
    estadoProceso: filtros.estadoProceso || undefined,
    estadoSri: filtros.estadoSri || undefined,
    busqueda: filtros.busqueda || undefined,
    grupoEstado: filtros.grupoEstado || undefined,
  });

  return {
    listarEmpresas: () => apiRequest<EmpresaReporte[]>({ method: "GET", url: `${reportesPath}/empresas` }),
    buscar: (filtros, pagina, tamano) =>
      apiRequest<PaginaComprobantes>({
        method: "GET",
        url: reportesPath,
        params: { ...parametros(filtros), pagina, tamano },
      }),
    resumir: (filtros) =>
      apiRequest<ResumenComprobantes>({
        method: "GET",
        url: `${reportesPath}/resumen`,
        params: parametros(filtros),
      }),
    obtenerDetalle: (idEmpresa, id) =>
      apiRequest<DetalleComprobanteReporte>({
        method: "GET",
        url: `${reportesPath}/${id}`,
        params: { idEmpresa },
      }),
    exportarCsv: (filtros) =>
      apiRequestRaw<Blob>({
        method: "GET",
        url: `${reportesPath}/exportacion.csv`,
        params: parametros(filtros),
        responseType: "blob",
      }),
    reprocesar: async (id) => {
      await apiRequestRaw<unknown>({
        method: "POST",
        url: `${comprobantesPath}/${id}/reprocesar`,
      });
    },
  };
}
