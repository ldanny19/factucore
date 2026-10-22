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
import { useEmpresaUseCases } from "../../empresas/presentation/hooks/EmpresaUseCasesContext";
import type { Empresa } from "../../empresas/domain/Empresa";
import { MessageCodes } from "../../../shared/messages/MessageCodes";
import { resolveMessage } from "../../../shared/messages/MessageResolver";
import type {
  ConfiguracionEmpresa,
  ConfiguracionEmpresaDatos,
} from "../domain/ConfiguracionEmpresa";
import { useConfiguracionEmpresaUseCase } from "./hooks/ConfiguracionEmpresaUseCaseContext";
import "../../../styles/app.css";

interface ConfiguracionFormulario {
  clave: string;
  valor: string;
  tipoDato: string;
  fechaVigenciaDesde: string;
  fechaVigenciaHasta: string;
}

const FORMULARIO_VACIO: ConfiguracionFormulario = {
  clave: "",
  valor: "",
  tipoDato: "STRING",
  fechaVigenciaDesde: fechaLocalInput(new Date()),
  fechaVigenciaHasta: "",
};

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

export default function ConfiguracionEmpresaPage() {
  const useCases = useConfiguracionEmpresaUseCase();
  const empresaUseCases = useEmpresaUseCases();
  const [empresas, setEmpresas] = useState<Empresa[]>([]);
  const [empresaId, setEmpresaId] = useState<number | "">("");
  const [configuraciones, setConfiguraciones] = useState<ConfiguracionEmpresa[]>([]);
  const [cargandoEmpresas, setCargandoEmpresas] = useState(true);
  const [cargando, setCargando] = useState(false);
  const [guardando, setGuardando] = useState(false);
  const [operacionFilaId, setOperacionFilaId] = useState<number | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [aviso, setAviso] = useState<string | null>(null);
  const [dialogoAbierto, setDialogoAbierto] = useState(false);
  const [edicion, setEdicion] = useState<ConfiguracionEmpresa | null>(null);
  const [formulario, setFormulario] = useState<ConfiguracionFormulario>(FORMULARIO_VACIO);
  const [confirmacionEliminacion, setConfirmacionEliminacion] =
    useState<ConfiguracionEmpresa | null>(null);

  const empresasPorId = useMemo(
    () => new Map(empresas.map((empresa) => [empresa.id, empresa])),
    [empresas],
  );
  const empresaSeleccionada = empresaId === "" ? undefined : empresasPorId.get(empresaId);

  const cargar = useCallback(async () => {
    if (empresaId === "") {
      setConfiguraciones([]);
      setCargando(false);
      return;
    }
    setCargando(true);
    try {
      setConfiguraciones(await useCases.listar(empresaId));
    } catch (exception) {
      setError(mensajeError(exception));
      setConfiguraciones([]);
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
    setFormulario({ ...FORMULARIO_VACIO, fechaVigenciaDesde: fechaLocalInput(new Date()) });
    setError(null);
    setAviso(null);
    setDialogoAbierto(true);
  };

  const abrirEdicion = (configuracion: ConfiguracionEmpresa) => {
    setEdicion(configuracion);
    setFormulario({
      clave: configuracion.clave,
      valor: configuracion.valor ?? "",
      tipoDato: configuracion.tipoDato,
      fechaVigenciaDesde: fechaParaInput(configuracion.fechaVigenciaDesde),
      fechaVigenciaHasta: fechaParaInput(configuracion.fechaVigenciaHasta),
    });
    setError(null);
    setAviso(null);
    setDialogoAbierto(true);
  };

  const guardar = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError(null);
    setAviso(null);
    if (!empresaSeleccionada || !Number.isSafeInteger(empresaSeleccionada.id)) {
      setError(resolveMessage(MessageCodes.CONFIGURACION_REQUIRED));
      return;
    }
    const clave = formulario.clave.trim();
    const tipoDato = formulario.tipoDato.trim();
    if (!clave || clave.length > 100 || !tipoDato || tipoDato.length > 30) {
      setError(resolveMessage(MessageCodes.CONFIGURACION_FIELDS_INVALID));
      return;
    }
    if (
      !formulario.fechaVigenciaDesde ||
      (formulario.fechaVigenciaHasta &&
        formulario.fechaVigenciaHasta < formulario.fechaVigenciaDesde)
    ) {
      setError(resolveMessage(MessageCodes.CONFIGURACION_DATE_RANGE_INVALID));
      return;
    }
    if (formulario.valor.length > 2000) {
      setError(resolveMessage(MessageCodes.CONFIGURACION_VALUE_INVALID));
      return;
    }

    const datos: ConfiguracionEmpresaDatos = {
      idEmpresa: empresaSeleccionada.id,
      clave,
      valor: formulario.valor,
      tipoDato,
      fechaVigenciaDesde: `${formulario.fechaVigenciaDesde}:00`,
      fechaVigenciaHasta: formulario.fechaVigenciaHasta
        ? `${formulario.fechaVigenciaHasta}:00`
        : null,
    };

    setGuardando(true);
    try {
      if (edicion) {
        await useCases.actualizar(edicion.id, datos);
        setAviso(resolveMessage(MessageCodes.CONFIGURACION_UPDATED));
      } else {
        await useCases.crear(datos);
        setAviso(resolveMessage(MessageCodes.CONFIGURACION_CREATED));
      }
      setDialogoAbierto(false);
      await cargar();
    } catch (exception) {
      setError(mensajeError(exception));
    } finally {
      setGuardando(false);
    }
  };

  const cambiarEstado = async (configuracion: ConfiguracionEmpresa) => {
    setOperacionFilaId(configuracion.id);
    setError(null);
    setAviso(null);
    try {
      if (configuracion.estadoRegistro === "ACTIVO") {
        await useCases.inactivar(configuracion.id);
        setAviso(resolveMessage(MessageCodes.CONFIGURACION_INACTIVATED));
      } else {
        await useCases.reactivar(configuracion.id);
        setAviso(resolveMessage(MessageCodes.CONFIGURACION_REACTIVATED));
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
        resolveMessage(MessageCodes.CONFIGURACION_DELETED, {
          key: confirmacionEliminacion.clave,
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

  const cambiarCampo = (campo: keyof ConfiguracionFormulario, valor: string) => {
    setFormulario((actual) => ({ ...actual, [campo]: valor }));
  };

  return (
    <Box className="configuracion-empresa-page">
      <Box className="configuracion-empresa-heading">
        <Box>
          <Typography className="page-title" component="h1" variant="h4">
            {resolveMessage(MessageCodes.CONFIGURACION_PAGE_TITLE)}
          </Typography>
          <Typography color="text.secondary">
            {resolveMessage(MessageCodes.CONFIGURACION_PAGE_DESCRIPTION)}
          </Typography>
        </Box>
        <Button
          variant="contained"
          startIcon={<AddOutlinedIcon />}
          onClick={abrirCrear}
          disabled={cargandoEmpresas || !empresaSeleccionada}
        >
          {resolveMessage(MessageCodes.CONFIGURACION_NEW)}
        </Button>
      </Box>

      {error && <Alert severity="error" onClose={() => setError(null)}>{error}</Alert>}
      {aviso && <Alert severity="success" onClose={() => setAviso(null)}>{aviso}</Alert>}

      <Paper variant="outlined" className="configuracion-empresa-filter">
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
        <Typography variant="body2" color="text.secondary">
          {resolveMessage(MessageCodes.CONFIGURACION_FILTER_HELP)}
        </Typography>
      </Paper>

      <Box className="configuracion-empresa-list-heading">
        <Box>
          <Typography variant="h6" component="h2">
            {resolveMessage(MessageCodes.CONFIGURACION_LIST_TITLE)}
          </Typography>
          <Typography variant="body2" color="text.secondary">
            {configuraciones.length} {resolveMessage(MessageCodes.CONFIGURACION_RECORDS)}
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

      <TableContainer component={Paper} variant="outlined" className="configuracion-empresa-table">
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>{resolveMessage(MessageCodes.CONFIGURACION_KEY)}</TableCell>
              <TableCell>{resolveMessage(MessageCodes.CONFIGURACION_VALUE)}</TableCell>
              <TableCell>{resolveMessage(MessageCodes.CONFIGURACION_DATA_TYPE)}</TableCell>
              <TableCell>{resolveMessage(MessageCodes.CONFIGURACION_VALID_FROM)}</TableCell>
              <TableCell>{resolveMessage(MessageCodes.CONFIGURACION_VALID_UNTIL)}</TableCell>
              <TableCell>{resolveMessage(MessageCodes.COMMON_STATUS)}</TableCell>
              <TableCell align="right">{resolveMessage(MessageCodes.COMMON_ACTIONS)}</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {cargando ? (
              <TableRow><TableCell colSpan={7} align="center"><CircularProgress size={26} /></TableCell></TableRow>
            ) : configuraciones.length === 0 ? (
              <TableRow><TableCell colSpan={7} align="center">{resolveMessage(MessageCodes.CONFIGURACION_EMPTY)}</TableCell></TableRow>
            ) : (
              configuraciones.map((configuracion) => (
                <TableRow hover key={configuracion.id}>
                  <TableCell>{configuracion.clave}</TableCell>
                  <TableCell className="configuracion-value-cell">{configuracion.valor ?? "—"}</TableCell>
                  <TableCell>{configuracion.tipoDato}</TableCell>
                  <TableCell>{configuracion.fechaVigenciaDesde.replace("T", " ")}</TableCell>
                  <TableCell>{configuracion.fechaVigenciaHasta?.replace("T", " ") ?? "—"}</TableCell>
                  <TableCell>
                    <Chip
                      size="small"
                      label={configuracion.estadoRegistro}
                      color={configuracion.estadoRegistro === "ACTIVO" ? "success" : "default"}
                    />
                  </TableCell>
                  <TableCell align="right">
                    <Box className="configuracion-empresa-row-actions">
                      <Button size="small" onClick={() => abrirEdicion(configuracion)} startIcon={<EditOutlinedIcon />}>
                        {resolveMessage(MessageCodes.COMMON_EDIT)}
                      </Button>
                      <Button
                        size="small"
                        onClick={() => void cambiarEstado(configuracion)}
                        disabled={operacionFilaId !== null || guardando}
                        startIcon={configuracion.estadoRegistro === "ACTIVO" ? <BlockOutlinedIcon /> : <ReplayOutlinedIcon />}
                      >
                        {resolveMessage(configuracion.estadoRegistro === "ACTIVO" ? MessageCodes.CONFIGURACION_INACTIVATE : MessageCodes.CONFIGURACION_REACTIVATE)}
                      </Button>
                      <Button
                        size="small"
                        color="error"
                        onClick={() => setConfirmacionEliminacion(configuracion)}
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
            {resolveMessage(edicion ? MessageCodes.CONFIGURACION_EDIT_TITLE : MessageCodes.CONFIGURACION_CREATE_TITLE)}
          </DialogTitle>
          <DialogContent>
            <Typography className="configuracion-empresa-dialog-description" color="text.secondary">
              {resolveMessage(MessageCodes.CONFIGURACION_FORM_DESCRIPTION)}
            </Typography>
            <Box className="configuracion-empresa-form-grid">
              <TextField
                label={resolveMessage(MessageCodes.ESTABLECIMIENTO_COMPANY)}
                value={empresaSeleccionada ? `${empresaSeleccionada.ruc} - ${empresaSeleccionada.razonSocial}` : ""}
                disabled
                fullWidth
              />
              <TextField
                label={resolveMessage(MessageCodes.CONFIGURACION_KEY)}
                value={formulario.clave}
                onChange={(event) => cambiarCampo("clave", event.target.value)}
                required
                inputProps={{ maxLength: 100 }}
              />
              <TextField
                label={resolveMessage(MessageCodes.CONFIGURACION_DATA_TYPE)}
                value={formulario.tipoDato}
                onChange={(event) => cambiarCampo("tipoDato", event.target.value)}
                required
                inputProps={{ maxLength: 30 }}
                helperText={resolveMessage(MessageCodes.CONFIGURACION_DATA_TYPE_HELP)}
              />
              <TextField
                label={resolveMessage(MessageCodes.CONFIGURACION_VALUE)}
                value={formulario.valor}
                onChange={(event) => cambiarCampo("valor", event.target.value)}
                inputProps={{ maxLength: 2000 }}
                multiline
                minRows={2}
                maxRows={5}
              />
              <TextField
                label={resolveMessage(MessageCodes.CONFIGURACION_VALID_FROM)}
                type="datetime-local"
                value={formulario.fechaVigenciaDesde}
                onChange={(event) => cambiarCampo("fechaVigenciaDesde", event.target.value)}
                required
                InputLabelProps={{ shrink: true }}
              />
              <TextField
                label={resolveMessage(MessageCodes.CONFIGURACION_VALID_UNTIL)}
                type="datetime-local"
                value={formulario.fechaVigenciaHasta}
                onChange={(event) => cambiarCampo("fechaVigenciaHasta", event.target.value)}
                InputLabelProps={{ shrink: true }}
              />
            </Box>
          </DialogContent>
          <DialogActions className="configuracion-empresa-dialog-actions">
            <Button onClick={() => setDialogoAbierto(false)} disabled={guardando}>
              {resolveMessage(MessageCodes.COMMON_CANCEL)}
            </Button>
            <Button type="submit" variant="contained" disabled={guardando}>
              {guardando ? resolveMessage(MessageCodes.CONFIGURACION_SAVING) : resolveMessage(MessageCodes.COMMON_SAVE)}
            </Button>
          </DialogActions>
        </Box>
      </Dialog>

      <Dialog open={Boolean(confirmacionEliminacion)} onClose={() => !guardando && setConfirmacionEliminacion(null)} fullWidth maxWidth="xs">
        <DialogTitle>{resolveMessage(MessageCodes.CONFIGURACION_DELETE_TITLE)}</DialogTitle>
        <DialogContent>
          <Typography>
            {resolveMessage(MessageCodes.CONFIGURACION_DELETE_CONFIRM_PREFIX)}{" "}
            <strong>{confirmacionEliminacion?.clave}</strong>?
          </Typography>
          <Typography color="text.secondary" sx={{ mt: 1 }}>
            {resolveMessage(MessageCodes.CONFIGURACION_DELETE_CONFIRM_DESCRIPTION)}
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setConfirmacionEliminacion(null)} disabled={guardando}>
            {resolveMessage(MessageCodes.COMMON_CANCEL)}
          </Button>
          <Button color="error" variant="contained" onClick={() => void eliminar()} disabled={guardando}>
            {guardando ? resolveMessage(MessageCodes.COMMON_DELETING) : resolveMessage(MessageCodes.CONFIGURACION_DELETE_CONFIRM)}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
