import { useCallback, useEffect, useMemo, useState, type FormEvent } from "react";
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
import BlockOutlinedIcon from "@mui/icons-material/BlockOutlined";
import DeleteOutlineOutlinedIcon from "@mui/icons-material/DeleteOutlineOutlined";
import EditOutlinedIcon from "@mui/icons-material/EditOutlined";
import ReplayOutlinedIcon from "@mui/icons-material/ReplayOutlined";
import RefreshOutlinedIcon from "@mui/icons-material/RefreshOutlined";
import { getRuntimeConfig } from "../../../config/runtime-config";
import { useEmpresaUseCases } from "../../empresas/presentation/hooks/EmpresaUseCasesContext";
import type { Empresa } from "../../empresas/domain/Empresa";
import { MessageCodes } from "../../../shared/messages/MessageCodes";
import { resolveMessage } from "../../../shared/messages/MessageResolver";
import type {
  CertificadoFirma,
  CertificadoFirmaDatos,
} from "../domain/CertificadoFirma";
import { useCertificadoFirmaUseCase } from "./hooks/CertificadoFirmaUseCaseContext";
import "../../../styles/app.css";

interface CertificadoFormulario {
  nombreArchivo: string;
  fechaInicio: string;
  fechaFin: string;
}

function fechaLocalInput(value: Date): string {
  const pad = (part: number) => String(part).padStart(2, "0");
  return `${value.getFullYear()}-${pad(value.getMonth() + 1)}-${pad(value.getDate())}T${pad(value.getHours())}:${pad(value.getMinutes())}`;
}

function fechaParaInput(value: string | null): string {
  return value ? value.slice(0, 16) : "";
}

function mensajeError(error: unknown): string {
  if (error instanceof Error && error.message.trim()) {
    return error.message;
  }
  return resolveMessage(MessageCodes.COMMON_OPERATION_FAILED);
}

export default function CertificadosFirmaPage() {
  const useCases = useCertificadoFirmaUseCase();
  const empresaUseCases = useEmpresaUseCases();
  const [empresas, setEmpresas] = useState<Empresa[]>([]);
  const [empresaId, setEmpresaId] = useState<number | "">("");
  const [certificados, setCertificados] = useState<CertificadoFirma[]>([]);
  const [cargandoEmpresas, setCargandoEmpresas] = useState(true);
  const [cargando, setCargando] = useState(false);
  const [guardando, setGuardando] = useState(false);
  const [operacionFilaId, setOperacionFilaId] = useState<number | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [aviso, setAviso] = useState<string | null>(null);
  const [dialogoAbierto, setDialogoAbierto] = useState(false);
  const [edicion, setEdicion] = useState<CertificadoFirma | null>(null);
  const [archivoSeleccionado, setArchivoSeleccionado] = useState<File | null>(null);
  const [formulario, setFormulario] = useState<CertificadoFormulario>({
    nombreArchivo: "",
    fechaInicio: fechaLocalInput(new Date()),
    fechaFin: "",
  });
  const [confirmacionEliminacion, setConfirmacionEliminacion] =
    useState<CertificadoFirma | null>(null);

  const empresasPorId = useMemo(
    () => new Map(empresas.map((empresa) => [empresa.id, empresa])),
    [empresas],
  );
  const empresaSeleccionada = empresaId === "" ? undefined : empresasPorId.get(empresaId);

  const cargar = useCallback(async () => {
    if (empresaId === "") {
      setCertificados([]);
      setCargando(false);
      return;
    }
    setCargando(true);
    try {
      setCertificados(await useCases.listar(empresaId));
    } catch (exception) {
      setError(mensajeError(exception));
      setCertificados([]);
    } finally {
      setCargando(false);
    }
  }, [empresaId, useCases]);

  useEffect(() => {
    let vigente = true;
    const iniciar = async () => {
      setCargandoEmpresas(true);
      try {
        const resultado = await empresaUseCases.listar();
        if (!vigente) return;
        setEmpresas(resultado);
        setEmpresaId((actual) => actual || resultado[0]?.id || "");
      } catch (exception) {
        if (vigente) setError(mensajeError(exception));
      } finally {
        if (vigente) setCargandoEmpresas(false);
      }
    };
    void iniciar();
    return () => {
      vigente = false;
    };
  }, [empresaUseCases]);

  useEffect(() => {
    void cargar();
  }, [cargar]);

  const abrirCrear = () => {
    setEdicion(null);
    setArchivoSeleccionado(null);
    setFormulario({
      nombreArchivo: "",
      fechaInicio: fechaLocalInput(new Date()),
      fechaFin: "",
    });
    setError(null);
    setAviso(null);
    setDialogoAbierto(true);
  };

  const abrirEdicion = (certificado: CertificadoFirma) => {
    setEdicion(certificado);
    setArchivoSeleccionado(null);
    setFormulario({
      nombreArchivo: certificado.nombreArchivo ?? "",
      fechaInicio: fechaParaInput(certificado.fechaInicio),
      fechaFin: fechaParaInput(certificado.fechaFin),
    });
    setError(null);
    setAviso(null);
    setDialogoAbierto(true);
  };

  const seleccionarArchivo = (archivo: File | undefined) => {
    setError(null);
    if (!archivo) return;
    if (!/\.(p12|pfx)$/i.test(archivo.name)) {
      setArchivoSeleccionado(null);
      setFormulario((actual) => ({
        ...actual,
        nombreArchivo: edicion?.nombreArchivo ?? "",
      }));
      setError(resolveMessage(MessageCodes.CERTIFICADO_FILE_INVALID));
      return;
    }
    const nombreArchivoInvalido =
      archivo.name.includes("/") ||
      archivo.name.includes("\\") ||
      Array.from(archivo.name).some((caracter) => {
        const codigo = caracter.charCodeAt(0);
        return codigo < 32 || codigo === 127;
      });
    if (nombreArchivoInvalido) {
      setArchivoSeleccionado(null);
      setFormulario((actual) => ({
        ...actual,
        nombreArchivo: edicion?.nombreArchivo ?? "",
      }));
      setError(resolveMessage(MessageCodes.CERTIFICADO_FILE_INVALID));
      return;
    }
    setArchivoSeleccionado(archivo);
    setFormulario((actual) => ({ ...actual, nombreArchivo: archivo.name }));
  };

  const guardar = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError(null);
    setAviso(null);
    if (!empresaSeleccionada) {
      setError(resolveMessage(MessageCodes.CERTIFICADO_EMPRESA_REQUIRED));
      return;
    }
    if (!edicion && !archivoSeleccionado) {
      setError(resolveMessage(MessageCodes.CERTIFICADO_FILE_REQUIRED));
      return;
    }
    const nombreArchivo = archivoSeleccionado?.name ?? formulario.nombreArchivo.trim();
    if (nombreArchivo.length > 300) {
      setError(resolveMessage(MessageCodes.CERTIFICADO_FIELDS_INVALID));
      return;
    }
    if (
      !formulario.fechaInicio ||
      (formulario.fechaFin && formulario.fechaFin < formulario.fechaInicio)
    ) {
      setError(resolveMessage(MessageCodes.CERTIFICADO_DATE_RANGE_INVALID));
      return;
    }

    const datosBase = {
      idEmpresa: empresaSeleccionada.id,
      fechaInicio: `${formulario.fechaInicio}:00`,
      fechaFin: formulario.fechaFin ? `${formulario.fechaFin}:00` : null,
    };

    setGuardando(true);
    try {
      if (archivoSeleccionado) {
        const storagePath = getRuntimeConfig().CERTIFICADO_STORAGE_PATH.replace(/\/+$/, "");
        const datos: CertificadoFirmaDatos = {
          ...datosBase,
          nombreArchivo,
          rutaCertificado: `${storagePath}/${nombreArchivo}`,
        };
        await useCases.crear(datos);
      } else if (edicion) {
        const datos: CertificadoFirmaDatos = {
          ...datosBase,
          nombreArchivo,
          rutaCertificado: edicion.rutaCertificado,
        };
        await useCases.actualizar(edicion.id, datos);
      } else {
        setError(resolveMessage(MessageCodes.CERTIFICADO_FILE_REQUIRED));
        return;
      }

      if (edicion) {
        setAviso(resolveMessage(MessageCodes.CERTIFICADO_UPDATED));
      } else {
        setAviso(resolveMessage(MessageCodes.CERTIFICADO_CREATED));
      }
      setDialogoAbierto(false);
      await cargar();
    } catch (exception) {
      setError(mensajeError(exception));
    } finally {
      setGuardando(false);
    }
  };

  const cambiarEstado = async (certificado: CertificadoFirma) => {
    setOperacionFilaId(certificado.id);
    setError(null);
    setAviso(null);
    try {
      if (certificado.estadoRegistro === "ACTIVO") {
        await useCases.inactivar(certificado.id);
        setAviso(resolveMessage(MessageCodes.CERTIFICADO_INACTIVATED));
      } else {
        await useCases.reactivar(certificado.id);
        setAviso(resolveMessage(MessageCodes.CERTIFICADO_REACTIVATED));
      }
      await cargar();
    } catch (exception) {
      setError(mensajeError(exception));
    } finally {
      setOperacionFilaId(null);
    }
  };

  const eliminar = async () => {
    if (!confirmacionEliminacion) return;
    setGuardando(true);
    setError(null);
    setAviso(null);
    try {
      await useCases.eliminar(confirmacionEliminacion.id);
      setAviso(
        resolveMessage(MessageCodes.CERTIFICADO_DELETED, {
          name: confirmacionEliminacion.nombreArchivo || confirmacionEliminacion.id,
        }),
      );
      setConfirmacionEliminacion(null);
      await cargar();
    } catch (exception) {
      setError(mensajeError(exception));
    } finally {
      setGuardando(false);
    }
  };

  const cambiarCampo = (campo: keyof CertificadoFormulario, valor: string) => {
    setFormulario((actual) => ({ ...actual, [campo]: valor }));
  };

  return (
    <Box className="certificados-firma-page">
      <Box className="certificados-firma-heading">
        <Box>
          <Typography className="page-title" component="h1" variant="h4">
            {resolveMessage(MessageCodes.CERTIFICADO_PAGE_TITLE)}
          </Typography>
          <Typography color="text.secondary">
            {resolveMessage(MessageCodes.CERTIFICADO_PAGE_DESCRIPTION)}
          </Typography>
        </Box>
        <Button
          variant="contained"
          startIcon={<AddOutlinedIcon />}
          onClick={abrirCrear}
          disabled={cargandoEmpresas || !empresaSeleccionada}
        >
          {resolveMessage(MessageCodes.CERTIFICADO_NEW)}
        </Button>
      </Box>

      {error && !dialogoAbierto && (
        <Alert severity="error" onClose={() => setError(null)}>{error}</Alert>
      )}
      {aviso && <Alert severity="success" onClose={() => setAviso(null)}>{aviso}</Alert>}

      <Paper variant="outlined" className="certificados-firma-filter">
        <TextField
          select
          size="small"
          label={resolveMessage(MessageCodes.ESTABLECIMIENTO_COMPANY)}
          value={empresaId}
          onChange={(event) => {
            setError(null);
            setAviso(null);
            setEmpresaId(Number(event.target.value));
          }}
          disabled={cargandoEmpresas || empresas.length === 0}
          sx={{ minWidth: 320 }}
        >
          {empresas.map((empresa) => (
            <MenuItem key={empresa.id} value={empresa.id}>
              {empresa.ruc} - {empresa.razonSocial}
            </MenuItem>
          ))}
        </TextField>
      </Paper>

      <Box className="certificados-firma-list-heading">
        <Box>
          <Typography variant="h6" component="h2">
            {resolveMessage(MessageCodes.CERTIFICADO_LIST_TITLE)}
          </Typography>
          <Typography variant="body2" color="text.secondary">
            {certificados.length} {resolveMessage(MessageCodes.CERTIFICADO_RECORDS)}
          </Typography>
        </Box>
        <Button
          variant="text"
          startIcon={<RefreshOutlinedIcon />}
          onClick={() => void cargar()}
          disabled={cargando || empresaId === ""}
        >
          {resolveMessage(MessageCodes.COMMON_REFRESH)}
        </Button>
      </Box>

      <TableContainer component={Paper} variant="outlined" className="certificados-firma-table">
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>{resolveMessage(MessageCodes.CERTIFICADO_FILE_NAME)}</TableCell>
              <TableCell>{resolveMessage(MessageCodes.CERTIFICADO_VALID_FROM)}</TableCell>
              <TableCell>{resolveMessage(MessageCodes.CERTIFICADO_VALID_UNTIL)}</TableCell>
              <TableCell>{resolveMessage(MessageCodes.COMMON_STATUS)}</TableCell>
              <TableCell align="right">{resolveMessage(MessageCodes.COMMON_ACTIONS)}</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {cargando ? (
              <TableRow><TableCell colSpan={5} align="center"><CircularProgress size={26} /></TableCell></TableRow>
            ) : certificados.length === 0 ? (
              <TableRow><TableCell colSpan={5} align="center">{resolveMessage(MessageCodes.CERTIFICADO_EMPTY)}</TableCell></TableRow>
            ) : (
              certificados.map((certificado) => (
                <TableRow hover key={certificado.id}>
                  <TableCell>
                    <Typography variant="body2" fontWeight={700}>
                      {certificado.nombreArchivo || resolveMessage(MessageCodes.CERTIFICADO_FILE_NAME_EMPTY)}
                    </Typography>
                  </TableCell>
                  <TableCell>{certificado.fechaInicio?.replace("T", " ") ?? "—"}</TableCell>
                  <TableCell>{certificado.fechaFin?.replace("T", " ") ?? "—"}</TableCell>
                  <TableCell>
                    <Chip
                      size="small"
                      label={certificado.estadoRegistro}
                      color={certificado.estadoRegistro === "ACTIVO" ? "success" : "default"}
                    />
                  </TableCell>
                  <TableCell align="right">
                    <Box className="certificados-firma-row-actions">
                      <Button size="small" onClick={() => abrirEdicion(certificado)} startIcon={<EditOutlinedIcon />}>
                        {resolveMessage(MessageCodes.COMMON_EDIT)}
                      </Button>
                      <Button
                        size="small"
                        onClick={() => void cambiarEstado(certificado)}
                        disabled={operacionFilaId !== null || guardando}
                        startIcon={certificado.estadoRegistro === "ACTIVO" ? <BlockOutlinedIcon /> : <ReplayOutlinedIcon />}
                      >
                        {resolveMessage(certificado.estadoRegistro === "ACTIVO" ? MessageCodes.CERTIFICADO_INACTIVATE : MessageCodes.CERTIFICADO_REACTIVATE)}
                      </Button>
                      <Button
                        size="small"
                        color="error"
                        onClick={() => setConfirmacionEliminacion(certificado)}
                        disabled={operacionFilaId !== null || guardando}
                        startIcon={<DeleteOutlineOutlinedIcon />}
                      >
                        {resolveMessage(MessageCodes.COMMON_DELETE)}
                      </Button>
                    </Box>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </TableContainer>

      <Dialog open={dialogoAbierto} onClose={() => !guardando && setDialogoAbierto(false)} fullWidth maxWidth="md">
        <Box component="form" onSubmit={guardar}>
          <DialogTitle>
            {resolveMessage(edicion ? MessageCodes.CERTIFICADO_EDIT_TITLE : MessageCodes.CERTIFICADO_CREATE_TITLE)}
          </DialogTitle>
          <DialogContent>
            <Alert severity="warning" sx={{ mt: 1 }}>
              {resolveMessage(MessageCodes.CERTIFICADO_SECURITY_WARNING)}
            </Alert>
            {error && (
              <Alert severity="error" onClose={() => setError(null)} sx={{ mt: 1 }}>
                {error}
              </Alert>
            )}
            <Box className="certificados-firma-form-grid">
              <TextField
                label={resolveMessage(MessageCodes.ESTABLECIMIENTO_COMPANY)}
                value={empresaSeleccionada ? `${empresaSeleccionada.ruc} - ${empresaSeleccionada.razonSocial}` : ""}
                disabled
                fullWidth
              />
              <Box sx={{ gridColumn: "1 / -1" }}>
                <Typography variant="body2" sx={{ mb: 1 }}>
                  {resolveMessage(MessageCodes.CERTIFICADO_FILE_NAME)}:{" "}
                  {formulario.nombreArchivo || resolveMessage(MessageCodes.CERTIFICADO_NO_FILE_SELECTED)}
                </Typography>
                {!edicion && (
                  <>
                    <Button component="label" variant="outlined" disabled={guardando}>
                      {resolveMessage(MessageCodes.CERTIFICADO_SELECT_FILE)}
                      <input
                        hidden
                        type="file"
                        accept=".p12,.pfx,application/x-pkcs12"
                        onChange={(event) => {
                          seleccionarArchivo(event.currentTarget.files?.[0]);
                          event.currentTarget.value = "";
                        }}
                      />
                    </Button>
                  </>
                )}
              </Box>
              <TextField
                label={resolveMessage(MessageCodes.CERTIFICADO_VALID_FROM)}
                type="datetime-local"
                value={formulario.fechaInicio}
                onChange={(event) => cambiarCampo("fechaInicio", event.target.value)}
                required
                InputLabelProps={{ shrink: true }}
              />
              <TextField
                label={resolveMessage(MessageCodes.CERTIFICADO_VALID_UNTIL)}
                type="datetime-local"
                value={formulario.fechaFin}
                onChange={(event) => cambiarCampo("fechaFin", event.target.value)}
                InputLabelProps={{ shrink: true }}
              />
            </Box>
          </DialogContent>
          <DialogActions className="certificados-firma-dialog-actions">
            <Button onClick={() => setDialogoAbierto(false)} disabled={guardando}>
              {resolveMessage(MessageCodes.COMMON_CANCEL)}
            </Button>
            <Button type="submit" variant="contained" disabled={guardando}>
              {guardando ? resolveMessage(MessageCodes.CERTIFICADO_SAVING) : resolveMessage(MessageCodes.COMMON_SAVE)}
            </Button>
          </DialogActions>
        </Box>
      </Dialog>

      <Dialog open={Boolean(confirmacionEliminacion)} onClose={() => !guardando && setConfirmacionEliminacion(null)} fullWidth maxWidth="xs">
        <DialogTitle>{resolveMessage(MessageCodes.CERTIFICADO_DELETE_TITLE)}</DialogTitle>
        <DialogContent>
          <Typography>
            {resolveMessage(MessageCodes.CERTIFICADO_DELETE_CONFIRM_PREFIX)}{" "}
            <strong>{confirmacionEliminacion?.nombreArchivo || confirmacionEliminacion?.id}</strong>?
          </Typography>
          <Typography color="text.secondary" sx={{ mt: 1 }}>
            {resolveMessage(MessageCodes.CERTIFICADO_DELETE_CONFIRM_DESCRIPTION)}
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setConfirmacionEliminacion(null)} disabled={guardando}>
            {resolveMessage(MessageCodes.COMMON_CANCEL)}
          </Button>
          <Button color="error" variant="contained" onClick={() => void eliminar()} disabled={guardando}>
            {guardando ? resolveMessage(MessageCodes.COMMON_DELETING) : resolveMessage(MessageCodes.CERTIFICADO_DELETE_CONFIRM)}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
