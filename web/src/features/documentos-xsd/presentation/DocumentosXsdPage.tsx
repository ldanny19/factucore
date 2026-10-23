import { useCallback, useEffect, useState, type FormEvent } from "react";
import { useNavigate } from "react-router-dom";
import {
  Alert,
  Box,
  Button,
  Chip,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
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
import AddOutlinedIcon from "@mui/icons-material/AddOutlined";
import CloudUploadOutlinedIcon from "@mui/icons-material/CloudUploadOutlined";
import RefreshOutlinedIcon from "@mui/icons-material/RefreshOutlined";
import { getRuntimeConfig } from "../../../config/runtime-config";
import { apiRequest } from "../../../shared/api/api-client";
import type { AdministracionRequest } from "../../../shared/api/administracion-contracts";
import { MessageCodes } from "../../../shared/messages/MessageCodes";
import { resolveMessage } from "../../../shared/messages/MessageResolver";
import type { Empresa } from "../../empresas/domain/Empresa";
import type { CatalogoItem } from "../../catalogos/domain/Catalogo";
import { useCatalogoUseCase } from "../../catalogos/presentation/hooks/CatalogoUseCaseContext";
import "../../../styles/app.css";

interface VersionDocumentoXsdDatos {
  version: string;
  rutaXsd: string;
  nombreArchivo: string;
  fechaInicio: string;
  fechaFin: null;
}

interface DocumentoXsdDatos {
  codigo: string;
  nombre: string;
  descripcion: string | null;
  tipoDocumento: string;
  prefijoArchivo: string;
  contenidoXsd: string | null;
  version: VersionDocumentoXsdDatos;
}

interface DocumentoXsdResponse {
  id: number;
  idVersionDocumentoXsd: number;
  codigo: string;
  nombre: string;
  descripcion: string | null;
  tipoDocumento: string;
  prefijoArchivo: string;
  estadoRegistro: string;
}

interface FormularioDocumentoXsd {
  idEmpresa: string;
  codigo: string;
  nombre: string;
  descripcion: string;
  tipoDocumento: string;
  prefijoArchivo: string;
  version: string;
  fechaInicio: string;
}

function fechaHoraLocal(fecha: Date): string {
  const pad = (value: number) => String(value).padStart(2, "0");
  return `${fecha.getFullYear()}-${pad(fecha.getMonth() + 1)}-${pad(fecha.getDate())}T${pad(fecha.getHours())}:${pad(fecha.getMinutes())}`;
}

function fechaHoraSolicitud(fecha: string): string {
  return `${fecha.replace("T", " ")}:00.000`;
}

function fechaHoraVersion(fecha: string): string {
  return `${fecha}:00`;
}

function archivoABase64(archivo: File): Promise<string> {
  return new Promise((resolve, reject) => {
    const lector = new FileReader();
    lector.onload = () => {
      const resultado = lector.result;
      if (typeof resultado !== "string") {
        reject(new Error(resolveMessage(MessageCodes.DOCUMENTO_XSD_FILE_READ_ERROR)));
        return;
      }
      const separador = resultado.indexOf(",");
      if (separador < 0) {
        reject(new Error(resolveMessage(MessageCodes.DOCUMENTO_XSD_FILE_READ_ERROR)));
        return;
      }
      resolve(resultado.slice(separador + 1));
    };
    lector.onerror = () =>
      reject(new Error(resolveMessage(MessageCodes.DOCUMENTO_XSD_FILE_READ_ERROR)));
    lector.readAsDataURL(archivo);
  });
}

function mensajeError(error: unknown): string {
  if (error instanceof Error && error.message.trim()) return error.message;
  return resolveMessage(MessageCodes.COMMON_OPERATION_FAILED);
}

const FORMULARIO_INICIAL: FormularioDocumentoXsd = {
  idEmpresa: "",
  codigo: "",
  nombre: "",
  descripcion: "",
  tipoDocumento: "",
  prefijoArchivo: "",
  version: "",
  fechaInicio: fechaHoraLocal(new Date()),
};

export default function DocumentosXsdPage() {
  const navigate = useNavigate();
  const catalogoUseCases = useCatalogoUseCase();
  const [empresas, setEmpresas] = useState<Empresa[]>([]);
  const [tiposDocumento, setTiposDocumento] = useState<CatalogoItem[]>([]);
  const [formulario, setFormulario] = useState<FormularioDocumentoXsd>(FORMULARIO_INICIAL);
  const [archivo, setArchivo] = useState<File | null>(null);
  const [documentos, setDocumentos] = useState<DocumentoXsdResponse[]>([]);
  const [cargandoDocumentos, setCargandoDocumentos] = useState(true);
  const [dialogoAbierto, setDialogoAbierto] = useState(false);
  const [cargandoTipos, setCargandoTipos] = useState(true);
  const [cargandoEmpresas, setCargandoEmpresas] = useState(true);
  const [guardando, setGuardando] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [aviso, setAviso] = useState<string | null>(null);
  const [idVersionCargada, setIdVersionCargada] = useState<number | null>(null);

  const cargarDocumentos = useCallback(async () => {
    setCargandoDocumentos(true);
    try {
      const config = getRuntimeConfig();
      const path = config.API_DOCUMENTOS_XSD_PATH.replace(/^\/+|\/+$/g, "");
      const resultado = await apiRequest<DocumentoXsdResponse[]>({
        method: "GET",
        url: path,
      });
      setDocumentos(resultado);
    } catch (exception) {
      setDocumentos([]);
      setError(mensajeError(exception));
    } finally {
      setCargandoDocumentos(false);
    }
  }, []);

  const cargarTiposDocumento = useCallback(async () => {
    setCargandoTipos(true);
    setCargandoEmpresas(true);
    setError(null);
    const config = getRuntimeConfig();

    const cargarEmpresasActivas = async () => {
      try {
        const respuesta = await apiRequest<Empresa[]>({
          method: "GET",
          url: config.API_EMPRESAS_PATH,
        });
        const empresasActivas = respuesta.filter((empresa) => {
          const estado = String(empresa.estadoRegistro ?? "")
            .trim()
            .toUpperCase();
          return estado === "A" || estado === "ACTIVO";
        });
        setEmpresas(empresasActivas);
        setFormulario((actual) => ({
          ...actual,
          idEmpresa: empresasActivas.some((empresa) => String(empresa.id) === actual.idEmpresa)
            ? actual.idEmpresa
            : "",
        }));
      } catch (exception) {
        setEmpresas([]);
        setFormulario((actual) => ({ ...actual, idEmpresa: "" }));
        setError(mensajeError(exception));
      } finally {
        setCargandoEmpresas(false);
      }
    };

    const cargarTipos = async () => {
      try {
        const catalogos = await catalogoUseCases.listar();
        const catalogo = catalogos.find(
          (item) => item.codigo === "TIPO_DOCUMENTO" && item.estadoRegistro === "ACTIVO",
        );
        if (!catalogo) {
          throw new Error(resolveMessage(MessageCodes.DOCUMENTO_XSD_TYPES_NOT_CONFIGURED));
        }
        const items = await catalogoUseCases.listarItems(catalogo.id);
        setTiposDocumento(items.filter((item) => item.estadoRegistro === "ACTIVO"));
      } catch (exception) {
        setTiposDocumento([]);
        setError(mensajeError(exception));
      } finally {
        setCargandoTipos(false);
      }
    };

    await Promise.all([cargarEmpresasActivas(), cargarTipos()]);
  }, [catalogoUseCases]);

  useEffect(() => {
    void cargarTiposDocumento();
  }, [cargarTiposDocumento]);

  useEffect(() => {
    void cargarDocumentos();
  }, [cargarDocumentos]);

  const enviar = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError(null);
    setAviso(null);
    setIdVersionCargada(null);
    if (!formulario.idEmpresa) {
      setError(resolveMessage(MessageCodes.DOCUMENTO_XSD_COMPANY_REQUIRED));
      return;
    }
    if (!formulario.tipoDocumento) {
      setError(resolveMessage(MessageCodes.DOCUMENTO_XSD_TYPE_REQUIRED));
      return;
    }
    if (!archivo) {
      setError(resolveMessage(MessageCodes.DOCUMENTO_XSD_FILE_REQUIRED));
      return;
    }
    if (!archivo.name.toLowerCase().endsWith(".xsd")) {
      setError(resolveMessage(MessageCodes.DOCUMENTO_XSD_FILE_INVALID));
      return;
    }

    setGuardando(true);
    try {
      const config = getRuntimeConfig();
      const path = config.API_DOCUMENTOS_XSD_PATH.replace(/^\/+|\/+$/g, "");
      const datos: DocumentoXsdDatos = {
        codigo: formulario.codigo.trim(),
        nombre: formulario.nombre.trim(),
        descripcion: formulario.descripcion.trim() || null,
        tipoDocumento: formulario.tipoDocumento,
        prefijoArchivo: formulario.prefijoArchivo.trim(),
        contenidoXsd: await archivoABase64(archivo),
        version: {
          version: formulario.version.trim(),
          rutaXsd: "",
          nombreArchivo: archivo.name,
          fechaInicio: fechaHoraVersion(formulario.fechaInicio),
          fechaFin: null,
        },
      };
      const request: AdministracionRequest<DocumentoXsdDatos> = {
        fechaInicio: fechaHoraSolicitud(formulario.fechaInicio),
        canal: "WEB",
        idEmpresa: Number(formulario.idEmpresa),
        datos,
      };
      const resultado = await apiRequest<DocumentoXsdResponse>({
        method: "POST",
        url: path,
        data: request,
      });
      setAviso(
        resolveMessage(MessageCodes.DOCUMENTO_XSD_UPLOADED, {
          codigo: resultado.codigo,
          versionId: resultado.idVersionDocumentoXsd,
        }),
      );
      setIdVersionCargada(resultado.idVersionDocumentoXsd);
      setFormulario({ ...FORMULARIO_INICIAL, fechaInicio: fechaHoraLocal(new Date()) });
      setArchivo(null);
      setDialogoAbierto(false);
      await cargarDocumentos();
    } catch (exception) {
      setError(mensajeError(exception));
    } finally {
      setGuardando(false);
    }
  };

  return (
    <Box className="catalogos-page">
      <Box className="catalogos-page-heading">
        <Box>
          <Typography className="page-title" component="h1" variant="h4">
            {resolveMessage(MessageCodes.DOCUMENTO_XSD_PAGE_TITLE)}
          </Typography>
          <Typography color="text.secondary">
            {resolveMessage(MessageCodes.DOCUMENTO_XSD_PAGE_DESCRIPTION)}
          </Typography>
        </Box>
        <Button
          variant="contained"
          startIcon={<AddOutlinedIcon />}
          onClick={() => {
            setError(null);
            setAviso(null);
            setIdVersionCargada(null);
            setDialogoAbierto(true);
          }}
        >
          {resolveMessage(MessageCodes.DOCUMENTO_XSD_NEW)}
        </Button>
      </Box>

      {error && (
        <Alert severity="error" onClose={() => setError(null)}>
          {error}
        </Alert>
      )}
      {aviso && (
        <Alert
          severity="success"
          onClose={() => {
            setAviso(null);
            setIdVersionCargada(null);
          }}
          action={
            idVersionCargada ? (
              <Button
                color="inherit"
                size="small"
                onClick={() => navigate(`/app/mapeos-xsd?versionId=${idVersionCargada}`)}
              >
                {resolveMessage(MessageCodes.DOCUMENTO_XSD_CONFIGURE_MAPPING)}
              </Button>
            ) : undefined
          }
        >
          {aviso}
        </Alert>
      )}

      <Box className="empresas-list-heading">
        <Typography variant="h6" component="h2">
          {resolveMessage(MessageCodes.DOCUMENTO_XSD_LIST_TITLE)}
        </Typography>
        <Button
          variant="text"
          startIcon={<RefreshOutlinedIcon />}
          onClick={() => void cargarDocumentos()}
          disabled={cargandoDocumentos}
        >
          {resolveMessage(MessageCodes.COMMON_REFRESH)}
        </Button>
      </Box>

      <TableContainer component={Paper} variant="outlined" className="empresas-table-panel">
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>{resolveMessage(MessageCodes.DOCUMENTO_XSD_CODE)}</TableCell>
              <TableCell>{resolveMessage(MessageCodes.DOCUMENTO_XSD_NAME)}</TableCell>
              <TableCell>{resolveMessage(MessageCodes.DOCUMENTO_XSD_TYPE)}</TableCell>
              <TableCell>{resolveMessage(MessageCodes.COMMON_STATUS)}</TableCell>
              <TableCell align="right">{resolveMessage(MessageCodes.COMMON_ACTIONS)}</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {cargandoDocumentos ? (
              <TableRow>
                <TableCell colSpan={5} align="center">
                  <CircularProgress size={26} />
                </TableCell>
              </TableRow>
            ) : documentos.length === 0 ? (
              <TableRow>
                <TableCell colSpan={5} align="center">
                  {resolveMessage(MessageCodes.DOCUMENTO_XSD_LIST_EMPTY)}
                </TableCell>
              </TableRow>
            ) : (
              documentos.map((documento) => (
                <TableRow hover key={documento.id}>
                  <TableCell>{documento.codigo}</TableCell>
                  <TableCell>{documento.nombre}</TableCell>
                  <TableCell>{documento.tipoDocumento}</TableCell>
                  <TableCell>
                    <Chip
                      size="small"
                      label={documento.estadoRegistro}
                      color={documento.estadoRegistro === "A" ? "success" : "default"}
                    />
                  </TableCell>
                  <TableCell align="right">
                    <Button
                      size="small"
                      onClick={() =>
                        navigate(`/app/mapeos-xsd?versionId=${documento.idVersionDocumentoXsd}`)
                      }
                    >
                      {resolveMessage(MessageCodes.DOCUMENTO_XSD_CONFIGURE_MAPPING)}
                    </Button>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </TableContainer>

      <Dialog
        open={dialogoAbierto}
        onClose={() => !guardando && setDialogoAbierto(false)}
        fullWidth
        maxWidth="md"
      >
        <Box component="form" onSubmit={enviar}>
          <DialogTitle>{resolveMessage(MessageCodes.DOCUMENTO_XSD_UPLOAD_TITLE)}</DialogTitle>
          <DialogContent sx={{ display: "grid", gap: 2, pt: 1 }}>
            <TextField
              select
              label={resolveMessage(MessageCodes.DOCUMENTO_XSD_COMPANY)}
              value={formulario.idEmpresa}
              onChange={(event) =>
                setFormulario((actual) => ({ ...actual, idEmpresa: event.target.value }))
              }
              required
              disabled={cargandoEmpresas || guardando || empresas.length === 0}
            >
              <MenuItem value="">{resolveMessage(MessageCodes.COMMON_SELECT_OPTION)}</MenuItem>
              {empresas.map((empresa) => (
                <MenuItem key={empresa.id} value={empresa.id}>
                  {empresa.ruc} — {empresa.razonSocial}
                </MenuItem>
              ))}
            </TextField>
            <TextField
              label={resolveMessage(MessageCodes.DOCUMENTO_XSD_CODE)}
              required
              inputProps={{ maxLength: 50 }}
              value={formulario.codigo}
              onChange={(event) =>
                setFormulario((actual) => ({ ...actual, codigo: event.target.value }))
              }
            />
            <TextField
              label={resolveMessage(MessageCodes.DOCUMENTO_XSD_NAME)}
              required
              inputProps={{ maxLength: 300 }}
              value={formulario.nombre}
              onChange={(event) =>
                setFormulario((actual) => ({ ...actual, nombre: event.target.value }))
              }
            />
            <TextField
              label={resolveMessage(MessageCodes.DOCUMENTO_XSD_DESCRIPTION)}
              multiline
              minRows={2}
              inputProps={{ maxLength: 500 }}
              value={formulario.descripcion}
              onChange={(event) =>
                setFormulario((actual) => ({ ...actual, descripcion: event.target.value }))
              }
            />
            <TextField
              select
              label={resolveMessage(MessageCodes.DOCUMENTO_XSD_TYPE)}
              value={formulario.tipoDocumento}
              onChange={(event) =>
                setFormulario((actual) => ({ ...actual, tipoDocumento: event.target.value }))
              }
              required
              disabled={cargandoTipos || guardando}
            >
              <MenuItem value="">{resolveMessage(MessageCodes.COMMON_SELECT_OPTION)}</MenuItem>
              {tiposDocumento.map((tipo) => (
                <MenuItem key={tipo.id} value={tipo.codigo}>
                  {tipo.codigo} — {tipo.nombre}
                </MenuItem>
              ))}
            </TextField>
            <TextField
              label={resolveMessage(MessageCodes.DOCUMENTO_XSD_PREFIX)}
              required
              inputProps={{ maxLength: 20 }}
              value={formulario.prefijoArchivo}
              onChange={(event) =>
                setFormulario((actual) => ({ ...actual, prefijoArchivo: event.target.value }))
              }
            />
            <TextField
              label={resolveMessage(MessageCodes.DOCUMENTO_XSD_VERSION)}
              required
              inputProps={{ maxLength: 20 }}
              value={formulario.version}
              onChange={(event) =>
                setFormulario((actual) => ({ ...actual, version: event.target.value }))
              }
            />
            <TextField
              type="datetime-local"
              label={resolveMessage(MessageCodes.DOCUMENTO_XSD_VALID_FROM)}
              required
              InputLabelProps={{ shrink: true }}
              value={formulario.fechaInicio}
              onChange={(event) =>
                setFormulario((actual) => ({ ...actual, fechaInicio: event.target.value }))
              }
            />
            <Button
              component="label"
              variant="outlined"
              startIcon={
                cargandoTipos || cargandoEmpresas ? (
                  <CircularProgress size={18} />
                ) : (
                  <CloudUploadOutlinedIcon />
                )
              }
              disabled={cargandoTipos || cargandoEmpresas || guardando}
            >
              {archivo ? archivo.name : resolveMessage(MessageCodes.DOCUMENTO_XSD_SELECT_FILE)}
              <input
                hidden
                type="file"
                accept=".xsd,application/xml,text/xml"
                onChange={(event) => setArchivo(event.target.files?.[0] ?? null)}
              />
            </Button>
            <Typography variant="body2" color="text.secondary">
              {resolveMessage(MessageCodes.DOCUMENTO_XSD_STORAGE_INFO)}
            </Typography>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setDialogoAbierto(false)} disabled={guardando}>
              {resolveMessage(MessageCodes.COMMON_CANCEL)}
            </Button>
            <Button
              type="submit"
              variant="contained"
              disabled={guardando || cargandoTipos || cargandoEmpresas || empresas.length === 0}
            >
              {guardando
                ? resolveMessage(MessageCodes.COMMON_SAVING)
                : resolveMessage(MessageCodes.DOCUMENTO_XSD_UPLOAD)}
            </Button>
          </DialogActions>
        </Box>
      </Dialog>
    </Box>
  );
}
