import { useCallback, useEffect, useMemo, useRef, useState } from "react";
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
import type { ConfiguracionEmpresa } from "../../configuracion-empresa/domain/ConfiguracionEmpresa";
import { MessageCodes } from "../../../shared/messages/MessageCodes";
import { resolveMessage } from "../../../shared/messages/MessageResolver";
import MapeoTree, { type MapeoTreeNode } from "./MapeoTree";
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
  elementoPadreId: number | null;
  nombre: string;
  tipoDato: string | null;
  orden: number | null;
  estadoRegistro?: string;
}

interface AtributoXsd {
  id: number;
  elementoXsdId: number;
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
  ocupado?: boolean;
}

interface FormularioMapeo {
  tipoOrigen: string;
  codigoFuente: string;
  codigoCampo: string;
  codigoCatalogo: string;
  rutaJson: string;
  origenGenerado: string;
  destino: string;
}

const FORMULARIO_INICIAL: FormularioMapeo = {
  tipoOrigen: "",
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
  const elementosPorId = new Map(elementos.map((actual) => [actual.id, actual]));
  const partes = [elemento.nombre];
  let padreId = elemento.elementoPadreId;
  const visitados = new Set<number>([elemento.id]);

  while (padreId !== null) {
    if (visitados.has(padreId)) break;
    visitados.add(padreId);
    const padre = elementosPorId.get(padreId);
    if (!padre) break;
    partes.unshift(padre.nombre);
    padreId = padre.elementoPadreId;
  }

  return partes.join(".");
}

function estaEnRamaExcluida(ruta: string): boolean {
  return ruta.split(".").some((segmento) => {
    const nombreLocal = segmento.trim().split(":").at(-1)?.toLowerCase();
    return nombreLocal === "signature";
  });
}

function esCampoAdicionalInformacionAdicional(ruta: string): boolean {
  const segmentos = ruta.split(".").map((segmento) => segmento.trim().split(":").at(-1)?.toLowerCase());
  return segmentos.at(-2) === "infoadicional" && segmentos.at(-1) === "campoadicional";
}

function registroActivo(estadoRegistro?: string): boolean {
  const estado = estadoRegistro?.trim().toUpperCase();
  return !estado || estado === EstadoRegistro.ACTIVO || estado === "ACTIVO";
}

function compararOrden(a: ElementoXsd, b: ElementoXsd): number {
  const orden = (a.orden ?? Number.MAX_SAFE_INTEGER) - (b.orden ?? Number.MAX_SAFE_INTEGER);
  return orden || a.id - b.id;
}

function crearDestinos(definicion: DefinicionDocumento): DestinoXsd[] {
  const elementosActivos = definicion.elementos.filter((elemento) => registroActivo(elemento.estadoRegistro));
  const hijosPorPadre = new Map<number | null, ElementoXsd[]>();
  for (const elemento of elementosActivos) {
    const hermanos = hijosPorPadre.get(elemento.elementoPadreId) ?? [];
    hermanos.push(elemento);
    hijosPorPadre.set(elemento.elementoPadreId, hermanos);
  }
  for (const hermanos of hijosPorPadre.values()) {
    hermanos.sort(compararOrden);
  }

  const atributosPorElemento = new Map<number, AtributoXsd[]>();
  for (const atributo of definicion.atributos) {
    if (!registroActivo(atributo.estadoRegistro)) continue;
    const atributos = atributosPorElemento.get(atributo.elementoXsdId) ?? [];
    atributos.push(atributo);
    atributosPorElemento.set(atributo.elementoXsdId, atributos);
  }

  const destinos: DestinoXsd[] = [];
  const visitados = new Set<number>();
  const agregarSubarbol = (elemento: ElementoXsd) => {
    if (visitados.has(elemento.id)) return;
    visitados.add(elemento.id);

    const ruta = rutaElemento(elemento, definicion.elementos);
    if (estaEnRamaExcluida(ruta)) return;

    const tipoDato = elemento.tipoDato?.trim().toUpperCase();
    const esEstructural = tipoDato === "MAP" || tipoDato === "LIST";
    if (elemento.elementoPadreId !== null && (!esEstructural || esCampoAdicionalInformacionAdicional(ruta))) {
      destinos.push({
        key: `elemento:${elemento.id}`,
        tipoMapeo: "ELEMENTO",
        idElementoXsd: elemento.id,
        idAtributoXsd: null,
        nombre: ruta,
      });
    }

    for (const hijo of hijosPorPadre.get(elemento.id) ?? []) {
      agregarSubarbol(hijo);
    }

    for (const atributo of atributosPorElemento.get(elemento.id) ?? []) {
      destinos.push({
        key: `atributo:${atributo.id}`,
        tipoMapeo: "ATRIBUTO",
        idElementoXsd: null,
        idAtributoXsd: atributo.id,
        nombre: `Atributo · ${ruta}.@${atributo.nombre}`,
      });
    }
  };

  for (const raiz of hijosPorPadre.get(null) ?? []) {
    agregarSubarbol(raiz);
  }
  for (const elemento of elementosActivos.slice().sort(compararOrden)) {
    agregarSubarbol(elemento);
  }

  return destinos;
}


function extraerRutasJson(valor: unknown, prefijo = "", rutas: string[] = []): string[] {
  if (valor === null || typeof valor !== "object") {
    if (prefijo) rutas.push(prefijo);
    return rutas;
  }
  if (Array.isArray(valor)) {
    if (valor.length === 0) { if (prefijo) rutas.push(prefijo); return rutas; }
    for (const elemento of valor) extraerRutasJson(elemento, prefijo, rutas);
    return [...new Set(rutas)];
  }
  const entradas = Object.entries(valor as Record<string, unknown>);
  if (entradas.length === 0 && prefijo) rutas.push(prefijo);
  for (const [clave, contenido] of entradas) {
    extraerRutasJson(contenido, prefijo ? `${prefijo}.${clave}` : clave, rutas);
  }
  return [...new Set(rutas)];
}

function normalizarRutaJsonParaMapeo(ruta: string): string {
  const segmentos = ruta.split(".").filter(Boolean);
  const indiceComprobante = segmentos.lastIndexOf("comprobante");
  if (indiceComprobante >= 0) return segmentos.slice(indiceComprobante + 1).join(".");
  if (segmentos[0] === "datos") return segmentos.slice(1).join(".");
  return segmentos.join(".");
}

function crearArbolJson(rutas: string[]): MapeoTreeNode[] {
  interface NodoMutable {
    key: string;
    label: string;
    path: string;
    children: Map<string, NodoMutable>;
  }
  const raices = new Map<string, NodoMutable>();
  for (const ruta of rutas) {
    const segmentos = ruta.split(".").filter(Boolean);
    let nivel = raices;
    let prefijo = "";
    for (const segmento of segmentos) {
      prefijo = prefijo ? `${prefijo}.${segmento}` : segmento;
      let nodo = nivel.get(segmento);
      if (!nodo) {
        nodo = { key: prefijo, label: segmento, path: prefijo, children: new Map() };
        nivel.set(segmento, nodo);
      }
      nivel = nodo.children;
    }
  }
  const convertir = (nodo: NodoMutable): MapeoTreeNode => ({
    key: nodo.key,
    label: nodo.label,
    path: nodo.path,
    children: [...nodo.children.values()].map(convertir),
  });
  return [...raices.values()].map(convertir);
}

function crearArbolXsd(
  definicion: DefinicionDocumento | null,
  destinos: DestinoXsd[],
): MapeoTreeNode[] {
  if (!definicion) return [];
  const elementosActivos = definicion.elementos.filter((elemento) =>
    registroActivo(elemento.estadoRegistro),
  );
  const hijosPorPadre = new Map<number | null, ElementoXsd[]>();
  for (const elemento of elementosActivos) {
    const hijos = hijosPorPadre.get(elemento.elementoPadreId) ?? [];
    hijos.push(elemento);
    hijosPorPadre.set(elemento.elementoPadreId, hijos);
  }
  for (const hijos of hijosPorPadre.values()) hijos.sort(compararOrden);

  const destinosPorElemento = new Map<number, DestinoXsd>();
  const destinosPorAtributo = new Map<number, DestinoXsd>();
  for (const destino of destinos) {
    if (destino.idElementoXsd !== null) destinosPorElemento.set(destino.idElementoXsd, destino);
    if (destino.idAtributoXsd !== null) destinosPorAtributo.set(destino.idAtributoXsd, destino);
  }
  const atributosPorElemento = new Map<number, AtributoXsd[]>();
  for (const atributo of definicion.atributos) {
    if (!registroActivo(atributo.estadoRegistro)) continue;
    const atributos = atributosPorElemento.get(atributo.elementoXsdId) ?? [];
    atributos.push(atributo);
    atributosPorElemento.set(atributo.elementoXsdId, atributos);
  }

  const crearNodo = (elemento: ElementoXsd, visitados: Set<number>): MapeoTreeNode | null => {
    if (visitados.has(elemento.id)) return null;
    const visitadosActuales = new Set(visitados).add(elemento.id);
    const ruta = rutaElemento(elemento, elementosActivos);
    if (estaEnRamaExcluida(ruta)) return null;
    const destino = destinosPorElemento.get(elemento.id);
    const hijos = (hijosPorPadre.get(elemento.id) ?? [])
      .map((hijo) => crearNodo(hijo, visitadosActuales))
      .filter((hijo): hijo is MapeoTreeNode => hijo !== null);
    for (const atributo of atributosPorElemento.get(elemento.id) ?? []) {
      const destinoAtributo = destinosPorAtributo.get(atributo.id);
      if (!destinoAtributo) continue;
      hijos.push({
        key: destinoAtributo.key,
        label: `@${atributo.nombre}`,
        path: `${ruta}.@${atributo.nombre}`,
        destinoKey: destinoAtributo.key,
        children: [],
      });
    }
    return {
      key: destino?.key ?? `elemento:${elemento.id}`,
      label: elemento.nombre,
      path: ruta,
      destinoKey: destino?.key,
      children: hijos,
    };
  };

  return (hijosPorPadre.get(null) ?? [])
    .map((elemento) => crearNodo(elemento, new Set()))
    .filter((nodo): nodo is MapeoTreeNode => nodo !== null);
}

function obtenerNodosRaizSeleccionados(
  nodos: MapeoTreeNode[],
  seleccionados: Set<string>,
): MapeoTreeNode[] {
  const raices: MapeoTreeNode[] = [];
  const visitar = (nodo: MapeoTreeNode, padreSeleccionado: boolean) => {
    const seleccionado = seleccionados.has(nodo.key);
    if (seleccionado && !padreSeleccionado) {
      raices.push(nodo);
      return;
    }
    for (const hijo of nodo.children) visitar(hijo, seleccionado);
  };
  for (const nodo of nodos) visitar(nodo, false);
  return raices;
}

function obtenerClavesArbol(nodo: MapeoTreeNode): string[] {
  return [nodo.key, ...nodo.children.flatMap(obtenerClavesArbol)];
}

function obtenerRutasArbol(nodos: MapeoTreeNode[]): string[] {
  return nodos.flatMap((nodo) => [
    nodo.path ?? nodo.key,
    ...obtenerRutasArbol(nodo.children),
  ]);
}

function esDestinoIndividualSeleccionado(
  nodo: MapeoTreeNode,
  destinosOcupados: Set<string>,
): boolean {
  if (!nodo.destinoKey) return false;
  if (nodo.children.length === 0) return true;
  return esCampoAdicionalInformacionAdicional(nodo.path ?? "")
    && nodo.children.every((hijo) =>
      Boolean(hijo.destinoKey && destinosOcupados.has(hijo.destinoKey)),
    );
}

function esDestinoAutomatico(destino: DestinoXsd): boolean {
  const ruta = destino.nombre.toLowerCase();
  return !ruta.split(".").some((segmento) => segmento === "infotributaria");
}

function obtenerSufijoJsonDestino(
  destino: DestinoXsd,
  rutaDestino: string,
  rutaRaiz: string,
): string {
  const sufijo = rutaDestino.slice(rutaRaiz.length).replace(/\.@([^.]*)/g, ".$1");
  return destino.tipoMapeo === "ELEMENTO"
    && esCampoAdicionalInformacionAdicional(rutaDestino)
    ? `${sufijo}.valor`
    : sufijo;
}

function obtenerRutaDestino(destino: DestinoXsd, definicion: DefinicionDocumento): string {
  if (destino.idElementoXsd !== null) {
    const elemento = definicion.elementos.find((actual) => actual.id === destino.idElementoXsd);
    return elemento ? rutaElemento(elemento, definicion.elementos) : destino.nombre;
  }
  const atributo = definicion.atributos.find((actual) => actual.id === destino.idAtributoXsd);
  const elemento = definicion.elementos.find((actual) => actual.id === atributo?.elementoXsdId);
  return atributo && elemento
    ? `${rutaElemento(elemento, definicion.elementos)}.@${atributo.nombre}`
    : destino.nombre;
}

export default function MapeosXsdPage() {
  const [, setSearchParams] = useSearchParams();
  const [versiones, setVersiones] = useState<VersionSeleccionable[]>([]);
  const [empresas, setEmpresas] = useState<Empresa[]>([]);
  const [catalogos, setCatalogos] = useState<Catalogo[]>([]);
  const [configuracionesEmpresa, setConfiguracionesEmpresa] = useState<ConfiguracionEmpresa[]>([]);
  const [plantillasJsonPorVersion, setPlantillasJsonPorVersion] = useState<Record<string, string>>({});
  const [fuentes, setFuentes] = useState<FuenteFactuCore[]>([]);
  const [definicion, setDefinicion] = useState<DefinicionDocumento | null>(null);
  const [mapeos, setMapeos] = useState<MapeoXsd[]>([]);
  const [mapeosPendientes, setMapeosPendientes] = useState<SolicitudMapeoXsd[]>([]);
  const [idVersionSeleccionada, setIdVersionSeleccionada] = useState("");
  const [idEmpresa, setIdEmpresa] = useState("");
  const [formulario, setFormulario] = useState<FormularioMapeo>(FORMULARIO_INICIAL);
  const [nodosXsdSeleccionados, setNodosXsdSeleccionados] = useState<Set<string>>(new Set());
  const [nodosJsonSeleccionados, setNodosJsonSeleccionados] = useState<Set<string>>(new Set());
  const [destinosSinMapear, setDestinosSinMapear] = useState<string[]>([]);
  const [cargando, setCargando] = useState(true);
  const [cargandoDefinicion, setCargandoDefinicion] = useState(false);
  const [cargandoConfiguracionesEmpresa, setCargandoConfiguracionesEmpresa] = useState(false);
  const [guardando, setGuardando] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [aviso, setAviso] = useState<string | null>(null);
  const plantillasJsonEnCargaRef = useRef(new Map<string, Promise<string>>());
  const config = getRuntimeConfig();
  const documentosPath = config.API_DOCUMENTOS_XSD_PATH.replace(/^\/+|\/+$/g, "");
  const mapeosPath = config.API_MAPEOS_XSD_PATH.replace(/^\/+|\/+$/g, "");

  const camposConfiguracionEmpresa = useMemo(() => {
    const ahora = Date.now();
    const vigentes = configuracionesEmpresa
      .filter((configuracion) => {
        if (!registroActivo(configuracion.estadoRegistro)) return false;
        const desde = Date.parse(configuracion.fechaVigenciaDesde);
        const hasta = configuracion.fechaVigenciaHasta
          ? Date.parse(configuracion.fechaVigenciaHasta)
          : Number.POSITIVE_INFINITY;
        return Number.isFinite(desde) && desde <= ahora && hasta >= ahora;
      })
      .sort(
        (a, b) =>
          Date.parse(b.fechaVigenciaDesde) - Date.parse(a.fechaVigenciaDesde) || b.id - a.id,
      );
    const configuracionesPorClave = new Map<string, ConfiguracionEmpresa>();
    for (const configuracion of vigentes) {
      if (!configuracionesPorClave.has(configuracion.clave)) {
        configuracionesPorClave.set(configuracion.clave, configuracion);
      }
    }
    return Array.from(configuracionesPorClave.values()).map((configuracion) => ({
      codigo: configuracion.clave,
      nombre: configuracion.clave,
      tipoDato: configuracion.tipoDato,
      origen: `configuracion_empresa.${configuracion.clave}`,
    }));
  }, [configuracionesEmpresa]);
  const fuentesParaSelector = useMemo(
    () => [
      ...fuentes,
      {
        codigo: "configuracion_empresa",
        nombre: resolveMessage(MessageCodes.MAPEO_XSD_SOURCE_COMPANY_CONFIGURATION),
        requiereCatalogo: false,
        campos: camposConfiguracionEmpresa,
      },
    ],
    [camposConfiguracionEmpresa, fuentes],
  );
  const destinosBase = useMemo(() => (definicion ? crearDestinos(definicion) : []), [definicion]);
  const destinosOcupados = new Set(
    [...mapeos, ...mapeosPendientes].map((mapeo) =>
      mapeo.tipoMapeo === "ELEMENTO"
        ? `elemento:${mapeo.idElementoXsd}`
        : `atributo:${mapeo.idAtributoXsd}`,
    ),
  );
  const destinos = destinosBase.filter((destino) => !destinosOcupados.has(destino.key));
  const fuentesDisponibles = useMemo(
    () => fuentesParaSelector.find((fuente) => fuente.codigo === formulario.codigoFuente) ?? null,
    [fuentesParaSelector, formulario.codigoFuente],
  );
  const camposDisponibles = fuentesDisponibles?.campos ?? [];
  const campoSeleccionado = camposDisponibles.find(
    (campo) => campo.codigo === formulario.codigoCampo,
  );
  const catalogoSeleccionado = catalogos.find(
    (catalogo) => catalogo.codigo === formulario.codigoCatalogo,
  );
  const destinoSeleccionado = destinos.find((destino) => destino.key === formulario.destino);
  const ejemploJson = plantillasJsonPorVersion[idVersionSeleccionada] ?? "";
  const plantillaJson = useMemo(() => {
    if (!ejemploJson.trim()) return null;
    try { return JSON.parse(ejemploJson) as unknown; } catch { return null; }
  }, [ejemploJson]);
  const rutasJson = useMemo(() => plantillaJson === null ? [] : extraerRutasJson(plantillaJson), [plantillaJson]);
  const arbolJson = useMemo(() => crearArbolJson(rutasJson), [rutasJson]);
  const rutasJsonSeleccionables = useMemo(
    () => new Set(obtenerRutasArbol(arbolJson).map(normalizarRutaJsonParaMapeo).filter(Boolean)),
    [arbolJson],
  );
  const rutasJsonSeleccionadas = useMemo(
    () => new Set([...nodosJsonSeleccionados].map(normalizarRutaJsonParaMapeo).filter(Boolean)),
    [nodosJsonSeleccionados],
  );
  const arbolXsd = useMemo(
    () => crearArbolXsd(definicion, destinosBase),
    [definicion, destinosBase],
  );
  const raicesXsdSeleccionadas = useMemo(
    () => obtenerNodosRaizSeleccionados(arbolXsd, nodosXsdSeleccionados),
    [arbolXsd, nodosXsdSeleccionados],
  );
  const haySeleccionSeccionJson = formulario.tipoOrigen === "JSON"
    && raicesXsdSeleccionadas.length > 0
    && !(raicesXsdSeleccionadas.length === 1
      && esDestinoIndividualSeleccionado(raicesXsdSeleccionadas[0], destinosOcupados));

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
      setNodosXsdSeleccionados(new Set());
      setNodosJsonSeleccionados(new Set());
      setDestinosSinMapear([]);
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
      setNodosXsdSeleccionados(new Set());
      setNodosJsonSeleccionados(new Set());
      setDestinosSinMapear([]);
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
    if (!idEmpresa) {
      setConfiguracionesEmpresa([]);
      setCargandoConfiguracionesEmpresa(false);
      return;
    }

    let vigente = true;
    setCargandoConfiguracionesEmpresa(true);
    setError(null);
    apiRequest<ConfiguracionEmpresa[]>({
      method: "GET",
      url: config.API_CONFIGURACIONES_EMPRESA_PATH,
      params: { idEmpresa: Number(idEmpresa) },
    })
      .then((configuraciones) => {
        if (vigente) setConfiguracionesEmpresa(configuraciones);
      })
      .catch((exception: unknown) => {
        if (vigente) {
          setConfiguracionesEmpresa([]);
          setError(mensajeError(exception));
        }
      })
      .finally(() => {
        if (vigente) setCargandoConfiguracionesEmpresa(false);
      });

    return () => {
      vigente = false;
    };
  }, [config.API_CONFIGURACIONES_EMPRESA_PATH, idEmpresa]);

  useEffect(() => {
    if (idVersionSeleccionada) {
      setSearchParams({ versionId: idVersionSeleccionada }, { replace: true });
      void cargarVersion(idVersionSeleccionada);
    } else {
      setSearchParams({}, { replace: true });
      setDefinicion(null);
      setMapeos([]);
      setMapeosPendientes([]);
      setNodosXsdSeleccionados(new Set());
      setNodosJsonSeleccionados(new Set());
      setDestinosSinMapear([]);
    }
  }, [cargarVersion, idVersionSeleccionada, setSearchParams]);

  useEffect(() => {
    if (!idVersionSeleccionada || plantillasJsonPorVersion[idVersionSeleccionada]) return;
    let vigente = true;
    let solicitud = plantillasJsonEnCargaRef.current.get(idVersionSeleccionada);

    if (!solicitud) {
      solicitud = apiRequestRaw<string>({
        method: "GET",
        url: `${documentosPath}/versiones/${idVersionSeleccionada}/plantilla-json`,
        responseType: "text",
      })
        .then((respuesta) => {
          const plantilla = typeof respuesta === "string" ? respuesta : JSON.stringify(respuesta);
          JSON.parse(plantilla);
          setPlantillasJsonPorVersion((actuales) => ({
            ...actuales,
            [idVersionSeleccionada]: plantilla,
          }));
          return plantilla;
        })
        .finally(() => {
          plantillasJsonEnCargaRef.current.delete(idVersionSeleccionada);
        });
      plantillasJsonEnCargaRef.current.set(idVersionSeleccionada, solicitud);
    }

    solicitud.catch((exception: unknown) => {
      if (vigente) setError(mensajeError(exception));
    });

    return () => {
      vigente = false;
    };
  }, [documentosPath, idVersionSeleccionada, plantillasJsonPorVersion]);
  const cambiarTipoOrigen = (tipoOrigen: string) => {
    setFormulario((actual) => ({
      ...FORMULARIO_INICIAL,
      tipoOrigen,
      destino: actual.destino,
    }));
    setNodosJsonSeleccionados(new Set());
  };

  const cambiarSeleccionXsd = (nodo: MapeoTreeNode, seleccionado: boolean) => {
    const claves = obtenerClavesArbol(nodo);
    const esDestinoIndividual = esDestinoIndividualSeleccionado(nodo, destinosOcupados);
    setNodosXsdSeleccionados((actuales) => {
      const nuevas = new Set(actuales);
      for (const clave of claves) {
        if (seleccionado && !destinosOcupados.has(clave)) nuevas.add(clave);
        else nuevas.delete(clave);
      }
      return nuevas;
    });
    if (nodo.children.length > 0 && seleccionado && !esDestinoIndividual) {
      setFormulario((actual) => ({ ...actual, destino: "" }));
    } else if (nodo.destinoKey) {
      setFormulario((actual) => ({
        ...actual,
        destino: seleccionado ? nodo.destinoKey ?? "" : actual.destino === nodo.destinoKey ? "" : actual.destino,
      }));
    }
    setError(null);
    setAviso(null);
    setDestinosSinMapear([]);
  };

  const cambiarSeleccionJson = (nodo: MapeoTreeNode, seleccionado: boolean) => {
    const claves = obtenerClavesArbol(nodo);
    const rutaNodo = normalizarRutaJsonParaMapeo(nodo.path ?? nodo.key);
    const raizActual = formulario.rutaJson.trim();
    const perteneceALaRaizActual = raizActual
      && (rutaNodo === raizActual || rutaNodo.startsWith(`${raizActual}.`));
    setNodosJsonSeleccionados((actuales) => {
      const nuevas = new Set(actuales);
      if (seleccionado) {
        if (raizActual && !perteneceALaRaizActual) nuevas.clear();
        for (const clave of claves) nuevas.add(clave);
      } else {
        for (const clave of claves) nuevas.delete(clave);
      }
      return nuevas;
    });
    if (seleccionado && (!raizActual || !perteneceALaRaizActual)) {
      setFormulario((actual) => ({ ...actual, rutaJson: rutaNodo }));
    } else if (
      !seleccionado
      && raizActual
      && (rutaNodo === raizActual || raizActual.startsWith(`${rutaNodo}.`))
    ) {
      setFormulario((actual) => ({ ...actual, rutaJson: "" }));
    }
    setError(null);
    setAviso(null);
  };

  const obtenerOrigen = (): string => {
    if (formulario.tipoOrigen === "JSON") {
      if (formulario.rutaJson.trim()) {
        return rutasJsonSeleccionables.has(formulario.rutaJson.trim())
          ? formulario.rutaJson.trim()
          : "";
      }
      if (!destinoSeleccionado || !definicion) return "";
      const ruta = obtenerRutaDestino(destinoSeleccionado, definicion);
      return rutasJsonSeleccionables.has(ruta) ? ruta : "";
    }
    if (formulario.tipoOrigen === "GENERADO") return formulario.origenGenerado.trim();
    if (formulario.tipoOrigen === "FACTUCORE" && campoSeleccionado) {
      if (fuentesDisponibles?.requiereCatalogo && catalogoSeleccionado) {
        return campoSeleccionado.origen.replace("{codigoCatalogo}", catalogoSeleccionado.codigo);
      }
      return campoSeleccionado.origen;
    }
    return "";
  };

  const agregarMapeoJsonDesdeArbol = (): boolean => {
    const raices = raicesXsdSeleccionadas;
    const esUnaHojaIndividual = raices.length === 1
      && esDestinoIndividualSeleccionado(raices[0], destinosOcupados);
    if (formulario.tipoOrigen !== "JSON" || raices.length === 0 || esUnaHojaIndividual) return false;
    if (!definicion || !idVersionSeleccionada) {
      setError(resolveMessage(MessageCodes.MAPEO_XSD_REQUIRED_FIELDS));
      return true;
    }
    if (!idEmpresa) {
      setError(resolveMessage(MessageCodes.MAPEO_XSD_COMPANY_REQUIRED));
      return true;
    }

    const jsonRaiz = formulario.rutaJson.trim();
    const origenesUsados = new Set([
      ...mapeos.map((mapeo) => mapeo.origen),
      ...mapeosPendientes.map((mapeo) => mapeo.origen),
    ]);
    const destinosUsados = new Set(destinosOcupados);
    const nuevosMapeos: SolicitudMapeoXsd[] = [];
    const sinRuta: string[] = [];

    for (const raiz of raices) {
      const rutaRaiz = raiz.path ?? "";
      if (!rutaRaiz) continue;
      const esHojaDestino = Boolean(raiz.destinoKey) && raiz.children.length === 0;
      const candidatos = destinosBase.filter((destino) => {
        if (destinosUsados.has(destino.key)) return false;
        if (!nodosXsdSeleccionados.has(destino.key)) return false;
        const ruta = obtenerRutaDestino(destino, definicion);
        const pertenece = ruta === rutaRaiz || ruta.startsWith(`${rutaRaiz}.`);
        return pertenece && (esHojaDestino ? destino.key === raiz.destinoKey : esDestinoAutomatico(destino));
      });

      if (jsonRaiz && !esHojaDestino) {
        const nombreXsd = rutaRaiz.split(".").at(-1)?.toLowerCase();
        const nombreJson = jsonRaiz.split(".").at(-1)?.toLowerCase();
        if (nombreXsd && nombreJson && nombreXsd !== nombreJson) {
          sinRuta.push(...candidatos.map((destino) => destino.nombre));
          continue;
        }
      }

      for (const destino of candidatos) {
        const rutaDestino = obtenerRutaDestino(destino, definicion);
        const sufijo = esHojaDestino
          ? ""
          : obtenerSufijoJsonDestino(destino, rutaDestino, rutaRaiz);
        const origen = `${jsonRaiz || rutaRaiz}${sufijo}`;
        if (
          !rutasJsonSeleccionables.has(origen)
          || (jsonRaiz && !rutasJsonSeleccionadas.has(origen))
          || origenesUsados.has(origen)
        ) {
          sinRuta.push(destino.nombre);
          continue;
        }
        nuevosMapeos.push({
          idVersionDocumentoXsd: Number(idVersionSeleccionada),
          tipoOrigen: "JSON",
          origen,
          idElementoXsd: destino.idElementoXsd,
          idAtributoXsd: destino.idAtributoXsd,
          tipoMapeo: destino.tipoMapeo,
        });
        origenesUsados.add(origen);
        destinosUsados.add(destino.key);
      }
    }

    if (nuevosMapeos.length > 0) {
      setMapeosPendientes((actuales) => [...actuales, ...nuevosMapeos]);
      setAviso(resolveMessage(MessageCodes.MAPEO_XSD_TREE_ADDED, { total: nuevosMapeos.length }));
    }
    if (sinRuta.length > 0) {
      setDestinosSinMapear([...new Set(sinRuta)]);
      setError(resolveMessage(MessageCodes.MAPEO_XSD_JSON_PATH_NOT_FOUND, { total: new Set(sinRuta).size }));
    } else if (nuevosMapeos.length === 0) {
      setError(resolveMessage(MessageCodes.MAPEO_XSD_TREE_NO_TARGETS));
    } else {
      setError(null);
      setNodosXsdSeleccionados(new Set());
      setNodosJsonSeleccionados(new Set());
      setFormulario((actual) => ({ ...actual, destino: "", rutaJson: "" }));
    }
    return true;
  };

  const agregarMapeo = () => {
    setError(null);
    setAviso(null);
    setDestinosSinMapear([]);
    if (agregarMapeoJsonDesdeArbol()) return;
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
    setFormulario((actual) => ({
      ...FORMULARIO_INICIAL,
      tipoOrigen: actual.tipoOrigen,
    }));
    setNodosXsdSeleccionados(new Set());
    setNodosJsonSeleccionados(new Set());
  };

  const quitarMapeoPendiente = (indice: number) => {
    setMapeosPendientes((actuales) => actuales.filter((_, i) => i !== indice));
    setError(null);
    setDestinosSinMapear([]);
  };

  const guardarMapeos = async () => {
    setError(null);
    setAviso(null);
    setDestinosSinMapear([]);
    if (!idEmpresa || !idVersionSeleccionada) {
      setError(resolveMessage(MessageCodes.MAPEO_XSD_REQUIRED_FIELDS));
      return;
    }
    const destinosMapeados = new Set(destinosOcupados);
    const faltantes = destinosBase.filter((destino) => !destinosMapeados.has(destino.key));
    if (faltantes.length > 0) {
      setDestinosSinMapear(faltantes.map((destino) => destino.nombre));
      setError(resolveMessage(MessageCodes.MAPEO_XSD_UNMAPPED_TARGETS, { total: faltantes.length }));
      return;
    }
    if (mapeosPendientes.length === 0) {
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
      setNodosXsdSeleccionados(new Set());
      setNodosJsonSeleccionados(new Set());
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
    const elemento = definicion?.elementos.find((actual) => actual.id === atributo?.elementoXsdId);
    return atributo
      ? `${elemento ? rutaElemento(elemento, definicion?.elementos ?? []) : ""}.@${atributo.nombre}`
      : String(mapeo.idAtributoXsd);
  };
  const mapeosVisibles = mapeos.filter((mapeo) => !estaEnRamaExcluida(descripcionDestino(mapeo)));

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

      {error && (
        <Alert severity="error" onClose={() => { setError(null); setDestinosSinMapear([]); }}>
          {error}
          {destinosSinMapear.length > 0 && (
            <Box component="ul" sx={{ mt: 1, mb: 0, pl: 2.5, maxHeight: 220, overflowY: "auto" }}>
              {destinosSinMapear.map((destino) => <li key={destino}>{destino}</li>)}
            </Box>
          )}
        </Alert>
      )}
      {aviso && <Alert severity="success" onClose={() => setAviso(null)}>{aviso}</Alert>}

      <Box sx={{ display: "grid", gridTemplateColumns: { xs: "1fr", md: "minmax(240px, 0.8fr) minmax(320px, 1.5fr)" }, gap: 2, alignItems: "center" }}>
        <TextField select fullWidth size="small" label={resolveMessage(MessageCodes.MAPEO_XSD_COMPANY_CONTEXT)} value={idEmpresa}
          onChange={(event) => {
            setIdEmpresa(event.target.value);
            setMapeosPendientes([]);
            setFormulario(FORMULARIO_INICIAL);
            setNodosXsdSeleccionados(new Set());
            setNodosJsonSeleccionados(new Set());
            setDestinosSinMapear([]);
            setError(null);
          }}
          disabled={cargando || guardando || empresas.length === 0}>
          <MenuItem value="">{resolveMessage(MessageCodes.COMMON_SELECT_OPTION)}</MenuItem>
          {empresas.map((empresa) => <MenuItem key={empresa.id} value={String(empresa.id)}>{empresa.razonSocial} · {empresa.ruc}</MenuItem>)}
        </TextField>
        <TextField select fullWidth size="small" label={resolveMessage(MessageCodes.MAPEO_XSD_SELECT_VERSION)} value={idVersionSeleccionada}
          onChange={(event) => {
            setIdVersionSeleccionada(event.target.value);
            setMapeosPendientes([]);
            setNodosXsdSeleccionados(new Set());
            setNodosJsonSeleccionados(new Set());
            setDestinosSinMapear([]);
            setError(null);
          }}
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
            <Box sx={{ p: 1.5 }}>
              <Box sx={{ display: "grid", gridTemplateColumns: { xs: "1fr", lg: "minmax(0, 1fr) minmax(0, 1fr)" }, gap: 1.5, alignItems: "start" }}>
                <Paper variant="outlined" sx={{ p: 0, minWidth: 0, overflow: "hidden" }}>
                  <Box sx={{ px: 1.5, py: 0.75, bgcolor: "secondary.main", color: "secondary.contrastText" }}><Typography variant="subtitle1" component="h3">{resolveMessage(MessageCodes.MAPEO_XSD_XML_TARGET)}</Typography></Box>
                  <Box sx={{ p: 1.25 }}>
                    <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
                      {resolveMessage(
                        formulario.tipoOrigen === "JSON"
                          ? MessageCodes.MAPEO_XSD_XSD_TREE_HELP
                          : MessageCodes.MAPEO_XSD_XSD_TREE_MANUAL_HELP,
                      )}
                    </Typography>
                    <Box sx={{ maxHeight: 320, overflowY: "auto" }}>
                      {arbolXsd.length === 0
                        ? <Typography color="text.secondary">{idVersionSeleccionada ? resolveMessage(MessageCodes.MAPEO_XSD_EMPTY) : resolveMessage(MessageCodes.MAPEO_XSD_SELECT_VERSION)}</Typography>
                        : <MapeoTree
                          nodes={arbolXsd}
                          selectedKeys={nodosXsdSeleccionados}
                          disabledKeys={destinosOcupados}
                          selectionMode="cascade"
                          onSelectionChange={cambiarSeleccionXsd}
                        />}
                    </Box>
                  </Box>
                </Paper>
                <Paper variant="outlined" sx={{ p: 0, minWidth: 0, overflow: "hidden" }}>
                  <Box sx={{ px: 1.5, py: 0.75, bgcolor: "secondary.main", color: "secondary.contrastText" }}><Typography variant="subtitle1" component="h3">Origen del dato</Typography></Box>
                  <Box sx={{ p: 1.25, display: "grid", gap: 1.25 }}>
                    <TextField select fullWidth label={resolveMessage(MessageCodes.MAPEO_XSD_SOURCE_TYPE)} value={formulario.tipoOrigen}
                      onChange={(event) => cambiarTipoOrigen(event.target.value)} disabled={guardando}>
                      <MenuItem value="">{resolveMessage(MessageCodes.COMMON_SELECT_OPTION)}</MenuItem>
                      <MenuItem value="JSON">JSON</MenuItem><MenuItem value="FACTUCORE">FACTUCORE</MenuItem><MenuItem value="GENERADO">GENERADO</MenuItem>
                    </TextField>
                    {formulario.tipoOrigen === "FACTUCORE" && <>
                      <TextField select fullWidth label={resolveMessage(MessageCodes.MAPEO_XSD_SOURCE_ENTITY)} value={formulario.codigoFuente}
                        onChange={(event) => setFormulario((actual) => ({ ...actual, codigoFuente: event.target.value, codigoCampo: "", codigoCatalogo: "" }))} disabled={guardando}>
                        <MenuItem value="">{resolveMessage(MessageCodes.COMMON_SELECT_OPTION)}</MenuItem>
                        {fuentesParaSelector.map((fuente) => <MenuItem key={fuente.codigo} value={fuente.codigo}>{fuente.nombre}</MenuItem>)}
                      </TextField>
                      {fuentesDisponibles?.requiereCatalogo && <TextField select fullWidth label={resolveMessage(MessageCodes.MAPEO_XSD_SOURCE_CATALOG)}
                        value={formulario.codigoCatalogo} onChange={(event) => setFormulario((actual) => ({ ...actual, codigoCatalogo: event.target.value, codigoCampo: "" }))} disabled={guardando}>
                        <MenuItem value="">{resolveMessage(MessageCodes.COMMON_SELECT_OPTION)}</MenuItem>
                        {catalogos.map((catalogo) => <MenuItem key={catalogo.id} value={catalogo.codigo}>{catalogo.codigo} — {catalogo.nombre}</MenuItem>)}
                      </TextField>}
                      <TextField select fullWidth label={resolveMessage(MessageCodes.MAPEO_XSD_SOURCE_FIELD)} value={formulario.codigoCampo}
                        onChange={(event) => setFormulario((actual) => ({ ...actual, codigoCampo: event.target.value }))} disabled={guardando || cargandoConfiguracionesEmpresa || !fuentesDisponibles}>
                        <MenuItem value="">{resolveMessage(MessageCodes.COMMON_SELECT_OPTION)}</MenuItem>
                        {camposDisponibles.map((campo) => <MenuItem key={campo.codigo} value={campo.codigo}>{campo.nombre} · {campo.tipoDato}</MenuItem>)}
                      </TextField>
                    </>}
                    {formulario.tipoOrigen === "JSON" && <>
                      <Typography variant="body2" color="text.secondary">
                        {resolveMessage(MessageCodes.MAPEO_XSD_JSON_TREE_HELP)}
                      </Typography>
                      <Box sx={{ maxHeight: 320, overflowY: "auto", border: 1, borderColor: "divider", borderRadius: 1, p: 1 }}>
                        {arbolJson.length === 0
                          ? <Typography color="text.secondary">{resolveMessage(MessageCodes.MAPEO_XSD_JSON_TREE_EMPTY)}</Typography>
                          : <MapeoTree
                            nodes={arbolJson}
                            selectedKeys={nodosJsonSeleccionados}
                            selectionMode="cascade"
                            onSelectionChange={cambiarSeleccionJson}
                          />}
                      </Box>
                    </>}
                    {formulario.tipoOrigen === "GENERADO" && <TextField fullWidth label={resolveMessage(MessageCodes.MAPEO_XSD_GENERATED_SOURCE)}
                      value={formulario.origenGenerado} onChange={(event) => setFormulario((actual) => ({ ...actual, origenGenerado: event.target.value }))}
                      disabled={guardando} inputProps={{ maxLength: 1000 }} />}
                  </Box>
                </Paper>
              </Box>
              <Box sx={{ pt: 1, pb: 1.5, display: "flex", justifyContent: "flex-end" }}>
                <Button variant="contained" onClick={agregarMapeo} disabled={guardando || !definicion || !idEmpresa || (!haySeleccionSeccionJson && (!formulario.destino || !obtenerOrigen()))}>Agregar</Button>
              </Box>
              <Paper variant="outlined" sx={{ overflow: "hidden" }}>
                <Box sx={{ px: 1.5, py: 0.5, bgcolor: "primary.main", color: "primary.contrastText" }}><Typography variant="subtitle1">Mapeos por guardar ({mapeosPendientes.length})</Typography></Box>
                <TableContainer><Table size="small"><TableHead><TableRow><TableCell sx={{ py: 0.5, px: 1 }}>Origen</TableCell><TableCell sx={{ py: 0.5, px: 1 }}>Destino XML</TableCell><TableCell align="right" sx={{ py: 0.5, px: 1 }}>Acciones</TableCell></TableRow></TableHead>
                  <TableBody>{mapeosPendientes.map((mapeo, indice) => <TableRow key={mapeo.origen + indice}>
                    <TableCell sx={{ py: 0.5, px: 1, fontSize: "0.78rem", overflowWrap: "anywhere" }}>{mapeo.tipoOrigen}: {mapeo.origen}</TableCell>
                    <TableCell sx={{ py: 0.5, px: 1, fontSize: "0.78rem", overflowWrap: "anywhere" }}>{destinosBase.find((d) => mapeo.tipoMapeo === "ELEMENTO" ? d.idElementoXsd === mapeo.idElementoXsd : d.idAtributoXsd === mapeo.idAtributoXsd)?.nombre ?? "—"}</TableCell>
                    <TableCell align="right" sx={{ py: 0.25, px: 1 }}><Button size="small" color="error" onClick={() => quitarMapeoPendiente(indice)} disabled={guardando}>Quitar</Button></TableCell>
                  </TableRow>)}</TableBody>
                </Table></TableContainer>
                <Box sx={{ p: 1, display: "flex", justifyContent: "flex-end" }}><Button size="small" variant="contained" onClick={() => void guardarMapeos()} disabled={guardando || !idEmpresa || !idVersionSeleccionada}>{guardando ? resolveMessage(MessageCodes.COMMON_SAVING) : resolveMessage(MessageCodes.MAPEO_XSD_SAVE)}</Button></Box>
              </Paper>
            </Box>
          </Paper>

          <Paper variant="outlined" className="catalogos-panel" sx={{ overflow: "hidden" }}>
            <Box sx={{ px: 1.5, py: 0.5, bgcolor: "primary.main", color: "primary.contrastText" }}><Typography variant="subtitle1" component="h2">{resolveMessage(MessageCodes.MAPEO_XSD_EXISTING_TITLE)}</Typography></Box>
            <Box sx={{ p: 1.25 }}>
            {mapeosVisibles.length === 0 ? (
              <Typography color="text.secondary">
                {resolveMessage(MessageCodes.MAPEO_XSD_EMPTY)}
              </Typography>
            ) : (
              <Box sx={{ display: "grid", gap: 0.5 }}>
                {mapeosVisibles.map((mapeo) => (
                  <Box
                    key={mapeo.id}
                    sx={{
                      display: "grid",
                      gridTemplateColumns: { xs: "1fr", md: "1fr 2fr" },
                      gap: 0.75,
                      borderBottom: 1,
                      borderColor: "divider",
                      pb: 0.5,
                    }}
                  >
                    <Typography sx={{ fontSize: "0.8rem", overflowWrap: "anywhere" }}>{mapeo.tipoOrigen}: {mapeo.origen}</Typography>
                    <Typography color="text.secondary" sx={{ fontSize: "0.8rem", overflowWrap: "anywhere" }}>{descripcionDestino(mapeo)}</Typography>
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
