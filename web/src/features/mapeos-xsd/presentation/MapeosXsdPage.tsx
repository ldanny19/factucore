import { useCallback, useEffect, useMemo, useState } from "react";
import {
  Alert,
  Box,
  Button,
  CircularProgress,
  MenuItem,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Typography,
} from "@mui/material";
import { useSearchParams } from "react-router-dom";
import { getRuntimeConfig } from "../../../config/runtime-config";
import { EstadoRegistro } from "../../../shared/domain/EstadoRegistro";
import { apiRequest, apiRequestRaw } from "../../../shared/api/api-client";
import type { AdministracionRequest } from "../../../shared/api/administracion-contracts";
import type { Empresa } from "../../empresas/domain/Empresa";
import type { Catalogo } from "../../catalogos/domain/Catalogo";
import { MessageCodes } from "../../../shared/messages/MessageCodes";
import { resolveMessage } from "../../../shared/messages/MessageResolver";
import "../../../styles/app.css";

interface DocumentoXsd {
  id: number;
  codigo: string;
  nombre: string;
  estadoRegistro: string;
}

interface VersionDocumentoXsd {
  id: number;
  documentoXsdId: number;
  version: string;
  estadoRegistro?: string;
}

interface VersionSeleccionable {
  id: number;
  version: string;
  documentoCodigo: string;
  documentoNombre: string;
}

interface ElementoXsd {
  id: number;
  idVersionDocumentoXsd: number;
  idElementoPadre: number | null;
  nombre: string;
  tipoDato: string | null;
  orden: number | null;
  estadoRegistro?: string;
}

interface AtributoXsd {
  id: number;
  idElementoXsd: number;
  nombre: string;
  tipoDato: string | null;
  estadoRegistro?: string;
}

interface MapeoXsd {
  id: number;
  idVersionDocumentoXsd: number;
  tipoOrigen: string;
  origen: string;
  idElementoXsd: number | null;
  idAtributoXsd: number | null;
  tipoMapeo: string;
  estadoRegistro: string;
}

interface DefinicionDocumento {
  documento: DocumentoXsd;
  version: VersionDocumentoXsd;
  elementos: ElementoXsd[];
  atributos: AtributoXsd[];
  mapeos: MapeoXsd[];
}

interface CampoFuente {
  codigo: string;
  nombre: string;
  tipoDato: string;
  origen: string;
}

interface FuenteFactuCore {
  codigo: string;
  nombre: string;
  requiereCatalogo: boolean;
  campos: CampoFuente[];
}

interface SolicitudMapeoXsd {
  idVersionDocumentoXsd: number;
  tipoOrigen: string;
  origen: string;
  idElementoXsd: number | null;
  idAtributoXsd: number | null;
  tipoMapeo: string;
}

interface DestinoXsd {
  key: string;
  tipoMapeo: "ELEMENTO" | "ATRIBUTO";
  idElementoXsd: number | null;
  idAtributoXsd: number | null;
  nombre: string;
}

interface FormularioMapeo {
  tipoOrigen: string;
  ejemploJson: string;
  codigoFuente: string;
  codigoCampo: string;
  codigoCatalogo: string;
  rutaJson: string;
  origenGenerado: string;
  destino: string;
}

const FORMULARIO_INICIAL: FormularioMapeo = {
  tipoOrigen: "",
  ejemploJson: "",
  codigoFuente: "",
  codigoCampo: "",
  codigoCatalogo: "",
  rutaJson: "",
  origenGenerado: "",
  destino: "",
};

function fechaHoraSolicitud(): string {
  const ahora = new Date();
  const pad = (valor: number, longitud = 2) => String(valor).padStart(longitud, "0");
  return `${ahora.getFullYear()}-${pad(ahora.getMonth() + 1)}-${pad(ahora.getDate())} ${pad(ahora.getHours())}:${pad(ahora.getMinutes())}:${pad(ahora.getSeconds())}.${pad(ahora.getMilliseconds(), 3)}`;
}

function mensajeError(error: unknown): string {
  if (error instanceof Error && error.message.trim()) return error.message;
  return resolveMessage(MessageCodes.COMMON_OPERATION_FAILED);
}

function rutaElemento(elemento: ElementoXsd, elementos: ElementoXsd[]): string {
  const partes = [elemento.nombre];
  let padreId = elemento.idElementoPadre;
  const visitados = new Set<number>([elemento.id]);

  while (padreId !== null) {
    if (visitados.has(padreId)) break;
    visitados.add(padreId);
    const padre = elementos.find((actual) => actual.id === padreId);
    if (!padre) break;
    partes.unshift(padre.nombre);
    padreId = padre.idElementoPadre;
  }

  return partes.join(".");
}

function crearDestinos(definicion: DefinicionDocumento): DestinoXsd[] {
  const idsElementosPadre = new Set(
    definicion.elementos
      .filter((elemento) => elemento.idElementoPadre !== null)
      .map((elemento) => elemento.idElementoPadre as number),
  );

  return definicion.elementos
    .filter((elemento) => {
      const tipoDato = elemento.tipoDato?.trim().toUpperCase();
      const activo = !elemento.estadoRegistro || elemento.estadoRegistro === EstadoRegistro.ACTIVO;
      const esContenedor = tipoDato === "MAP" || tipoDato === "LIST";
      const tieneHijos = idsElementosPadre.has(elemento.id);

      // Solo se ofrecen elementos hoja: algunos contenedores están mal tipados en los datos como STRING.
      return activo && tipoDato !== undefined && !esContenedor && !tieneHijos;
    })
    .sort((a, b) => {
      const orden = (a.orden ?? 0) - (b.orden ?? 0);
      return orden || a.id - b.id;
    })
    .map((elemento) => ({
      key: `elemento:${elemento.id}`,
      tipoMapeo: "ELEMENTO",
      idElementoXsd: elemento.id,
      idAtributoXsd: null,
      // Se muestra la ruta completa; el ID se conserva únicamente para guardar el mapeo.
      nombre: rutaElemento(elemento, definicion.elementos),
    }));
}


function extraerRutasJson(valor: unknown, prefijo = "", rutas: string[] = []): string[] {
  if (valor === null || typeof valor !== "object") {
    if (prefijo) rutas.push(prefijo);
    return rutas;
  }
  if (Array.isArray(valor)) {
    if (valor.length === 0) { if (prefijo) rutas.push(prefijo); return rutas; }
    return extraerRutasJson(valor[0], prefijo ? `${prefijo}[]` : "[]", rutas);
  }
  const entradas = Object.entries(valor as Record<string, unknown>);
  if (entradas.length === 0 && prefijo) rutas.push(prefijo);
  for (const [clave, contenido] of entradas) {
    extraerRutasJson(contenido, prefijo ? `${prefijo}.${clave}` : clave, rutas);
  }
  return rutas;
}

export default function MapeosXsdPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const [versiones, setVersiones] = useState<VersionSeleccionable[]>([]);
  const [empresas, setEmpresas] = useState<Empresa[]>([]);
  const [catalogos, setCatalogos] = useState<Catalogo[]>([]);
  const [fuentes, setFuentes] = useState<FuenteFactuCore[]>([]);
  const [definicion, setDefinicion] = useState<DefinicionDocumento | null>(null);
  const [mapeos, setMapeos] = useState<MapeoXsd[]>([]);
  const [mapeosPendientes, setMapeosPendientes] = useState<SolicitudMapeoXsd[]>([]);
  const [idVersionSeleccionada, setIdVersionSeleccionada] = useState("");
  const [idEmpresa, setIdEmpresa] = useState("");
  const [formulario, setFormulario] = useState<FormularioMapeo>(FORMULARIO_INICIAL);
  const [cargando, setCargando] = useState(true);
  const [cargandoDefinicion, setCargandoDefinicion] = useState(false);
  const [guardando, setGuardando] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [aviso, setAviso] = useState<string | null>(null);
  const config = getRuntimeConfig();
  const documentosPath = config.API_DOCUMENTOS_XSD_PATH.replace(/^\/+|\/+$/g, "");
  const mapeosPath = config.API_MAPEOS_XSD_PATH.replace(/^\/+|\/+$/g, "");

  const destinosBase = useMemo(() => (definicion ? crearDestinos(definicion) : []), [definicion]);
  const destinos = destinosBase.filter((destino) =>
    !mapeos.some((mapeo) => destino.tipoMapeo === "ELEMENTO" ? mapeo.idElementoXsd === destino.idElementoXsd : mapeo.idAtributoXsd === destino.idAtributoXsd)
    && !mapeosPendientes.some((mapeo) => destino.tipoMapeo === "ELEMENTO" ? mapeo.idElementoXsd === destino.idElementoXsd : mapeo.idAtributoXsd === destino.idAtributoXsd));
  const fuentesDisponibles = useMemo(
    () => fuentes.find((fuente) => fuente.codigo === formulario.codigoFuente) ?? null,
    [fuentes, formulario.codigoFuente],
  );
  const camposDisponibles = fuentesDisponibles?.campos ?? [];
  const campoSeleccionado = camposDisponibles.find(
    (campo) => campo.codigo === formulario.codigoCampo,
  );
  const catalogoSeleccionado = catalogos.find(
    (catalogo) => catalogo.codigo === formulario.codigoCatalogo,
  );
  const destinoSeleccionado = destinos.find((destino) => destino.key === formulario.destino);
  const plantillaJson = useMemo(() => {
    if (!formulario.ejemploJson.trim()) return null;
    try { return JSON.parse(formulario.ejemploJson) as unknown; } catch { return null; }
  }, [formulario.ejemploJson]);
  const rutasJson = useMemo(() => plantillaJson === null ? [] : extraerRutasJson(plantillaJson), [plantillaJson]);

  const cargarRecursos = useCallback(async () => {
    setCargando(true);
    setError(null);
    try {
      const [documentos, empresasActivas, fuentesFactuCore, catalogosActivos] = await Promise.all([
        apiRequest<DocumentoXsd[]>({ method: "GET", url: documentosPath }),
        apiRequest<Empresa[]>({ method: "GET", url: config.API_EMPRESAS_PATH }).then((lista) =>
          lista.filter((empresa) => empresa.estadoRegistro === EstadoRegistro.ACTIVO),
        ),
        apiRequest<FuenteFactuCore[]>({
          method: "GET",
          url: `${mapeosPath}/fuentes/factucore`,
        }),
        apiRequest<Catalogo[]>({ method: "GET", url: config.API_CATALOGOS_PATH }).then((lista) =>
          lista.filter((catalogo) => catalogo.estadoRegistro === EstadoRegistro.ACTIVO),
        ),
      ]);
      const documentosActivos = documentos.filter(
        (documento) => documento.estadoRegistro === EstadoRegistro.ACTIVO,
      );
      const versionesPorDocumento = await Promise.all(
        documentosActivos.map(async (documento) => {
          const versionesDocumento = await apiRequest<VersionDocumentoXsd[]>({
            method: "GET",
            url: `${documentosPath}/${documento.id}/versiones`,
          });
          return versionesDocumento.map((version) => ({
            id: version.id,
            version: version.version,
            documentoCodigo: documento.codigo,
            documentoNombre: documento.nombre,
          }));
        }),
      );
      const versionesDisponibles = versionesPorDocumento.flat();
      setVersiones(versionesDisponibles);
      setEmpresas(empresasActivas);
      setFuentes(fuentesFactuCore);
      setCatalogos(catalogosActivos);
      setIdEmpresa("");
      setIdVersionSeleccionada("");
      setMapeosPendientes([]);
      if (versionesDisponibles.length === 0) {
        setError("No hay versiones documentales XSD disponibles para configurar mapeos.");
      }
    } catch (exception) {
      setError(mensajeError(exception));
      setVersiones([]);
      setEmpresas([]);
      setFuentes([]);
      setCatalogos([]);
    } finally {
      setCargando(false);
    }
  }, [config.API_CATALOGOS_PATH, config.API_EMPRESAS_PATH, documentosPath, mapeosPath]);

  const cargarVersion = useCallback(async (id: string) => {
    if (!id) {
      setDefinicion(null);
      setMapeos([]);
      return;
    }
    setCargandoDefinicion(true);
    setError(null);
    try {
      const versionId = Number(id);
      const [definition, mappings] = await Promise.all([
        apiRequest<DefinicionDocumento>({
          method: "GET",
          url: `${documentosPath}/versiones/${versionId}`,
        }),
        apiRequest<MapeoXsd[]>({
          method: "GET",
          url: `${mapeosPath}/version/${versionId}`,
        }),
      ]);
      setDefinicion(definition);
      setMapeos(mappings);
      setMapeosPendientes([]);
      setFormulario(FORMULARIO_INICIAL);
    } catch (exception) {
      setDefinicion(null);
      setMapeos([]);
      setError(mensajeError(exception));
    } finally {
      setCargandoDefinicion(false);
    }
  }, [documentosPath, mapeosPath]);

  useEffect(() => {
    void cargarRecursos();
  }, [cargarRecursos]);

  useEffect(() => {
    if (idVersionSeleccionada) {
      setSearchParams({ versionId: idVersionSeleccionada }, { replace: true });
      void cargarVersion(idVersionSeleccionada);
    } else {
      setSearchParams({}, { replace: true });
      setDefinicion(null);
      setMapeos([]);
      setMapeosPendientes([]);
    }
  }, [cargarVersion, idVersionSeleccionada, setSearchParams]);

  useEffect(() => {
    if (formulario.tipoOrigen !== "JSON" || !idVersionSeleccionada) return;
    let cancelado = false;
    const cargarPlantilla = async () => {
      try {
        const respuesta = await apiRequestRaw<string>({
          method: "GET",
          url: documentosPath + "/versiones/" + idVersionSeleccionada + "/plantilla-json",
          responseType: "text",
        });
        if (cancelado) return;
        const plantilla = typeof respuesta === "string" ? respuesta : JSON.stringify(respuesta);
        JSON.parse(plantilla);
        setFormulario((actual) => ({ ...actual, ejemploJson: plantilla, rutaJson: "", origenGenerado: "" }));
      } catch (exception) {
        if (!cancelado) {
          setFormulario((actual) => ({ ...actual, ejemploJson: "", rutaJson: "", origenGenerado: "" }));
          setError(mensajeError(exception));
        }
      }
    };
    void cargarPlantilla();
    return () => { cancelado = true; };
  }, [documentosPath, formulario.tipoOrigen, idVersionSeleccionada]);
  const cambiarTipoOrigen = (tipoOrigen: string) => {
    setFormulario((actual) => ({
      ...FORMULARIO_INICIAL,
      tipoOrigen,
      destino: actual.destino,
    }));
  };

  const obtenerOrigen = (): string => {
    if (formulario.tipoOrigen === "JSON") return formulario.rutaJson.trim();
    if (formulario.tipoOrigen === "GENERADO") return formulario.origenGenerado.trim();
    if (formulario.tipoOrigen === "FACTUCORE" && campoSeleccionado) {
      if (fuentesDisponibles?.requiereCatalogo && catalogoSeleccionado) {
        return campoSeleccionado.origen.replace("{codigoCatalogo}", catalogoSeleccionado.codigo);
      }
      return campoSeleccionado.origen;
    }
    return "";
  };

  const agregarMapeo = () => {
    setError(null);
    setAviso(null);
    const origen = obtenerOrigen();
    if (!definicion || !idVersionSeleccionada || !destinoSeleccionado || !origen) {
      setError(resolveMessage(MessageCodes.MAPEO_XSD_REQUIRED_FIELDS));
      return;
    }
    if (!idEmpresa) {
      setError(resolveMessage(MessageCodes.MAPEO_XSD_COMPANY_REQUIRED));
      return;
    }
    if (mapeos.some((mapeo) => mapeo.origen === origen) || mapeosPendientes.some((mapeo) => mapeo.origen === origen)) {
      setError(resolveMessage(MessageCodes.MAPEO_XSD_SOURCE_ALREADY_MAPPED));
      return;
    }
    const destinoYaMapeado = [...mapeos, ...mapeosPendientes].some((mapeo) =>
      destinoSeleccionado.tipoMapeo === "ELEMENTO"
        ? mapeo.idElementoXsd === destinoSeleccionado.idElementoXsd
        : mapeo.idAtributoXsd === destinoSeleccionado.idAtributoXsd,
    );
    if (destinoYaMapeado) {
      setError(resolveMessage(MessageCodes.MAPEO_XSD_TARGET_ALREADY_MAPPED));
      return;
    }

    const mapeo: SolicitudMapeoXsd = {
      idVersionDocumentoXsd: Number(idVersionSeleccionada),
      tipoOrigen: formulario.tipoOrigen,
      origen,
      idElementoXsd: destinoSeleccionado.idElementoXsd,
      idAtributoXsd: destinoSeleccionado.idAtributoXsd,
      tipoMapeo: destinoSeleccionado.tipoMapeo,
    };
    setMapeosPendientes((actuales) => [...actuales, mapeo]);
    setFormulario((actual) => ({ ...FORMULARIO_INICIAL, tipoOrigen: actual.tipoOrigen }));
  };

  const quitarMapeoPendiente = (indice: number) => setMapeosPendientes((actuales) => actuales.filter((_, i) => i !== indice));

  const guardarMapeos = async () => {
    setError(null);
    setAviso(null);
    if (!idEmpresa || !idVersionSeleccionada || mapeosPendientes.length === 0) {
      setError(resolveMessage(MessageCodes.MAPEO_XSD_REQUIRED_FIELDS));
      return;
    }
    const solicitud: AdministracionRequest<SolicitudMapeoXsd[]> = {
      fechaInicio: fechaHoraSolicitud(), canal: "WEB", idEmpresa: Number(idEmpresa), datos: mapeosPendientes,
    };
    setGuardando(true);
    try {
      await apiRequest<MapeoXsd[]>({ method: "POST", url: mapeosPath, data: solicitud });
      setAviso(resolveMessage(MessageCodes.MAPEO_XSD_SAVED));
      setMapeosPendientes([]);
      setFormulario(FORMULARIO_INICIAL);
      await cargarVersion(idVersionSeleccionada);
    } catch (exception) {
      setError(mensajeError(exception));
    } finally {
      setGuardando(false);
    }
  };

  const descripcionDestino = (mapeo: MapeoXsd): string => {
    if (mapeo.idElementoXsd !== null) {
      const elemento = definicion?.elementos.find((actual) => actual.id === mapeo.idElementoXsd);
      return elemento ? rutaElemento(elemento, definicion?.elementos ?? []) : String(mapeo.idElementoXsd);
    }
    const atributo = definicion?.atributos.find((actual) => actual.id === mapeo.idAtributoXsd);
    const elemento = definicion?.elementos.find((actual) => actual.id === atributo?.idElementoXsd);
    return atributo
      ? `${elemento ? rutaElemento(elemento, definicion?.elementos ?? []) : ""}.@${atributo.nombre}`
      : String(mapeo.idAtributoXsd);
  };

  return (
    <Box className="catalogos-page">
      <Box className="catalogos-page-heading">
        <Box>
          <Typography className="page-title" component="h1" variant="h4">
            {resolveMessage(MessageCodes.MAPEO_XSD_PAGE_TITLE)}
          </Typography>
          <Typography color="text.secondary">
            {resolveMessage(MessageCodes.MAPEO_XSD_PAGE_DESCRIPTION)}
          </Typography>
        </Box>
      </Box>

      {error && <Alert severity="error" onClose={() => setError(null)}>{error}</Alert>}
      {aviso && <Alert severity="success" onClose={() => setAviso(null)}>{aviso}</Alert>}

      <Box sx={{ display: "grid", gridTemplateColumns: { xs: "1fr", md: "minmax(240px, 0.8fr) minmax(320px, 1.5fr)" }, gap: 2, alignItems: "center" }}>
        <TextField select fullWidth size="small" label={resolveMessage(MessageCodes.MAPEO_XSD_COMPANY_CONTEXT)} value={idEmpresa}
          onChange={(event) => { setIdEmpresa(event.target.value); setMapeosPendientes([]); }}
          disabled={cargando || guardando || empresas.length === 0}>
          <MenuItem value="">{resolveMessage(MessageCodes.COMMON_SELECT_OPTION)}</MenuItem>
          {empresas.map((empresa) => <MenuItem key={empresa.id} value={String(empresa.id)}>{empresa.razonSocial} · {empresa.ruc}</MenuItem>)}
        </TextField>
        <TextField select fullWidth size="small" label={resolveMessage(MessageCodes.MAPEO_XSD_SELECT_VERSION)} value={idVersionSeleccionada}
          onChange={(event) => { setIdVersionSeleccionada(event.target.value); setMapeosPendientes([]); }}
          disabled={cargando || cargandoDefinicion || guardando || !idEmpresa}>
          <MenuItem value="">{resolveMessage(MessageCodes.COMMON_SELECT_OPTION)}</MenuItem>
          {versiones.map((version) => <MenuItem key={version.id} value={String(version.id)}>{version.documentoCodigo} — {version.documentoNombre} · {version.version}</MenuItem>)}
        </TextField>
        {cargando && <CircularProgress size={24} />}
      </Box>

      {cargandoDefinicion && <CircularProgress />}

      {!cargandoDefinicion && (
        <>
          <Paper variant="outlined" className="catalogos-panel">
            <Box sx={{ px: 2, py: 1, bgcolor: "primary.main", color: "primary.contrastText" }}>
              <Typography variant="h6" component="h2">{definicion ? resolveMessage(MessageCodes.MAPEO_XSD_DEFINITION_TITLE, { codigo: definicion.documento.codigo, version: definicion.version.version }) : resolveMessage(MessageCodes.MAPEO_XSD_MAPPING_DESCRIPTION)}</Typography>
            </Box>
            <Box sx={{ p: 2 }}>
              <Box sx={{ display: "grid", gridTemplateColumns: { xs: "1fr", lg: "minmax(0, 1fr) minmax(0, 1fr)" }, gap: 2, alignItems: "start" }}>
                <Paper variant="outlined" sx={{ p: 0, minWidth: 0, overflow: "hidden" }}>
                  <Box sx={{ px: 2, py: 1, bgcolor: "secondary.main", color: "secondary.contrastText" }}><Typography variant="subtitle1" component="h3">{resolveMessage(MessageCodes.MAPEO_XSD_XML_TARGET)}</Typography></Box>
                  <Box sx={{ p: 2 }}>
                    <Box sx={{ display: "grid", gap: 0.75, maxHeight: 440, overflowY: "auto" }}>
                      {destinos.length === 0 ? <Typography color="text.secondary">{idVersionSeleccionada ? resolveMessage(MessageCodes.MAPEO_XSD_EMPTY) : resolveMessage(MessageCodes.MAPEO_XSD_SELECT_VERSION)}</Typography> : destinos.map((destino) => (
                        <Button key={destino.key} variant={formulario.destino === destino.key ? "contained" : "outlined"} disabled={guardando}
                          onClick={() => setFormulario((actual) => ({ ...actual, destino: destino.key }))}
                          sx={{ justifyContent: "flex-start", textAlign: "left", textTransform: "none" }}>{destino.nombre}</Button>
                      ))}
                    </Box>
                  </Box>
                </Paper>
                <Paper variant="outlined" sx={{ p: 0, minWidth: 0, overflow: "hidden" }}>
                  <Box sx={{ px: 2, py: 1, bgcolor: "secondary.main", color: "secondary.contrastText" }}><Typography variant="subtitle1" component="h3">Origen del dato</Typography></Box>
                  <Box sx={{ p: 2, display: "grid", gap: 2 }}>
                    <TextField select fullWidth label={resolveMessage(MessageCodes.MAPEO_XSD_SOURCE_TYPE)} value={formulario.tipoOrigen}
                      onChange={(event) => cambiarTipoOrigen(event.target.value)} disabled={guardando}>
                      <MenuItem value="">{resolveMessage(MessageCodes.COMMON_SELECT_OPTION)}</MenuItem>
                      <MenuItem value="JSON">JSON</MenuItem><MenuItem value="FACTUCORE">FACTUCORE</MenuItem><MenuItem value="GENERADO">GENERADO</MenuItem>
                    </TextField>
                    {formulario.tipoOrigen === "FACTUCORE" && <>
                      <TextField select fullWidth label={resolveMessage(MessageCodes.MAPEO_XSD_SOURCE_ENTITY)} value={formulario.codigoFuente}
                        onChange={(event) => setFormulario((actual) => ({ ...actual, codigoFuente: event.target.value, codigoCampo: "", codigoCatalogo: "" }))} disabled={guardando}>
                        <MenuItem value="">{resolveMessage(MessageCodes.COMMON_SELECT_OPTION)}</MenuItem>
                        {fuentes.map((fuente) => <MenuItem key={fuente.codigo} value={fuente.codigo}>{fuente.nombre}</MenuItem>)}
                      </TextField>
                      {fuentesDisponibles?.requiereCatalogo && <TextField select fullWidth label={resolveMessage(MessageCodes.MAPEO_XSD_SOURCE_CATALOG)}
                        value={formulario.codigoCatalogo} onChange={(event) => setFormulario((actual) => ({ ...actual, codigoCatalogo: event.target.value, codigoCampo: "" }))} disabled={guardando}>
                        <MenuItem value="">{resolveMessage(MessageCodes.COMMON_SELECT_OPTION)}</MenuItem>
                        {catalogos.map((catalogo) => <MenuItem key={catalogo.id} value={catalogo.codigo}>{catalogo.codigo} — {catalogo.nombre}</MenuItem>)}
                      </TextField>}
                      <TextField select fullWidth label={resolveMessage(MessageCodes.MAPEO_XSD_SOURCE_FIELD)} value={formulario.codigoCampo}
                        onChange={(event) => setFormulario((actual) => ({ ...actual, codigoCampo: event.target.value }))} disabled={guardando || !fuentesDisponibles}>
                        <MenuItem value="">{resolveMessage(MessageCodes.COMMON_SELECT_OPTION)}</MenuItem>
                        {camposDisponibles.map((campo) => <MenuItem key={campo.codigo} value={campo.codigo}>{campo.nombre} · {campo.tipoDato}</MenuItem>)}
                      </TextField>
                    </>}
                    {formulario.tipoOrigen === "JSON" && <>
                      <TextField select fullWidth label={resolveMessage(MessageCodes.MAPEO_XSD_JSON_PATH)} value={formulario.rutaJson}
                        onChange={(event) => setFormulario((actual) => ({ ...actual, rutaJson: event.target.value, origenGenerado: "" }))} disabled={guardando || rutasJson.length === 0}>
                        <MenuItem value="">{resolveMessage(MessageCodes.COMMON_SELECT_OPTION)}</MenuItem>
                        {rutasJson.map((ruta) => <MenuItem key={ruta} value={ruta}>{ruta}</MenuItem>)}
                      </TextField>
                    </>}
                    {formulario.tipoOrigen === "GENERADO" && <TextField fullWidth label={resolveMessage(MessageCodes.MAPEO_XSD_GENERATED_SOURCE)}
                      value={formulario.origenGenerado} onChange={(event) => setFormulario((actual) => ({ ...actual, origenGenerado: event.target.value }))}
                      disabled={guardando} inputProps={{ maxLength: 1000 }} />}
                  </Box>
                </Paper>
              </Box>
              <Box sx={{ p: 2, display: "flex", justifyContent: "flex-end" }}>
                <Button variant="contained" onClick={agregarMapeo} disabled={guardando || !definicion || !idEmpresa || !formulario.destino || !obtenerOrigen()}>Agregar</Button>
              </Box>
              <Paper variant="outlined" sx={{ overflow: "hidden" }}>
                <Box sx={{ px: 2, py: 1, bgcolor: "primary.main", color: "primary.contrastText" }}><Typography variant="h6">Mapeos por guardar ({mapeosPendientes.length})</Typography></Box>
                <TableContainer><Table size="small"><TableHead><TableRow><TableCell>Origen</TableCell><TableCell>Destino XML</TableCell><TableCell align="right">Acciones</TableCell></TableRow></TableHead>
                  <TableBody>{mapeosPendientes.map((mapeo, indice) => <TableRow key={mapeo.origen + indice}>
                    <TableCell>{mapeo.tipoOrigen}: {mapeo.origen}</TableCell>
                    <TableCell>{destinosBase.find((d) => mapeo.tipoMapeo === "ELEMENTO" ? d.idElementoXsd === mapeo.idElementoXsd : d.idAtributoXsd === mapeo.idAtributoXsd)?.nombre ?? "—"}</TableCell>
                    <TableCell align="right"><Button color="error" onClick={() => quitarMapeoPendiente(indice)} disabled={guardando}>Quitar</Button></TableCell>
                  </TableRow>)}</TableBody>
                </Table></TableContainer>
                <Box sx={{ p: 2, display: "flex", justifyContent: "flex-end" }}><Button variant="contained" onClick={() => void guardarMapeos()} disabled={guardando || mapeosPendientes.length === 0 || !idEmpresa || !idVersionSeleccionada}>{guardando ? resolveMessage(MessageCodes.COMMON_SAVING) : resolveMessage(MessageCodes.MAPEO_XSD_SAVE)}</Button></Box>
              </Paper>
            </Box>
          </Paper>

          <Paper variant="outlined" className="catalogos-panel" sx={{ overflow: "hidden" }}>
            <Box sx={{ px: 2, py: 1, bgcolor: "primary.main", color: "primary.contrastText" }}><Typography variant="h6" component="h2">{resolveMessage(MessageCodes.MAPEO_XSD_EXISTING_TITLE)}</Typography></Box>
            <Box sx={{ p: 2 }}>
            {mapeos.length === 0 ? (
              <Typography color="text.secondary">
                {resolveMessage(MessageCodes.MAPEO_XSD_EMPTY)}
              </Typography>
            ) : (
              <Box sx={{ display: "grid", gap: 1 }}>
                {mapeos.map((mapeo) => (
                  <Box
                    key={mapeo.id}
                    sx={{
                      display: "grid",
                      gridTemplateColumns: { xs: "1fr", md: "1fr 2fr" },
                      gap: 1,
                      borderBottom: 1,
                      borderColor: "divider",
                      pb: 1,
                    }}
                  >
                    <Typography>{mapeo.tipoOrigen}: {mapeo.origen}</Typography>
                    <Typography color="text.secondary">{descripcionDestino(mapeo)}</Typography>
                  </Box>
                ))}
              </Box>
            )}
            </Box>
          </Paper>
        </>
      )}
    </Box>
  );
}
