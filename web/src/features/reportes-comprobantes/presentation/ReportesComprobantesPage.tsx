import { useCallback, useEffect, useMemo, useRef, useState, type FormEvent } from "react";
import {
  Alert,
  Box,
  Button,
  Chip,
  CircularProgress,
  Dialog,
  DialogContent,
  DialogTitle,
  Divider,
  Grid,
  MenuItem,
  Paper,
  Stack,
  Tab,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TablePagination,
  TableRow,
  Tabs,
  TextField,
  ToggleButton,
  ToggleButtonGroup,
  Typography,
} from "@mui/material";
import DownloadOutlinedIcon from "@mui/icons-material/DownloadOutlined";
import EmailOutlinedIcon from "@mui/icons-material/EmailOutlined";
import PictureAsPdfOutlinedIcon from "@mui/icons-material/PictureAsPdfOutlined";
import RefreshOutlinedIcon from "@mui/icons-material/RefreshOutlined";
import SearchOutlinedIcon from "@mui/icons-material/SearchOutlined";
import TimelineOutlinedIcon from "@mui/icons-material/TimelineOutlined";
import { Link as RouterLink, useLocation } from "react-router-dom";
import { MessageCodes } from "../../../shared/messages/MessageCodes";
import { resolveMessage } from "../../../shared/messages/MessageResolver";
import { canReprocessComprobante, getAccessContext } from "../../../shared/security/authorization";
import type {
  ComprobanteReporteDia,
  ComprobanteReporte,
  DetalleComprobanteReporte,
  EnvioNotificacion,
  EmpresaReporte,
  FiltrosReporteComprobantes,
  PaginaComprobantes,
  ResumenComprobantes,
} from "../domain/ReporteComprobantes";
import { createHttpReporteComprobantesRepository } from "../infrastructure/HttpReporteComprobantesRepository";
import "../../../styles/app.css";

type VistaReporte = "resumen" | "consulta" | "seguimiento" | "pendientes-fallidos" | "busqueda";
type TipoGrafico = "barras" | "dispersion" | "lineas";
type FiltrosFormulario = Omit<FiltrosReporteComprobantes, "idEmpresa"> & { idEmpresa: number | "" };

const PAGINAS_REPORTE: Array<{
  vista: VistaReporte;
  ruta: string;
  codigo: keyof typeof MessageCodes;
}> = [
  {
    vista: "resumen",
    ruta: "/app/reportes-comprobantes/resumen",
    codigo: "REPORTE_COMPROBANTES_SUMMARY",
  },
  {
    vista: "consulta",
    ruta: "/app/reportes-comprobantes/consulta",
    codigo: "REPORTE_COMPROBANTES_CONSULT",
  },
  {
    vista: "seguimiento",
    ruta: "/app/reportes-comprobantes/seguimiento",
    codigo: "REPORTE_COMPROBANTES_TRACKING",
  },
  {
    vista: "pendientes-fallidos",
    ruta: "/app/reportes-comprobantes/pendientes-fallidos",
    codigo: "REPORTE_COMPROBANTES_PENDING_FAILED",
  },
  {
    vista: "busqueda",
    ruta: "/app/reportes-comprobantes/busqueda",
    codigo: "REPORTE_COMPROBANTES_SEARCH",
  },
];

const ESTADOS_PROCESO = [
  "RECIBIDO",
  "VALIDADO",
  "SECUENCIAL_ASIGNADO",
  "CLAVE_ACCESO_GENERADA",
  "XML_GENERADO",
  "XSD_VALIDADO",
  "FIRMADO",
  "ENVIADO_SRI",
  "AUTORIZACION_PENDIENTE",
  "AUTORIZACION_SRI",
  "RIDE_GENERADO",
  "NOTIFICACION_PUBLICADA",
];

const ESTADOS_SRI: Array<{ valor: string; codigo: keyof typeof MessageCodes }> = [
  { valor: "AUTORIZADO", codigo: "REPORTE_COMPROBANTES_SRI_AUTHORIZED" },
  { valor: "EN_PROCESO", codigo: "REPORTE_COMPROBANTES_SRI_IN_PROGRESS" },
  { valor: "NO_AUTORIZADO", codigo: "REPORTE_COMPROBANTES_SRI_NOT_AUTHORIZED" },
  { valor: "RECHAZADO", codigo: "REPORTE_COMPROBANTES_SRI_REJECTED" },
];

const TAMANO_PAGINA_PDF = 100;
const MAXIMO_PAGINAS_EXPORTABLES = 100;

function fechaLocal(valor: Date): string {
  const pad = (parte: number) => String(parte).padStart(2, "0");
  return `${valor.getFullYear()}-${pad(valor.getMonth() + 1)}-${pad(valor.getDate())}`;
}

function filtrosIniciales(vista: VistaReporte, idEmpresa: number | "" = ""): FiltrosFormulario {
  const hoy = new Date();
  return {
    idEmpresa,
    fechaDesde: fechaLocal(hoy),
    fechaHasta: fechaLocal(hoy),
    codigoDocumento: "",
    estadoProceso: "",
    estadoSri: "",
    busqueda: "",
    grupoEstado: vista === "pendientes-fallidos" ? "PENDIENTES" : "",
  };
}

function vistaDesdeRuta(pathname: string): VistaReporte {
  return PAGINAS_REPORTE.find(({ ruta }) => ruta === pathname)?.vista ?? "resumen";
}

function mensajeError(error: unknown): string {
  return error instanceof Error && error.message.trim()
    ? error.message
    : resolveMessage(MessageCodes.COMMON_OPERATION_FAILED);
}

function fechaVisible(valor: string | null | undefined): string {
  if (!valor) return "—";
  const fecha = new Date(valor.length === 10 ? `${valor}T00:00:00` : valor);
  return Number.isNaN(fecha.getTime()) ? valor : fecha.toLocaleString("es-EC");
}

function generarIdSolicitud(): string {
  const bytes = crypto.getRandomValues(new Uint8Array(16));
  bytes[6] = (bytes[6] & 0x0f) | 0x40;
  bytes[8] = (bytes[8] & 0x3f) | 0x80;
  const hexadecimal = Array.from(bytes, (byte) => byte.toString(16).padStart(2, "0")).join("");
  return `${hexadecimal.slice(0, 8)}-${hexadecimal.slice(8, 12)}-${hexadecimal.slice(12, 16)}-${hexadecimal.slice(16, 20)}-${hexadecimal.slice(20)}`;
}

function diasResumen(
  porDia: ComprobanteReporteDia[],
  fechaDesde: string,
  fechaHasta: string,
): ComprobanteReporteDia[] {
  const porFecha = new Map(porDia.map((dia) => [dia.fecha, dia]));
  const [anioInicio, mesInicio, diaInicio] = fechaDesde.split("-").map(Number);
  const [anioFin, mesFin, diaFin] = fechaHasta.split("-").map(Number);
  const actual = new Date(anioInicio, mesInicio - 1, diaInicio);
  const final = new Date(anioFin, mesFin - 1, diaFin);
  const dias: ComprobanteReporteDia[] = [];

  while (actual <= final) {
    const fecha = fechaLocal(actual);
    dias.push(
      porFecha.get(fecha) ?? {
        fecha,
        total: 0,
        autorizados: 0,
        pendientes: 0,
        noAutorizados: 0,
        conError: 0,
        otros: 0,
      },
    );
    actual.setDate(actual.getDate() + 1);
  }
  return dias;
}

function crearFiltrosAplicables(formulario: FiltrosFormulario): FiltrosReporteComprobantes | null {
  if (formulario.idEmpresa === "") return null;
  return { ...formulario, idEmpresa: formulario.idEmpresa };
}

function esReprocesable(comprobante: ComprobanteReporte): boolean {
  return Boolean(comprobante.codigoError) || comprobante.estadoProceso === "AUTORIZACION_PENDIENTE";
}

function colorEstado(comprobante: ComprobanteReporte): "default" | "success" | "warning" | "error" {
  const estadoSri = comprobante.estadoSri?.toUpperCase().replace(/[\s-]+/g, "_");
  if (
    comprobante.codigoError ||
    ["NO_AUTORIZADO", "NOAUTORIZADO", "RECHAZADO"].includes(estadoSri ?? "")
  ) {
    return "error";
  }
  if (estadoSri === "AUTORIZADO") return "success";
  if (comprobante.estadoProceso === "AUTORIZACION_PENDIENTE") return "warning";
  return "default";
}

export default function ReportesComprobantesPage() {
  const location = useLocation();
  const vista = vistaDesdeRuta(location.pathname);
  const repositorio = useMemo(() => createHttpReporteComprobantesRepository(), []);
  const acceso = useMemo(() => getAccessContext(), []);
  const puedeReprocesar = canReprocessComprobante(acceso);
  const puedeConsultarCorreo = acceso.isTokenValid &&
    acceso.roles.some((role) => role === "SUPERADMIN" || role === "ADMIN");
  const puedeReenviarCorreo = puedeConsultarCorreo;
  const [empresas, setEmpresas] = useState<EmpresaReporte[]>([]);
  const [formulario, setFormulario] = useState<FiltrosFormulario>(() => filtrosIniciales(vista));
  const [filtrosAplicados, setFiltrosAplicados] = useState<FiltrosReporteComprobantes | null>(null);
  const [pagina, setPagina] = useState<PaginaComprobantes | null>(null);
  const [resumen, setResumen] = useState<ResumenComprobantes | null>(null);
  const [tipoGrafico, setTipoGrafico] = useState<TipoGrafico>("barras");
  const [numeroPagina, setNumeroPagina] = useState(0);
  const [tamanoPagina, setTamanoPagina] = useState(25);
  const [cargandoEmpresas, setCargandoEmpresas] = useState(true);
  const [cargando, setCargando] = useState(false);
  const [exportando, setExportando] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [aviso, setAviso] = useState<string | null>(null);
  const [detalle, setDetalle] = useState<DetalleComprobanteReporte | null>(null);
  const [cargandoDetalle, setCargandoDetalle] = useState(false);
  const [comprobanteCorreo, setComprobanteCorreo] = useState<ComprobanteReporte | null>(null);
  const [enviosCorreo, setEnviosCorreo] = useState<EnvioNotificacion[]>([]);
  const [cargandoCorreo, setCargandoCorreo] = useState(false);
  const [errorCorreo, setErrorCorreo] = useState<string | null>(null);
  const [avisoCorreo, setAvisoCorreo] = useState<string | null>(null);
  const [reenvioEnCurso, setReenvioEnCurso] = useState(false);
  const [confirmarReenvio, setConfirmarReenvio] = useState(false);
  const [filasImpresion, setFilasImpresion] = useState<ComprobanteReporte[] | null>(null);
  const [actualizacion, setActualizacion] = useState(0);
  const solicitudDetalle = useRef(0);
  const solicitudCorreo = useRef(0);
  const claveSolicitudReenvio = useRef<string | null>(null);
  const idEmpresaActual = useRef<number | "">("");
  const mostrarHistograma = Boolean(
    vista === "resumen" &&
      resumen &&
      filtrosAplicados?.fechaDesde &&
      filtrosAplicados.fechaHasta &&
      filtrosAplicados.fechaDesde < filtrosAplicados.fechaHasta,
  );
  const datosHistograma =
    resumen && filtrosAplicados?.fechaDesde && filtrosAplicados.fechaHasta &&
    filtrosAplicados.fechaDesde < filtrosAplicados.fechaHasta
      ? diasResumen(resumen.porDia, filtrosAplicados.fechaDesde, filtrosAplicados.fechaHasta)
      : [];
  const maximoDiario = datosHistograma.reduce((maximo, dia) => Math.max(maximo, dia.total), 1);
  const gruposHistograma = [
    {
      codigo: MessageCodes.REPORTE_COMPROBANTES_AUTHORIZED,
      campo: "autorizados" as const,
      color: "#2e7d32",
    },
    {
      codigo: MessageCodes.REPORTE_COMPROBANTES_PENDING,
      campo: "pendientes" as const,
      color: "#edb800",
    },
    {
      codigo: MessageCodes.REPORTE_COMPROBANTES_NOT_AUTHORIZED,
      campo: "noAutorizados" as const,
      color: "#ed6c02",
    },
    {
      codigo: MessageCodes.REPORTE_COMPROBANTES_WITH_ERROR,
      campo: "conError" as const,
      color: "#d32f2f",
    },
    {
      codigo: MessageCodes.REPORTE_COMPROBANTES_OTHER,
      campo: "otros" as const,
      color: "#546e7a",
    },
  ];
  const anchoGrafico = Math.max(600, datosHistograma.length * 52 + 72);
  const altoGrafico = 220;
  const margenGrafico = { arriba: 12, derecha: 12, abajo: 36, izquierda: 42 };
  const altoTrazado = altoGrafico - margenGrafico.arriba - margenGrafico.abajo;
  const maximoTrazado = datosHistograma.reduce((maximo, dia) => Math.max(maximo, dia.total), 1);
  const marcasEjeY =
    maximoTrazado < 4
      ? Array.from({ length: maximoTrazado + 1 }, (_value, indice) => maximoTrazado - indice)
      : Array.from({ length: 5 }, (_value, indice) =>
          Math.round((maximoTrazado * (4 - indice)) / 4),
        ).filter((valor, indice, valores) => valores.indexOf(valor) === indice);
  const puntoGrafico = (indiceDia: number, valor: number) => ({
    x:
      margenGrafico.izquierda +
      (datosHistograma.length <= 1
        ? (anchoGrafico - margenGrafico.izquierda - margenGrafico.derecha) / 2
        : (indiceDia / (datosHistograma.length - 1)) *
          (anchoGrafico - margenGrafico.izquierda - margenGrafico.derecha)),
    y: margenGrafico.arriba + altoTrazado * (1 - valor / maximoTrazado),
  });

  const cargarEmpresas = useCallback(async () => {
    setCargandoEmpresas(true);
    setError(null);
    try {
      setEmpresas(await repositorio.listarEmpresas());
    } catch (exception) {
      setError(mensajeError(exception));
    } finally {
      setCargandoEmpresas(false);
    }
  }, [repositorio]);

  useEffect(() => {
    void cargarEmpresas();
  }, [cargarEmpresas]);

  useEffect(() => {
    solicitudDetalle.current += 1;
    const filtrosInicialesVista = filtrosIniciales(vista, idEmpresaActual.current);
    setFormulario(filtrosInicialesVista);
    setFiltrosAplicados(crearFiltrosAplicables(filtrosInicialesVista));
    setNumeroPagina(0);
    setPagina(null);
    setResumen(null);
    setDetalle(null);
    setCargandoDetalle(false);
    setError(null);
    setAviso(null);
  }, [vista]);

  useEffect(() => {
    if (!filtrosAplicados) {
      setPagina(null);
      setResumen(null);
      setCargando(false);
      return;
    }
    let vigente = true;
    const cargarDatos = async () => {
      setCargando(true);
      setError(null);
      try {
        if (vista === "resumen") {
          const totales = await repositorio.resumir(filtrosAplicados);
          if (!vigente) return;
          setPagina(null);
          setResumen(totales);
        } else {
          const resultados = await repositorio.buscar(filtrosAplicados, numeroPagina, tamanoPagina);
          if (!vigente) return;
          setPagina(resultados);
          setResumen(null);
        }
      } catch (exception) {
        if (!vigente) return;
        setError(mensajeError(exception));
        setPagina(null);
        setResumen(null);
      } finally {
        if (vigente) setCargando(false);
      }
    };
    void cargarDatos();
    return () => {
      vigente = false;
    };
  }, [filtrosAplicados, numeroPagina, repositorio, tamanoPagina, vista, actualizacion]);

  useEffect(() => {
    if (!filasImpresion) return;
    const temporizador = window.setTimeout(() => {
      window.print();
      setFilasImpresion(null);
    }, 150);
    return () => window.clearTimeout(temporizador);
  }, [filasImpresion]);

  const cambiarCampo = (campo: keyof FiltrosFormulario, valor: string) => {
    const nuevoFormulario: FiltrosFormulario = {
      ...formulario,
      [campo]: campo === "idEmpresa" ? (valor ? Number(valor) : "") : valor,
    };
    setFormulario(nuevoFormulario);
    if (campo === "idEmpresa") {
      idEmpresaActual.current = nuevoFormulario.idEmpresa;
      solicitudDetalle.current += 1;
      setFiltrosAplicados(crearFiltrosAplicables(nuevoFormulario));
      setPagina(null);
      setResumen(null);
      setDetalle(null);
      setCargandoDetalle(false);
      setNumeroPagina(0);
    }
    setError(null);
    setAviso(null);
  };

  const aplicarFiltros = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError(null);
    setAviso(null);
    if (formulario.idEmpresa === "") {
      setError(resolveMessage(MessageCodes.REPORTE_COMPROBANTES_COMPANY_REQUIRED));
      return;
    }
    if (
      formulario.fechaDesde &&
      formulario.fechaHasta &&
      formulario.fechaDesde > formulario.fechaHasta
    ) {
      setError(resolveMessage(MessageCodes.REPORTE_COMPROBANTES_DATE_RANGE_INVALID));
      return;
    }
    solicitudDetalle.current += 1;
    setDetalle(null);
    setCargandoDetalle(false);
    setNumeroPagina(0);
    setFiltrosAplicados(crearFiltrosAplicables(formulario));
  };

  const limpiarFiltros = () => {
    solicitudDetalle.current += 1;
    const filtrosPredeterminados = filtrosIniciales(vista, formulario.idEmpresa);
    setFormulario(filtrosPredeterminados);
    setFiltrosAplicados(crearFiltrosAplicables(filtrosPredeterminados));
    setPagina(null);
    setResumen(null);
    setDetalle(null);
    setCargandoDetalle(false);
    setNumeroPagina(0);
  };

  const abrirDetalle = async (comprobante: ComprobanteReporte) => {
    if (!filtrosAplicados) return;
    const solicitudActual = ++solicitudDetalle.current;
    const idEmpresa = filtrosAplicados.idEmpresa;
    setCargandoDetalle(true);
    setDetalle(null);
    setError(null);
    try {
      const resultado = await repositorio.obtenerDetalle(idEmpresa, comprobante.id);
      if (solicitudActual === solicitudDetalle.current) setDetalle(resultado);
    } catch (exception) {
      if (solicitudActual === solicitudDetalle.current) setError(mensajeError(exception));
    } finally {
      if (solicitudActual === solicitudDetalle.current) setCargandoDetalle(false);
    }
  };

  const cerrarDetalle = () => {
    solicitudDetalle.current += 1;
    setDetalle(null);
    setCargandoDetalle(false);
  };

  const cargarEnviosCorreo = async (
    idTransaccion: string,
    solicitud: number,
  ): Promise<EnvioNotificacion[] | null> => {
    setCargandoCorreo(true);
    setErrorCorreo(null);
    try {
      const resultado = await repositorio.consultarEnvios(idTransaccion);
      if (solicitud === solicitudCorreo.current) {
        setEnviosCorreo(resultado);
        return resultado;
      }
    } catch (exception) {
      if (solicitud === solicitudCorreo.current) setErrorCorreo(mensajeError(exception));
    } finally {
      if (solicitud === solicitudCorreo.current) setCargandoCorreo(false);
    }
    return null;
  };

  const abrirHistorialCorreo = async (
    comprobante: ComprobanteReporte,
    confirmarReenvio = false,
  ) => {
    const solicitud = ++solicitudCorreo.current;
    setComprobanteCorreo(comprobante);
    setEnviosCorreo([]);
    setAvisoCorreo(null);
    setErrorCorreo(null);
    setConfirmarReenvio(false);
    const envios = await cargarEnviosCorreo(comprobante.idTransaccion, solicitud);
    const ultimoEnvio = envios?.[0];
    if (
      confirmarReenvio &&
      solicitud === solicitudCorreo.current &&
      ultimoEnvio &&
      !["PENDIENTE", "EN_PROCESO"].includes(ultimoEnvio.estado)
    ) {
      setConfirmarReenvio(true);
    }
  };

  const cerrarHistorialCorreo = () => {
    solicitudCorreo.current += 1;
    setComprobanteCorreo(null);
    setCargandoCorreo(false);
    setConfirmarReenvio(false);
  };

  const reenviarCorreo = async () => {
    if (!comprobanteCorreo || !puedeReenviarCorreo) return;
    setReenvioEnCurso(true);
    setErrorCorreo(null);
    setAvisoCorreo(null);
    try {
      const idSolicitud = claveSolicitudReenvio.current ?? generarIdSolicitud();
      claveSolicitudReenvio.current = idSolicitud;
      const envio = await repositorio.solicitarReenvio(comprobanteCorreo.idTransaccion, idSolicitud);
      claveSolicitudReenvio.current = null;
      setEnviosCorreo((anteriores) => [envio, ...anteriores].slice(0, 20));
      setAvisoCorreo(resolveMessage(MessageCodes.REPORTE_COMPROBANTES_EMAIL_RESEND_SUCCESS));
      setConfirmarReenvio(false);
    } catch (exception) {
      setErrorCorreo(mensajeError(exception));
    } finally {
      setReenvioEnCurso(false);
    }
  };

  const verRide = async (comprobante: ComprobanteReporte) => {
    setError(null);
    try {
      const archivo = await repositorio.obtenerRide(comprobante.idEmpresa, comprobante.id);
      const url = URL.createObjectURL(archivo);
      const enlace = document.createElement("a");
      enlace.href = url;
      enlace.target = "_blank";
      enlace.rel = "noopener noreferrer";
      document.body.appendChild(enlace);
      enlace.click();
      enlace.remove();
      window.setTimeout(() => URL.revokeObjectURL(url), 60_000);
    } catch (exception) {
      setError(mensajeError(exception));
    }
  };

  const exportarCsv = async () => {
    if (!filtrosAplicados) return;
    setExportando(true);
    setError(null);
    try {
      const archivo = await repositorio.exportarCsv(filtrosAplicados);
      const url = URL.createObjectURL(archivo);
      const enlace = document.createElement("a");
      enlace.href = url;
      enlace.download = "comprobantes.csv";
      document.body.appendChild(enlace);
      enlace.click();
      enlace.remove();
      window.setTimeout(() => URL.revokeObjectURL(url), 1000);
    } catch (exception) {
      setError(mensajeError(exception));
    } finally {
      setExportando(false);
    }
  };

  const exportarPdf = async () => {
    if (!filtrosAplicados) return;
    setExportando(true);
    setError(null);
    try {
      const primeraPagina = await repositorio.buscar(filtrosAplicados, 0, TAMANO_PAGINA_PDF);
      if (primeraPagina.totalPaginas > MAXIMO_PAGINAS_EXPORTABLES) {
        throw new Error(resolveMessage(MessageCodes.API_HTTP_PAYLOAD_TOO_LARGE));
      }
      const filas = [...primeraPagina.resultados];
      for (let inicio = 1; inicio < primeraPagina.totalPaginas; inicio += 4) {
        const solicitudes = Array.from(
          { length: Math.min(4, primeraPagina.totalPaginas - inicio) },
          (_, indice) => repositorio.buscar(filtrosAplicados, inicio + indice, TAMANO_PAGINA_PDF),
        );
        const siguientes = await Promise.all(solicitudes);
        filas.push(...siguientes.flatMap((resultado) => resultado.resultados));
      }
      setFilasImpresion(filas);
    } catch (exception) {
      setError(mensajeError(exception));
    } finally {
      setExportando(false);
    }
  };

  const reprocesar = async (comprobante: ComprobanteReporte) => {
    setError(null);
    setAviso(null);
    try {
      await repositorio.reprocesar(comprobante.id);
      setAviso(resolveMessage(MessageCodes.REPORTE_COMPROBANTES_REPROCESS_SUCCESS));
      setActualizacion((valor) => valor + 1);
      if (detalle?.comprobante.id === comprobante.id) {
        setDetalle(await repositorio.obtenerDetalle(comprobante.idEmpresa, comprobante.id));
      }
    } catch (exception) {
      setError(mensajeError(exception));
    }
  };

  const empresaSeleccionada = empresas.find(({ id }) => id === formulario.idEmpresa);
  const filas = pagina?.resultados ?? [];
  const codigoDescripcionVista: Record<VistaReporte, keyof typeof MessageCodes> = {
    resumen: "REPORTE_COMPROBANTES_VIEW_SUMMARY_HELP",
    consulta: "REPORTE_COMPROBANTES_VIEW_CONSULT_HELP",
    seguimiento: "REPORTE_COMPROBANTES_VIEW_TRACKING_HELP",
    "pendientes-fallidos": "REPORTE_COMPROBANTES_VIEW_PENDING_HELP",
    busqueda: "REPORTE_COMPROBANTES_VIEW_SEARCH_HELP",
  };
  const paginaActiva = PAGINAS_REPORTE.find(({ vista: vistaPagina }) => vistaPagina === vista);
  const mostrarExportaciones = vista !== "resumen" && vista !== "seguimiento";
  const grupoEstadoVisible = filtrosAplicados?.grupoEstado ?? formulario.grupoEstado;

  return (
    <Box className="comprobantes-report-page">
      <Box className="comprobantes-report-screen">
        <Stack
          direction={{ xs: "column", sm: "row" }}
          alignItems={{ xs: "flex-start", sm: "center" }}
          justifyContent="space-between"
          gap={2}
          sx={{ mb: 2 }}
        >
          <Box>
            <Typography className="page-title" component="h1" variant="h4">
              {paginaActiva ? resolveMessage(MessageCodes[paginaActiva.codigo]) : ""}
            </Typography>
            <Typography color="text.secondary">
              {resolveMessage(MessageCodes[codigoDescripcionVista[vista]])}
            </Typography>
          </Box>
          <Stack direction="row" flexWrap="wrap" gap={1}>
            {mostrarExportaciones && (
              <>
                <Button
                  variant="outlined"
                  startIcon={<DownloadOutlinedIcon />}
                  onClick={() => void exportarCsv()}
                  disabled={
                    !filtrosAplicados ||
                    cargando ||
                    exportando ||
                    (pagina?.totalElementos ?? 0) === 0
                  }
                >
                  {resolveMessage(MessageCodes.REPORTE_COMPROBANTES_EXPORT_CSV)}
                </Button>
                <Button
                  variant="outlined"
                  startIcon={<PictureAsPdfOutlinedIcon />}
                  onClick={() => void exportarPdf()}
                  disabled={
                    !filtrosAplicados ||
                    cargando ||
                    exportando ||
                    (pagina?.totalElementos ?? 0) === 0
                  }
                >
                  {resolveMessage(MessageCodes.REPORTE_COMPROBANTES_EXPORT_PDF)}
                </Button>
              </>
            )}
            <Button
              variant="text"
              startIcon={<RefreshOutlinedIcon />}
              onClick={() => setActualizacion((valor) => valor + 1)}
              disabled={!filtrosAplicados || cargando || exportando}
            >
              {resolveMessage(MessageCodes.REPORTE_COMPROBANTES_REFRESH)}
            </Button>
          </Stack>
        </Stack>

        <Tabs
          value={vista}
          variant="scrollable"
          scrollButtons="auto"
          aria-label={resolveMessage(MessageCodes.REPORTE_COMPROBANTES_TITLE)}
          sx={{ mb: 2, borderBottom: 1, borderColor: "divider" }}
        >
          {PAGINAS_REPORTE.map((elemento) => (
            <Tab
              key={elemento.vista}
              component={RouterLink}
              to={elemento.ruta}
              value={elemento.vista}
              label={resolveMessage(MessageCodes[elemento.codigo])}
            />
          ))}
        </Tabs>

        {error && (
          <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>
            {error}
          </Alert>
        )}
        {aviso && (
          <Alert severity="success" sx={{ mb: 2 }} onClose={() => setAviso(null)}>
            {aviso}
          </Alert>
        )}

        <Paper variant="outlined" sx={{ p: 2, mb: 2 }}>
          <Box component="form" onSubmit={aplicarFiltros}>
            <Grid container spacing={1.5} alignItems="center">
              <Grid size={{ xs: 12, md: vista === "resumen" ? 4 : 3 }}>
                <TextField
                  select
                  fullWidth
                  required
                  label={resolveMessage(MessageCodes.REPORTE_COMPROBANTES_COMPANY)}
                  value={formulario.idEmpresa}
                  onChange={(event) => cambiarCampo("idEmpresa", event.target.value)}
                  disabled={cargandoEmpresas || exportando}
                >
                  <MenuItem value="">{resolveMessage(MessageCodes.COMMON_SELECT_OPTION)}</MenuItem>
                  {empresas.map((empresa) => (
                    <MenuItem key={empresa.id} value={empresa.id}>
                      {empresa.ruc} — {empresa.razonSocial}
                    </MenuItem>
                  ))}
                </TextField>
              </Grid>
              <Grid size={{ xs: 12, sm: 6, md: 2 }}>
                <TextField
                  fullWidth
                  type="date"
                  label={resolveMessage(MessageCodes.REPORTE_COMPROBANTES_DATE_FROM)}
                  value={formulario.fechaDesde}
                  onChange={(event) => cambiarCampo("fechaDesde", event.target.value)}
                  slotProps={{ inputLabel: { shrink: true } }}
                />
              </Grid>
              <Grid size={{ xs: 12, sm: 6, md: 2 }}>
                <TextField
                  fullWidth
                  type="date"
                  label={resolveMessage(MessageCodes.REPORTE_COMPROBANTES_DATE_TO)}
                  value={formulario.fechaHasta}
                  onChange={(event) => cambiarCampo("fechaHasta", event.target.value)}
                  slotProps={{ inputLabel: { shrink: true } }}
                />
              </Grid>
              {(vista === "consulta" || vista === "pendientes-fallidos") && (
                <Grid size={{ xs: 12, sm: 6, md: 2 }}>
                  <TextField
                    fullWidth
                    label={resolveMessage(MessageCodes.REPORTE_COMPROBANTES_DOCUMENT_CODE)}
                    value={formulario.codigoDocumento}
                    onChange={(event) => cambiarCampo("codigoDocumento", event.target.value)}
                    inputProps={{ maxLength: 2 }}
                  />
                </Grid>
              )}
              {vista === "pendientes-fallidos" ? (
                <Grid size={{ xs: 12, sm: 6, md: 2 }}>
                  <TextField
                    select
                    fullWidth
                    label={resolveMessage(MessageCodes.REPORTE_COMPROBANTES_GROUP)}
                    value={formulario.grupoEstado}
                    onChange={(event) => cambiarCampo("grupoEstado", event.target.value)}
                  >
                    <MenuItem value="PENDIENTES">
                      {resolveMessage(MessageCodes.REPORTE_COMPROBANTES_PENDING)}
                    </MenuItem>
                    <MenuItem value="FALLIDOS">
                      {resolveMessage(MessageCodes.REPORTE_COMPROBANTES_FAILED)}
                    </MenuItem>
                  </TextField>
                </Grid>
              ) : null}
              {(vista === "consulta" ||
                vista === "seguimiento" ||
                vista === "pendientes-fallidos") && (
                <Grid size={{ xs: 12, sm: 6, md: 3 }}>
                  <TextField
                    select
                    fullWidth
                    label={resolveMessage(MessageCodes.REPORTE_COMPROBANTES_PROCESS_STATE)}
                    value={formulario.estadoProceso}
                    onChange={(event) => cambiarCampo("estadoProceso", event.target.value)}
                  >
                    <MenuItem value="">
                      {resolveMessage(MessageCodes.REPORTE_COMPROBANTES_ALL)}
                    </MenuItem>
                    {ESTADOS_PROCESO.map((estado) => (
                      <MenuItem key={estado} value={estado}>
                        {estado}
                      </MenuItem>
                    ))}
                  </TextField>
                </Grid>
              )}
              {(vista === "consulta" || vista === "pendientes-fallidos") && (
                <Grid size={{ xs: 12, sm: 6, md: 3 }}>
                  <TextField
                    select
                    fullWidth
                    label={resolveMessage(MessageCodes.REPORTE_COMPROBANTES_SRI_STATE)}
                    value={formulario.estadoSri}
                    onChange={(event) => cambiarCampo("estadoSri", event.target.value)}
                  >
                    <MenuItem value="">
                      {resolveMessage(MessageCodes.REPORTE_COMPROBANTES_ALL)}
                    </MenuItem>
                    {ESTADOS_SRI.map(({ valor, codigo }) => (
                      <MenuItem key={valor} value={valor}>
                        {resolveMessage(MessageCodes[codigo])}
                      </MenuItem>
                    ))}
                  </TextField>
                </Grid>
              )}
              {(vista === "consulta" ||
                vista === "pendientes-fallidos" ||
                vista === "busqueda") && (
                <Grid size={{ xs: 12, md: vista === "busqueda" ? 6 : 4 }}>
                  <TextField
                    fullWidth
                    label={resolveMessage(MessageCodes.REPORTE_COMPROBANTES_SEARCH_TERM)}
                    value={formulario.busqueda}
                    onChange={(event) => cambiarCampo("busqueda", event.target.value)}
                    inputProps={{ maxLength: 200 }}
                  />
                </Grid>
              )}
              <Grid size={{ xs: 12, md: 3 }}>
                <Stack direction="row" gap={1}>
                  <Button
                    type="submit"
                    variant="contained"
                    startIcon={<SearchOutlinedIcon />}
                    disabled={cargandoEmpresas || exportando}
                  >
                    {resolveMessage(MessageCodes.REPORTE_COMPROBANTES_APPLY_FILTERS)}
                  </Button>
                  <Button
                    type="button"
                    variant="text"
                    onClick={limpiarFiltros}
                    disabled={exportando}
                  >
                    {resolveMessage(MessageCodes.REPORTE_COMPROBANTES_CLEAR_FILTERS)}
                  </Button>
                </Stack>
              </Grid>
            </Grid>
          </Box>
        </Paper>

        {vista === "pendientes-fallidos" && (
          <Alert severity={grupoEstadoVisible === "FALLIDOS" ? "error" : "warning"} sx={{ mb: 2 }}>
            {resolveMessage(
              grupoEstadoVisible === "FALLIDOS"
                ? MessageCodes.REPORTE_COMPROBANTES_FAILED_HELP
                : MessageCodes.REPORTE_COMPROBANTES_PENDING_HELP,
            )}
          </Alert>
        )}

        {vista === "resumen" && cargando && (
          <Box display="flex" justifyContent="center" py={5}>
            <CircularProgress />
          </Box>
        )}

        {vista === "resumen" && resumen && (
          <Grid container spacing={1.5}>
            {[
              {
                codigo: MessageCodes.REPORTE_COMPROBANTES_TOTAL,
                total: resumen.total,
                fondo: "#eef4ff",
                borde: "#b6ccf5",
                texto: "#173765",
              },
              {
                codigo: MessageCodes.REPORTE_COMPROBANTES_AUTHORIZED,
                total: resumen.autorizados,
                fondo: "#e8f5e9",
                borde: "#a5d6a7",
                texto: "#1b5e20",
              },
              {
                codigo: MessageCodes.REPORTE_COMPROBANTES_PENDING,
                total: resumen.pendientes,
                fondo: "#fff8e1",
                borde: "#ffe082",
                texto: "#8d5d00",
              },
              {
                codigo: MessageCodes.REPORTE_COMPROBANTES_NOT_AUTHORIZED,
                total: resumen.noAutorizados,
                fondo: "#fff3e0",
                borde: "#ffcc80",
                texto: "#e65100",
              },
              {
                codigo: MessageCodes.REPORTE_COMPROBANTES_WITH_ERROR,
                total: resumen.conError,
                fondo: "#ffebee",
                borde: "#ef9a9a",
                texto: "#b71c1c",
              },
              {
                codigo: MessageCodes.REPORTE_COMPROBANTES_OTHER,
                total: resumen.otros,
                fondo: "#eceff1",
                borde: "#b0bec5",
                texto: "#37474f",
              },
            ].map(({ codigo, total, fondo, borde, texto }) => (
              <Grid key={String(codigo)} size={{ xs: 12, sm: 6, lg: 2 }}>
                <Paper
                  variant="outlined"
                  sx={{ p: 2, height: "100%", bgcolor: fondo, borderColor: borde }}
                >
                  <Typography variant="body2" sx={{ color: texto, opacity: 0.85 }}>
                    {resolveMessage(codigo)}
                  </Typography>
                  <Typography variant="h4" fontWeight={800} sx={{ color: texto }}>
                    {total}
                  </Typography>
                </Paper>
              </Grid>
            ))}
          </Grid>
        )}

        {mostrarHistograma && (
          <Paper variant="outlined" sx={{ mt: 2, p: { xs: 2, sm: 3 } }}>
            <Typography
              id="reporte-comprobantes-grafico-diario"
              variant="h6"
              component="h2"
              gutterBottom
            >
              {resolveMessage(MessageCodes.REPORTE_COMPROBANTES_DAILY_CHART_TITLE)}
            </Typography>
            <Stack
              direction={{ xs: "column", sm: "row" }}
              alignItems={{ xs: "stretch", sm: "center" }}
              spacing={1}
              sx={{ mb: 2 }}
            >
              <Typography id="reporte-comprobantes-tipo-grafico" variant="body2">
                {resolveMessage(MessageCodes.REPORTE_COMPROBANTES_DAILY_CHART_TYPE)}
              </Typography>
              <ToggleButtonGroup
                size="small"
                exclusive
                value={tipoGrafico}
                aria-labelledby="reporte-comprobantes-tipo-grafico"
                onChange={(_event, valor: TipoGrafico | null) => {
                  if (valor) setTipoGrafico(valor);
                }}
              >
                <ToggleButton value="barras">
                  {resolveMessage(MessageCodes.REPORTE_COMPROBANTES_DAILY_CHART_BARS)}
                </ToggleButton>
                <ToggleButton value="dispersion">
                  {resolveMessage(MessageCodes.REPORTE_COMPROBANTES_DAILY_CHART_SCATTER)}
                </ToggleButton>
                <ToggleButton value="lineas">
                  {resolveMessage(MessageCodes.REPORTE_COMPROBANTES_DAILY_CHART_LINES)}
                </ToggleButton>
              </ToggleButtonGroup>
            </Stack>
            <Stack direction="row" flexWrap="wrap" gap={1.5} sx={{ mb: 2 }}>
              {gruposHistograma.map(({ codigo, color }) => (
                <Stack key={String(codigo)} direction="row" alignItems="center" spacing={0.75}>
                  <Box
                    aria-hidden="true"
                    sx={{ width: 12, height: 12, borderRadius: 0.5, bgcolor: color }}
                  />
                  <Typography variant="caption">{resolveMessage(codigo)}</Typography>
                </Stack>
              ))}
            </Stack>
            <Box
              role="region"
              aria-labelledby="reporte-comprobantes-grafico-diario"
              sx={{ overflowX: "auto", pb: 1 }}
            >
              {tipoGrafico === "barras" ? (
                <Stack
                  direction="row"
                  alignItems="flex-end"
                  spacing={1}
                  sx={{ minWidth: "max-content", height: 220 }}
                >
                  {datosHistograma.map((dia) => {
                    const segmentos = gruposHistograma.map(({ codigo, campo, color }) => ({
                      etiqueta: resolveMessage(codigo),
                      valor: dia[campo],
                      color,
                    }));
                    return (
                      <Stack
                        key={dia.fecha}
                        role="group"
                        aria-label={`${fechaVisible(dia.fecha)}: ${dia.total}`}
                        alignItems="center"
                        justifyContent="flex-end"
                        spacing={0.5}
                        sx={{ width: 42, height: "100%" }}
                        title={`${fechaVisible(dia.fecha)}: ${dia.total}`}
                      >
                        <Typography variant="caption" color="text.secondary">
                          {dia.total}
                        </Typography>
                        <Stack
                          direction="column-reverse"
                          sx={{
                            width: 24,
                            height: 160,
                            justifyContent: "flex-start",
                            borderBottom: "1px solid",
                            borderColor: "divider",
                          }}
                        >
                          {segmentos.map(({ etiqueta, valor, color }) => (
                            <Box
                              key={etiqueta}
                              title={`${etiqueta}: ${valor}`}
                              sx={{
                                flex: `0 0 ${(valor / maximoDiario) * 100}%`,
                                minHeight: valor > 0 ? 2 : 0,
                                bgcolor: color,
                              }}
                            />
                          ))}
                        </Stack>
                        <Typography
                          variant="caption"
                          color="text.secondary"
                          sx={{ whiteSpace: "nowrap" }}
                        >
                          {dia.fecha.slice(5)}
                        </Typography>
                      </Stack>
                    );
                  })}
                </Stack>
              ) : (
                <svg
                  role="img"
                  aria-labelledby="reporte-comprobantes-grafico-diario"
                  width={anchoGrafico}
                  height={altoGrafico}
                  viewBox={`0 0 ${anchoGrafico} ${altoGrafico}`}
                >
                  {marcasEjeY.map((valor) => {
                    const y = margenGrafico.arriba + altoTrazado * (1 - valor / maximoTrazado);
                    return (
                      <g key={valor}>
                        <line
                          x1={margenGrafico.izquierda}
                          x2={anchoGrafico - margenGrafico.derecha}
                          y1={y}
                          y2={y}
                          stroke="#d9dee7"
                          strokeDasharray={valor === 0 ? undefined : "3 4"}
                        />
                        <text
                          x={margenGrafico.izquierda - 8}
                          y={y + 4}
                          textAnchor="end"
                          fill="#626b78"
                          fontSize="11"
                        >
                          {valor}
                        </text>
                      </g>
                    );
                  })}
                  {gruposHistograma
                    .filter(({ campo }) => datosHistograma.some((dia) => dia[campo] > 0))
                    .map(({ codigo, campo, color }, indiceGrupo, gruposVisibles) => {
                      const desplazamientoX =
                        (indiceGrupo - (gruposVisibles.length - 1) / 2) * 7;
                      const puntos = datosHistograma.map((dia, indice) => {
                        const punto = puntoGrafico(indice, dia[campo]);
                        return {
                          ...punto,
                          x: punto.x + desplazamientoX,
                          fecha: dia.fecha,
                          valor: dia[campo],
                        };
                      });
                      return (
                        <g key={String(codigo)}>
                          {tipoGrafico === "lineas" && (
                            <polyline
                              points={puntos.map(({ x, y }) => `${x},${y}`).join(" ")}
                              fill="none"
                              stroke={color}
                              strokeWidth="2.5"
                              strokeLinejoin="round"
                            />
                          )}
                          {puntos.map(({ x, y, fecha, valor }) => (
                            <g key={`${codigo}-${fecha}`}>
                              {tipoGrafico === "lineas" && (
                                <title>
                                  {`${fechaVisible(fecha)} · ${resolveMessage(codigo)}: ${valor}`}
                                </title>
                              )}
                              <circle
                                cx={x}
                                cy={y}
                                r={tipoGrafico === "lineas" ? 3.5 : 5}
                                fill={color}
                                stroke="#fff"
                                strokeWidth="1"
                              >
                                {tipoGrafico === "dispersion" && (
                                  <title>
                                    {`${fechaVisible(fecha)} · ${resolveMessage(codigo)}: ${valor}`}
                                  </title>
                                )}
                              </circle>
                            </g>
                          ))}
                        </g>
                      );
                    })}
                  {datosHistograma.map((dia, indice) => {
                    const { x } = puntoGrafico(indice, 0);
                    return (
                      <text
                        key={dia.fecha}
                        x={x}
                        y={altoGrafico - 10}
                        textAnchor="middle"
                        fill="#626b78"
                        fontSize="10"
                      >
                        {dia.fecha.slice(5)}
                      </text>
                    );
                  })}
                </svg>
              )}
            </Box>
          </Paper>
        )}

        {vista === "resumen" && !resumen && !cargando && (
          <Paper variant="outlined" sx={{ p: 3, textAlign: "center" }}>
            {resolveMessage(MessageCodes.REPORTE_COMPROBANTES_SELECT_COMPANY)}
          </Paper>
        )}

        {vista === "seguimiento" && (
          <Grid container spacing={2}>
            <Grid size={{ xs: 12, lg: 5 }}>
              <Paper variant="outlined" sx={{ overflow: "hidden" }}>
                <Stack
                  direction="row"
                  justifyContent="space-between"
                  alignItems="center"
                  sx={{ px: 2, py: 1.5 }}
                >
                  <Typography variant="h6" component="h2">
                    {resolveMessage(MessageCodes.REPORTE_COMPROBANTES_RESULTS)}
                    {pagina ? ` (${pagina.totalElementos})` : ""}
                  </Typography>
                  {cargando && <CircularProgress size={22} />}
                </Stack>
                <Stack spacing={1} sx={{ p: 1.5 }}>
                  {filas.map((comprobante) => (
                    <Paper
                      key={comprobante.id}
                      variant="outlined"
                      sx={{
                        p: 1.5,
                        borderColor:
                          detalle?.comprobante.id === comprobante.id ? "primary.main" : "divider",
                        bgcolor:
                          detalle?.comprobante.id === comprobante.id
                            ? "action.hover"
                            : "background.paper",
                      }}
                    >
                      <Stack
                        direction={{ xs: "column", sm: "row" }}
                        justifyContent="space-between"
                        gap={1}
                      >
                        <Box>
                          <Typography fontWeight={700}>{comprobante.numeroComprobante}</Typography>
                          <Typography variant="body2" color="text.secondary">
                            {comprobante.tipoDocumento} · {fechaVisible(comprobante.fechaEmision)}
                          </Typography>
                          <Typography variant="caption" color="text.secondary">
                            {comprobante.razonSocialReceptor ||
                              comprobante.identificacionReceptor ||
                              "—"}
                          </Typography>
                        </Box>
                        <Stack alignItems={{ xs: "flex-start", sm: "flex-end" }} gap={0.75}>
                          <Chip
                            size="small"
                            label={comprobante.estadoProceso}
                            color={colorEstado(comprobante)}
                          />
                          <Button
                            size="small"
                            startIcon={<TimelineOutlinedIcon />}
                            onClick={() => void abrirDetalle(comprobante)}
                          >
                            {resolveMessage(MessageCodes.REPORTE_COMPROBANTES_TRACKING_SELECT)}
                          </Button>
                        </Stack>
                      </Stack>
                    </Paper>
                  ))}
                  {filtrosAplicados && !cargando && filas.length === 0 && (
                    <Typography color="text.secondary" align="center" sx={{ py: 3 }}>
                      {resolveMessage(MessageCodes.REPORTE_COMPROBANTES_EMPTY)}
                    </Typography>
                  )}
                  {!filtrosAplicados && (
                    <Typography color="text.secondary" align="center" sx={{ py: 3 }}>
                      {resolveMessage(MessageCodes.REPORTE_COMPROBANTES_SELECT_COMPANY)}
                    </Typography>
                  )}
                </Stack>
                <TablePagination
                  component="div"
                  count={pagina?.totalElementos ?? 0}
                  page={numeroPagina}
                  onPageChange={(_event, nuevaPagina) => setNumeroPagina(nuevaPagina)}
                  rowsPerPage={tamanoPagina}
                  rowsPerPageOptions={[25, 50, 100]}
                  onRowsPerPageChange={(event) => {
                    setTamanoPagina(Number(event.target.value));
                    setNumeroPagina(0);
                  }}
                  labelRowsPerPage={resolveMessage(MessageCodes.COMMON_ROWS_PER_PAGE)}
                  labelDisplayedRows={({ from, to, count }) => `${from}–${to} de ${count}`}
                />
              </Paper>
            </Grid>
            <Grid size={{ xs: 12, lg: 7 }}>
              <Paper variant="outlined" sx={{ p: 2, minHeight: 320 }}>
                {cargandoDetalle ? (
                  <CircularProgress />
                ) : detalle ? (
                  <Stack spacing={2}>
                    <Box>
                      <Typography variant="h6" component="h2">
                        {detalle.comprobante.numeroComprobante}
                      </Typography>
                      <Typography color="text.secondary">
                        {detalle.comprobante.claveAcceso}
                      </Typography>
                    </Box>
                    <Stack direction="row" flexWrap="wrap" gap={1}>
                      <Chip
                        label={detalle.comprobante.estadoProceso}
                        color={colorEstado(detalle.comprobante)}
                      />
                      {detalle.comprobante.estadoSri && (
                        <Chip label={detalle.comprobante.estadoSri} variant="outlined" />
                      )}
                    </Stack>
                    {detalle.comprobante.mensajeError && (
                      <Alert severity="error">
                        {detalle.comprobante.codigoError}: {detalle.comprobante.mensajeError}
                      </Alert>
                    )}
                    <Divider />
                    <Typography variant="h6" component="h3">
                      <TimelineOutlinedIcon
                        fontSize="small"
                        sx={{ verticalAlign: "middle", mr: 0.75 }}
                      />
                      {resolveMessage(MessageCodes.REPORTE_COMPROBANTES_AUDIT)}
                    </Typography>
                    {detalle.auditoria.length === 0 ? (
                      <Typography color="text.secondary">
                        {resolveMessage(MessageCodes.REPORTE_COMPROBANTES_AUDIT_EMPTY)}
                      </Typography>
                    ) : (
                      <Stack spacing={0} sx={{ pl: 1 }}>
                        {detalle.auditoria.map((evento, indice) => (
                          <Box
                            key={`${evento.etapa}-${evento.intento}-${indice}`}
                            sx={{
                              position: "relative",
                              pl: 2.5,
                              pb: 2,
                              borderLeft:
                                indice === detalle.auditoria.length - 1 ? "none" : "2px solid",
                              borderColor: "divider",
                            }}
                          >
                            <Box
                              sx={{
                                position: "absolute",
                                left: -6,
                                top: 2,
                                width: 10,
                                height: 10,
                                borderRadius: "50%",
                                bgcolor: /error|fallido|rechazado/i.test(evento.resultado)
                                  ? "error.main"
                                  : "primary.main",
                              }}
                            />
                            <Typography fontWeight={700}>
                              {evento.etapa} · {evento.resultado}
                            </Typography>
                            <Typography variant="body2" color="text.secondary">
                              {evento.estadoAnterior || "—"} → {evento.estadoNuevo || "—"}
                            </Typography>
                            <Typography variant="caption" color="text.secondary">
                              {fechaVisible(evento.fechaInicio)} · intento {evento.intento}
                            </Typography>
                            {evento.mensajeError && (
                              <Typography variant="body2" color="error.main">
                                {evento.codigoError}: {evento.mensajeError}
                              </Typography>
                            )}
                          </Box>
                        ))}
                      </Stack>
                    )}
                  </Stack>
                ) : (
                  <Typography color="text.secondary">
                    {resolveMessage(MessageCodes.REPORTE_COMPROBANTES_TRACKING_SELECT_PROMPT)}
                  </Typography>
                )}
              </Paper>
            </Grid>
          </Grid>
        )}

        {vista !== "resumen" && vista !== "seguimiento" && (
          <>
            <Paper variant="outlined" sx={{ overflow: "hidden" }}>
              <Stack
                direction="row"
                justifyContent="space-between"
                alignItems="center"
                sx={{ px: 2, py: 1.5 }}
              >
                <Typography variant="h6" component="h2">
                  {resolveMessage(MessageCodes.REPORTE_COMPROBANTES_RESULTS)}
                  {pagina ? ` (${pagina.totalElementos})` : ""}
                </Typography>
                {cargando && <CircularProgress size={22} />}
              </Stack>
              <TableContainer>
                <Table size="small">
                  <TableHead>
                    <TableRow>
                      <TableCell>
                        {resolveMessage(MessageCodes.REPORTE_COMPROBANTES_NUMBER)}
                      </TableCell>
                      <TableCell>
                        {resolveMessage(MessageCodes.REPORTE_COMPROBANTES_ISSUE_DATE)}
                      </TableCell>
                      <TableCell>
                        {resolveMessage(MessageCodes.REPORTE_COMPROBANTES_RECEIVER)}
                      </TableCell>
                      <TableCell>
                        {resolveMessage(MessageCodes.REPORTE_COMPROBANTES_DOCUMENT_TYPE)}
                      </TableCell>
                      <TableCell>
                        {resolveMessage(MessageCodes.REPORTE_COMPROBANTES_PROCESS_STATE)}
                      </TableCell>
                      <TableCell>
                        {resolveMessage(MessageCodes.REPORTE_COMPROBANTES_AUTHORIZATION)}
                      </TableCell>
                      <TableCell>
                        {resolveMessage(MessageCodes.REPORTE_COMPROBANTES_ERROR)}
                      </TableCell>
                      <TableCell align="right">
                        {resolveMessage(MessageCodes.REPORTE_COMPROBANTES_ACTIONS)}
                      </TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {filas.map((comprobante) => (
                      <TableRow hover key={comprobante.id}>
                        <TableCell>
                          <Typography variant="body2" fontWeight={700}>
                            {comprobante.numeroComprobante}
                          </Typography>
                          <Typography variant="caption" color="text.secondary">
                            {comprobante.claveAcceso}
                          </Typography>
                        </TableCell>
                        <TableCell>{fechaVisible(comprobante.fechaEmision)}</TableCell>
                        <TableCell>
                          <Typography variant="body2">
                            {comprobante.razonSocialReceptor || "—"}
                          </Typography>
                          <Typography variant="caption" color="text.secondary">
                            {comprobante.identificacionReceptor || "—"}
                          </Typography>
                        </TableCell>
                        <TableCell>{comprobante.tipoDocumento}</TableCell>
                        <TableCell>
                          <Chip
                            size="small"
                            label={comprobante.estadoProceso}
                            color={colorEstado(comprobante)}
                          />
                          {comprobante.estadoSri && (
                            <Typography variant="caption" display="block">
                              {comprobante.estadoSri}
                            </Typography>
                          )}
                        </TableCell>
                        <TableCell>{comprobante.numeroAutorizacion || "—"}</TableCell>
                        <TableCell>
                          {comprobante.codigoError ? (
                            <Typography
                              variant="body2"
                              color="error.main"
                              title={comprobante.mensajeError ?? ""}
                            >
                              {comprobante.codigoError}
                            </Typography>
                          ) : (
                            "—"
                          )}
                        </TableCell>
                        <TableCell align="right">
                          <Stack direction="row" justifyContent="flex-end" flexWrap="wrap" gap={0.5}>
                            <Button
                              size="small"
                              startIcon={<PictureAsPdfOutlinedIcon fontSize="small" />}
                              onClick={() => void verRide(comprobante)}
                              disabled={exportando}
                            >
                              {resolveMessage(MessageCodes.REPORTE_COMPROBANTES_VIEW_RIDE)}
                            </Button>
                            {puedeConsultarCorreo && (
                              <>
                                <Button
                                  size="small"
                                  startIcon={<EmailOutlinedIcon fontSize="small" />}
                                  onClick={() => void abrirHistorialCorreo(comprobante)}
                                  disabled={!comprobante.idTransaccion}
                                >
                                  {resolveMessage(MessageCodes.REPORTE_COMPROBANTES_EMAIL_VIEW)}
                                </Button>
                                <Button
                                  size="small"
                                  startIcon={<RefreshOutlinedIcon fontSize="small" />}
                                  onClick={() => void abrirHistorialCorreo(comprobante, true)}
                                  disabled={!comprobante.idTransaccion}
                                >
                                  {resolveMessage(MessageCodes.REPORTE_COMPROBANTES_EMAIL_RESEND)}
                                </Button>
                              </>
                            )}
                            {puedeReprocesar && esReprocesable(comprobante) && (
                              <Button
                                size="small"
                                onClick={() => void reprocesar(comprobante)}
                                disabled={exportando}
                              >
                                {resolveMessage(MessageCodes.REPORTE_COMPROBANTES_REPROCESS)}
                              </Button>
                            )}
                          </Stack>
                        </TableCell>
                      </TableRow>
                    ))}
                    {filtrosAplicados && !cargando && filas.length === 0 && (
                      <TableRow>
                        <TableCell colSpan={8} align="center" sx={{ py: 4 }}>
                          {resolveMessage(MessageCodes.REPORTE_COMPROBANTES_EMPTY)}
                        </TableCell>
                      </TableRow>
                    )}
                    {!filtrosAplicados && (
                      <TableRow>
                        <TableCell colSpan={8} align="center" sx={{ py: 4 }}>
                          {resolveMessage(
                            vista === "busqueda"
                              ? MessageCodes.REPORTE_COMPROBANTES_SEARCH_REQUIRED
                              : MessageCodes.REPORTE_COMPROBANTES_SELECT_COMPANY,
                          )}
                        </TableCell>
                      </TableRow>
                    )}
                  </TableBody>
                </Table>
              </TableContainer>
              <TablePagination
                component="div"
                count={pagina?.totalElementos ?? 0}
                page={numeroPagina}
                onPageChange={(_event, nuevaPagina) => setNumeroPagina(nuevaPagina)}
                rowsPerPage={tamanoPagina}
                rowsPerPageOptions={[25, 50, 100]}
                onRowsPerPageChange={(event) => {
                  setTamanoPagina(Number(event.target.value));
                  setNumeroPagina(0);
                }}
                labelRowsPerPage={resolveMessage(MessageCodes.COMMON_ROWS_PER_PAGE)}
                labelDisplayedRows={({ from, to, count }) => `${from}–${to} de ${count}`}
              />
            </Paper>
            <Typography variant="caption" color="text.secondary" sx={{ display: "block", mt: 1 }}>
              {resolveMessage(MessageCodes.REPORTE_COMPROBANTES_PRINT_HELP)}
            </Typography>
          </>
        )}
      </Box>

      <Box className="comprobantes-report-print">
        <Typography variant="h5" fontWeight={700}>
          {resolveMessage(MessageCodes.REPORTE_COMPROBANTES_TITLE)}
        </Typography>
        <Typography variant="body2" sx={{ mb: 2 }}>
          {empresaSeleccionada
            ? `${empresaSeleccionada.ruc} — ${empresaSeleccionada.razonSocial}`
            : ""}
          {" · "}
          {filtrosAplicados?.fechaDesde} — {filtrosAplicados?.fechaHasta}
          {" · "}
          {filasImpresion?.length ?? 0} {resolveMessage(MessageCodes.REPORTE_COMPROBANTES_RESULTS)}
        </Typography>
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>{resolveMessage(MessageCodes.REPORTE_COMPROBANTES_NUMBER)}</TableCell>
              <TableCell>{resolveMessage(MessageCodes.REPORTE_COMPROBANTES_ISSUE_DATE)}</TableCell>
              <TableCell>{resolveMessage(MessageCodes.REPORTE_COMPROBANTES_RECEIVER)}</TableCell>
              <TableCell>
                {resolveMessage(MessageCodes.REPORTE_COMPROBANTES_DOCUMENT_TYPE)}
              </TableCell>
              <TableCell>
                {resolveMessage(MessageCodes.REPORTE_COMPROBANTES_PROCESS_STATE)}
              </TableCell>
              <TableCell>{resolveMessage(MessageCodes.REPORTE_COMPROBANTES_SRI_STATE)}</TableCell>
              <TableCell>
                {resolveMessage(MessageCodes.REPORTE_COMPROBANTES_AUTHORIZATION)}
              </TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {(filasImpresion ?? []).map((comprobante) => (
              <TableRow key={comprobante.id}>
                <TableCell>{comprobante.numeroComprobante}</TableCell>
                <TableCell>{fechaVisible(comprobante.fechaEmision)}</TableCell>
                <TableCell>{`${comprobante.razonSocialReceptor ?? ""} ${comprobante.identificacionReceptor ?? ""}`}</TableCell>
                <TableCell>{comprobante.tipoDocumento}</TableCell>
                <TableCell>{comprobante.estadoProceso}</TableCell>
                <TableCell>{comprobante.estadoSri ?? ""}</TableCell>
                <TableCell>{comprobante.numeroAutorizacion ?? ""}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </Box>

      <Dialog
        open={Boolean(comprobanteCorreo)}
        onClose={cerrarHistorialCorreo}
        fullWidth
        maxWidth="md"
      >
        <DialogTitle>{resolveMessage(MessageCodes.REPORTE_COMPROBANTES_EMAIL_HISTORY)}</DialogTitle>
        <DialogContent dividers>
          {comprobanteCorreo && (
            <Stack spacing={2}>
              <Typography variant="body2">
                {comprobanteCorreo.numeroComprobante} · {comprobanteCorreo.claveAcceso}
              </Typography>
              {errorCorreo && <Alert severity="error">{errorCorreo}</Alert>}
              {avisoCorreo && <Alert severity="success">{avisoCorreo}</Alert>}
              <Alert severity="info">
                {resolveMessage(MessageCodes.REPORTE_COMPROBANTES_EMAIL_SMTP_NOTICE)}
              </Alert>
              {cargandoCorreo ? (
                <CircularProgress />
              ) : enviosCorreo.length === 0 ? (
                <Typography color="text.secondary">
                  {resolveMessage(MessageCodes.REPORTE_COMPROBANTES_EMAIL_EMPTY)}
                </Typography>
              ) : (
                enviosCorreo.map((envio) => {
                  const codigoEstado = {
                    PENDIENTE: MessageCodes.REPORTE_COMPROBANTES_EMAIL_STATE_PENDING,
                    EN_PROCESO: MessageCodes.REPORTE_COMPROBANTES_EMAIL_STATE_PROCESSING,
                    ENVIADA: MessageCodes.REPORTE_COMPROBANTES_EMAIL_STATE_SENT,
                    FALLIDA: MessageCodes.REPORTE_COMPROBANTES_EMAIL_STATE_FAILED,
                  }[envio.estado];
                  const colorEstadoEnvio =
                    envio.estado === "ENVIADA" ? "success" :
                      envio.estado === "FALLIDA" ? "error" :
                        envio.estado === "PENDIENTE" || envio.estado === "EN_PROCESO" ? "warning" : "default";
                  return (
                    <Paper key={envio.id} variant="outlined" sx={{ p: 1.5 }}>
                      <Stack direction={{ xs: "column", sm: "row" }} justifyContent="space-between" gap={1}>
                        <Box>
                          <Typography variant="body2">
                            {resolveMessage(MessageCodes.REPORTE_COMPROBANTES_EMAIL_RECIPIENT)}: {envio.destinatario}
                          </Typography>
                          <Typography variant="caption" color="text.secondary" display="block">
                            {resolveMessage(MessageCodes.REPORTE_COMPROBANTES_EMAIL_RECEIVED)}: {fechaVisible(envio.fechaRecepcion)}
                          </Typography>
                          {envio.fechaEnvio && (
                            <Typography variant="caption" color="text.secondary" display="block">
                              {resolveMessage(MessageCodes.REPORTE_COMPROBANTES_EMAIL_SENT)}: {fechaVisible(envio.fechaEnvio)}
                            </Typography>
                          )}
                          <Typography variant="caption" color="text.secondary" display="block">
                            {resolveMessage(MessageCodes.REPORTE_COMPROBANTES_EMAIL_ATTEMPTS)}: {envio.numeroIntentos}
                          </Typography>
                          {envio.usuarioSolicitud && (
                            <Typography variant="caption" color="text.secondary" display="block">
                              {resolveMessage(MessageCodes.REPORTE_COMPROBANTES_EMAIL_REQUESTED_BY)}: {envio.usuarioSolicitud}
                            </Typography>
                          )}
                        </Box>
                        <Chip size="small" color={colorEstadoEnvio} label={resolveMessage(codigoEstado)} />
                      </Stack>
                    </Paper>
                  );
                })
              )}
              {confirmarReenvio ? (
                <Alert
                  severity="warning"
                  action={
                    <Stack direction="row" spacing={1}>
                      <Button size="small" onClick={() => setConfirmarReenvio(false)}>
                        {resolveMessage(MessageCodes.COMMON_CANCEL)}
                      </Button>
                      <Button
                        size="small"
                        color="inherit"
                        onClick={() => void reenviarCorreo()}
                        disabled={reenvioEnCurso}
                      >
                        {reenvioEnCurso
                          ? resolveMessage(MessageCodes.COMMON_SAVING)
                          : resolveMessage(MessageCodes.REPORTE_COMPROBANTES_EMAIL_RESEND)}
                      </Button>
                    </Stack>
                  }
                >
                  {resolveMessage(MessageCodes.REPORTE_COMPROBANTES_EMAIL_RESEND_CONFIRM)}
                </Alert>
              ) : puedeReenviarCorreo ? (
                <Button
                  variant="contained"
                  onClick={() => setConfirmarReenvio(true)}
                  disabled={cargandoCorreo || reenvioEnCurso || enviosCorreo.length === 0 ||
                    ["PENDIENTE", "EN_PROCESO"].includes(enviosCorreo[0]?.estado ?? "")}
                >
                  {resolveMessage(MessageCodes.REPORTE_COMPROBANTES_EMAIL_RESEND)}
                </Button>
              ) : null}
              <Button
                variant="text"
                startIcon={<RefreshOutlinedIcon />}
                onClick={() => void cargarEnviosCorreo(comprobanteCorreo.idTransaccion, solicitudCorreo.current)}
                disabled={cargandoCorreo || reenvioEnCurso}
              >
                {resolveMessage(MessageCodes.REPORTE_COMPROBANTES_REFRESH)}
              </Button>
            </Stack>
          )}
        </DialogContent>
      </Dialog>

      <Dialog
        open={vista !== "seguimiento" && (Boolean(detalle) || cargandoDetalle)}
        onClose={cerrarDetalle}
        fullWidth
        maxWidth="md"
      >
        <DialogTitle>{resolveMessage(MessageCodes.REPORTE_COMPROBANTES_DETAIL)}</DialogTitle>
        <DialogContent dividers>
          {cargandoDetalle ? (
            <CircularProgress />
          ) : detalle ? (
            <Stack spacing={2}>
              <Grid container spacing={1}>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <Typography variant="caption" color="text.secondary">
                    {resolveMessage(MessageCodes.REPORTE_COMPROBANTES_NUMBER)}
                  </Typography>
                  <Typography>{detalle.comprobante.numeroComprobante}</Typography>
                </Grid>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <Typography variant="caption" color="text.secondary">
                    {resolveMessage(MessageCodes.REPORTE_COMPROBANTES_ACCESS_KEY)}
                  </Typography>
                  <Typography sx={{ overflowWrap: "anywhere" }}>
                    {detalle.comprobante.claveAcceso}
                  </Typography>
                </Grid>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <Typography variant="caption" color="text.secondary">
                    {resolveMessage(MessageCodes.REPORTE_COMPROBANTES_ISSUE_DATE)}
                  </Typography>
                  <Typography>{fechaVisible(detalle.comprobante.fechaEmision)}</Typography>
                </Grid>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <Typography variant="caption" color="text.secondary">
                    {resolveMessage(MessageCodes.REPORTE_COMPROBANTES_RECEIVER)}
                  </Typography>
                  <Typography>
                    {detalle.comprobante.razonSocialReceptor || "—"} ·{" "}
                    {detalle.comprobante.identificacionReceptor || "—"}
                  </Typography>
                </Grid>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <Typography variant="caption" color="text.secondary">
                    {resolveMessage(MessageCodes.REPORTE_COMPROBANTES_PROCESS_STATE)}
                  </Typography>
                  <Typography>{detalle.comprobante.estadoProceso}</Typography>
                </Grid>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <Typography variant="caption" color="text.secondary">
                    {resolveMessage(MessageCodes.REPORTE_COMPROBANTES_SRI_STATE)}
                  </Typography>
                  <Typography>{detalle.comprobante.estadoSri || "—"}</Typography>
                </Grid>
                {detalle.comprobante.mensajeError && (
                  <Grid size={12}>
                    <Alert severity="error">
                      {detalle.comprobante.codigoError}: {detalle.comprobante.mensajeError}
                    </Alert>
                  </Grid>
                )}
              </Grid>
              <Divider />
              <Typography variant="h6" component="h3">
                <TimelineOutlinedIcon fontSize="small" sx={{ verticalAlign: "middle", mr: 0.75 }} />
                {resolveMessage(MessageCodes.REPORTE_COMPROBANTES_AUDIT)}
              </Typography>
              {detalle.auditoria.length === 0 ? (
                <Typography color="text.secondary">
                  {resolveMessage(MessageCodes.REPORTE_COMPROBANTES_AUDIT_EMPTY)}
                </Typography>
              ) : (
                detalle.auditoria.map((evento, indice) => (
                  <Paper
                    key={`${evento.etapa}-${evento.intento}-${indice}`}
                    variant="outlined"
                    sx={{ p: 1.5 }}
                  >
                    <Stack
                      direction={{ xs: "column", sm: "row" }}
                      justifyContent="space-between"
                      gap={1}
                    >
                      <Box>
                        <Typography fontWeight={700}>
                          {evento.etapa} · {evento.resultado}
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                          {evento.estadoAnterior || "—"} → {evento.estadoNuevo || "—"}
                        </Typography>
                        {evento.mensajeError && (
                          <Typography variant="body2" color="error.main">
                            {evento.codigoError}: {evento.mensajeError}
                          </Typography>
                        )}
                      </Box>
                      <Typography variant="caption" color="text.secondary">
                        {fechaVisible(evento.fechaInicio)} · intento {evento.intento}
                      </Typography>
                    </Stack>
                  </Paper>
                ))
              )}
            </Stack>
          ) : null}
        </DialogContent>
      </Dialog>
    </Box>
  );
}
