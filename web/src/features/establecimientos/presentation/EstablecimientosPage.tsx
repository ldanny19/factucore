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
  FormControl,
  InputLabel,
  MenuItem,
  Paper,
  Select,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Typography,
} from "@mui/material";
import AddBusinessOutlinedIcon from "@mui/icons-material/AddBusinessOutlined";
import SearchOutlinedIcon from "@mui/icons-material/SearchOutlined";
import EditOutlinedIcon from "@mui/icons-material/EditOutlined";
import DeleteOutlineOutlinedIcon from "@mui/icons-material/DeleteOutlineOutlined";
import RefreshOutlinedIcon from "@mui/icons-material/RefreshOutlined";
import { useEmpresaUseCases } from "../../empresas/presentation/hooks/EmpresaUseCasesContext";
import { MessageCodes } from "../../../shared/messages/MessageCodes";
import { resolveMessage } from "../../../shared/messages/MessageResolver";
import type { Empresa } from "../../empresas/domain/Empresa";
import type { Establecimiento, EstablecimientoDatos } from "../domain/Establecimiento";
import { useEstablecimientoUseCase } from "./hooks/EstablecimientoUseCaseContext";
import "../../../styles/app.css";

const FORMULARIO_VACIO: EstablecimientoDatos = {
  idEmpresa: 0,
  codigo: "",
  nombre: "",
  direccion: "",
};

function mensajeError(error: unknown): string {
  if (error instanceof Error && error.message.trim()) {
    return error.message;
  }
  return resolveMessage(MessageCodes.COMMON_OPERATION_FAILED);
}

export default function EstablecimientosPage() {
  const establecimientoUseCases = useEstablecimientoUseCase();
  const empresaUseCases = useEmpresaUseCases();
  const [establecimientos, setEstablecimientos] = useState<Establecimiento[]>([]);
  const [empresas, setEmpresas] = useState<Empresa[]>([]);
  const [empresaFiltroId, setEmpresaFiltroId] = useState<number | "">("");
  const [cargando, setCargando] = useState(true);
  const [cargandoEmpresas, setCargandoEmpresas] = useState(true);
  const [guardando, setGuardando] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [aviso, setAviso] = useState<string | null>(null);
  const [dialogoAbierto, setDialogoAbierto] = useState(false);
  const [establecimientoEdicion, setEstablecimientoEdicion] = useState<Establecimiento | null>(
    null,
  );
  const [formulario, setFormulario] = useState<EstablecimientoDatos>(FORMULARIO_VACIO);
  const [idBusqueda, setIdBusqueda] = useState("");
  const [confirmacionEliminacion, setConfirmacionEliminacion] = useState<Establecimiento | null>(
    null,
  );

  const empresasPorId = useMemo(
    () => new Map(empresas.map((empresa) => [empresa.id, empresa])),
    [empresas],
  );

  const cargarEstablecimientos = useCallback(async (idEmpresa: number) => {
    setCargando(true);
    setError(null);
    try {
      setEstablecimientos(await establecimientoUseCases.listar(idEmpresa));
    } catch (exception) {
      setError(mensajeError(exception));
    } finally {
      setCargando(false);
    }
  }, [establecimientoUseCases]);

  const cargarEmpresas = useCallback(async () => {
    setCargandoEmpresas(true);
    try {
      const resultado = await empresaUseCases.listar();
      setEmpresas(resultado);
      setEmpresaFiltroId((actual) => actual || resultado[0]?.id || "");
    } catch (exception) {
      setError(mensajeError(exception));
    } finally {
      setCargandoEmpresas(false);
    }
  }, [empresaUseCases]);

  useEffect(() => {
    void cargarEmpresas();
  }, [cargarEmpresas]);

  useEffect(() => {
    if (empresaFiltroId === "") {
      setEstablecimientos([]);
      setCargando(false);
      return;
    }
    void cargarEstablecimientos(empresaFiltroId);
  }, [empresaFiltroId, cargarEstablecimientos]);

  const abrirCrear = () => {
    setEstablecimientoEdicion(null);
    setFormulario({
      ...FORMULARIO_VACIO,
      idEmpresa: empresaFiltroId === "" ? 0 : empresaFiltroId,
    });
    setError(null);
    setAviso(null);
    setDialogoAbierto(true);
  };

  const abrirEdicion = (establecimiento: Establecimiento) => {
    setEstablecimientoEdicion(establecimiento);
    setFormulario({
      idEmpresa: establecimiento.idEmpresa,
      codigo: establecimiento.codigo,
      nombre: establecimiento.nombre,
      direccion: establecimiento.direccion,
    });
    setError(null);
    setAviso(null);
    setDialogoAbierto(true);
  };

  const guardar = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError(null);
    setAviso(null);

    const datos: EstablecimientoDatos = {
      ...formulario,
      codigo: formulario.codigo.trim(),
      nombre: formulario.nombre.trim(),
      direccion: formulario.direccion.trim(),
    };

    if (!Number.isSafeInteger(datos.idEmpresa) || datos.idEmpresa <= 0) {
      setError(resolveMessage(MessageCodes.ESTABLECIMIENTO_EMPRESA_REQUIRED));
      return;
    }
    if (!datos.codigo) {
      setError(resolveMessage(MessageCodes.ESTABLECIMIENTO_CODE_REQUIRED));
      return;
    }
    if (!datos.nombre) {
      setError(resolveMessage(MessageCodes.ESTABLECIMIENTO_NAME_REQUIRED));
      return;
    }
    if (!datos.direccion) {
      setError(resolveMessage(MessageCodes.ESTABLECIMIENTO_ADDRESS_REQUIRED));
      return;
    }

    setGuardando(true);
    try {
      if (establecimientoEdicion) {
        await establecimientoUseCases.actualizar(establecimientoEdicion.id, datos);
        setAviso(resolveMessage(MessageCodes.ESTABLECIMIENTO_UPDATED));
      } else {
        await establecimientoUseCases.crear(datos);
        setAviso(resolveMessage(MessageCodes.ESTABLECIMIENTO_CREATED));
      }
      setDialogoAbierto(false);
      setIdBusqueda("");
      if (empresaFiltroId !== "") {
        await cargarEstablecimientos(empresaFiltroId);
      }
    } catch (exception) {
      setError(mensajeError(exception));
    } finally {
      setGuardando(false);
    }
  };

  const buscarPorId = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError(null);
    setAviso(null);
    setEstablecimientos([]);

    const id = Number(idBusqueda);
    if (!Number.isSafeInteger(id) || id <= 0) {
      setError(resolveMessage(MessageCodes.ESTABLECIMIENTO_ID_INVALID));
      return;
    }

    setCargando(true);
    try {
      const establecimiento = await establecimientoUseCases.obtener(id);
      setEstablecimientos([establecimiento]);
      setAviso(
        resolveMessage(MessageCodes.ESTABLECIMIENTO_FOUND, {
          id: establecimiento.id,
        }),
      );
    } catch (exception) {
      setEstablecimientos([]);
      setError(mensajeError(exception));
    } finally {
      setCargando(false);
    }
  };

  const buscarTodas = async () => {
    setIdBusqueda("");
    setAviso(null);
    if (empresaFiltroId !== "") {
      await cargarEstablecimientos(empresaFiltroId);
    }
  };

  const confirmarEliminacion = async () => {
    if (!confirmacionEliminacion) return;
    setGuardando(true);
    setError(null);
    setAviso(null);
    try {
      await establecimientoUseCases.eliminar(confirmacionEliminacion.id);
      setAviso(
        resolveMessage(MessageCodes.ESTABLECIMIENTO_DELETED, {
          name: confirmacionEliminacion.nombre,
        }),
      );
      setConfirmacionEliminacion(null);
      if (empresaFiltroId !== "") {
        await cargarEstablecimientos(empresaFiltroId);
      }
    } catch (exception) {
      setError(mensajeError(exception));
    } finally {
      setGuardando(false);
    }
  };

  const cambiarCampo = (campo: keyof EstablecimientoDatos, valor: string | number) => {
    setFormulario((actual) => ({ ...actual, [campo]: valor }));
  };

  return (
    <Box className="establecimientos-page">
      <Box className="establecimientos-page-heading">
        <Box>
          <Typography className="page-title" component="h1" variant="h4">
            {resolveMessage(MessageCodes.ESTABLECIMIENTO_PAGE_TITLE)}
          </Typography>
          <Typography color="text.secondary">
            {resolveMessage(MessageCodes.ESTABLECIMIENTO_PAGE_DESCRIPTION)}
          </Typography>
        </Box>
        <Button
          variant="contained"
          startIcon={<AddBusinessOutlinedIcon />}
          onClick={abrirCrear}
          disabled={cargandoEmpresas || empresas.length === 0}
        >
          {resolveMessage(MessageCodes.ESTABLECIMIENTO_NEW)}
        </Button>
      </Box>

      {error && (
        <Alert severity="error" onClose={() => setError(null)}>
          {error}
        </Alert>
      )}
      {aviso && (
        <Alert severity="success" onClose={() => setAviso(null)}>
          {aviso}
        </Alert>
      )}

      <Paper component="section" variant="outlined" className="establecimientos-search-panel">
        <Typography variant="h6" component="h2">
          {resolveMessage(MessageCodes.ESTABLECIMIENTO_SEARCH_TITLE)}
        </Typography>
        <Box component="form" className="establecimientos-search-form" onSubmit={buscarPorId}>
          <FormControl size="small" sx={{ minWidth: 260 }}>
            <InputLabel id="establecimiento-filtro-empresa-label">
              {resolveMessage(MessageCodes.ESTABLECIMIENTO_COMPANY)}
            </InputLabel>
            <Select
              labelId="establecimiento-filtro-empresa-label"
              label={resolveMessage(MessageCodes.ESTABLECIMIENTO_COMPANY)}
              value={empresaFiltroId}
              onChange={(event) => setEmpresaFiltroId(Number(event.target.value))}
              disabled={cargandoEmpresas || empresas.length === 0}
            >
              {empresas.map((empresa) => (
                <MenuItem key={empresa.id} value={empresa.id}>
                  {empresa.ruc} - {empresa.razonSocial}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
          <TextField
            label={resolveMessage(MessageCodes.ESTABLECIMIENTO_ID_LABEL)}
            type="number"
            size="small"
            value={idBusqueda}
            onChange={(event) => setIdBusqueda(event.target.value)}
            inputProps={{ min: 1, step: 1 }}
          />
          <Button
            type="submit"
            variant="outlined"
            startIcon={<SearchOutlinedIcon />}
            disabled={empresaFiltroId === ""}
          >
            {resolveMessage(MessageCodes.COMMON_SEARCH)}
          </Button>
          <Button
            type="button"
            variant="outlined"
            startIcon={<RefreshOutlinedIcon />}
            onClick={() => void buscarTodas()}
            disabled={cargando || empresaFiltroId === ""}
          >
            {resolveMessage(MessageCodes.COMMON_REFRESH)}
          </Button>
          <Button
            type="button"
            variant="text"
            onClick={() => {
              setIdBusqueda("");
              setError(null);
              setAviso(null);
            }}
          >
            {resolveMessage(MessageCodes.COMMON_CLEAR_SEARCH)}
          </Button>
        </Box>
      </Paper>

      <Box className="establecimientos-list-heading">
        <Box>
          <Typography variant="h6" component="h2">
            {resolveMessage(MessageCodes.ESTABLECIMIENTO_LIST_TITLE)}
          </Typography>
          <Typography variant="body2" color="text.secondary">
            {establecimientos.length} {resolveMessage(MessageCodes.ESTABLECIMIENTO_RECORDS)}
          </Typography>
        </Box>
        <Button
          variant="text"
          startIcon={<RefreshOutlinedIcon />}
          onClick={() => empresaFiltroId !== "" && void cargarEstablecimientos(empresaFiltroId)}
          disabled={cargando || empresaFiltroId === ""}
        >
          {resolveMessage(MessageCodes.COMMON_REFRESH)}
        </Button>
      </Box>

      <TableContainer component={Paper} variant="outlined" className="establecimientos-table-panel">
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>{resolveMessage(MessageCodes.ESTABLECIMIENTO_ID_LABEL)}</TableCell>
              <TableCell>{resolveMessage(MessageCodes.ESTABLECIMIENTO_COMPANY)}</TableCell>
              <TableCell>{resolveMessage(MessageCodes.ESTABLECIMIENTO_CODE)}</TableCell>
              <TableCell>{resolveMessage(MessageCodes.ESTABLECIMIENTO_NAME)}</TableCell>
              <TableCell>{resolveMessage(MessageCodes.ESTABLECIMIENTO_ADDRESS)}</TableCell>
              <TableCell>{resolveMessage(MessageCodes.COMMON_STATUS)}</TableCell>
              <TableCell align="right">{resolveMessage(MessageCodes.COMMON_ACTIONS)}</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {cargando ? (
              <TableRow>
                <TableCell colSpan={7} align="center">
                  <CircularProgress size={26} />
                </TableCell>
              </TableRow>
            ) : establecimientos.length === 0 ? (
              <TableRow>
                <TableCell colSpan={7} align="center">
                  {resolveMessage(MessageCodes.ESTABLECIMIENTO_EMPTY)}
                </TableCell>
              </TableRow>
            ) : (
              establecimientos.map((establecimiento) => {
                const empresa = empresasPorId.get(establecimiento.idEmpresa);
                return (
                  <TableRow hover key={establecimiento.id}>
                    <TableCell>{establecimiento.id}</TableCell>
                    <TableCell>
                      {empresa
                        ? `${empresa.ruc} - ${empresa.razonSocial}`
                        : establecimiento.idEmpresa}
                    </TableCell>
                    <TableCell>{establecimiento.codigo}</TableCell>
                    <TableCell>{establecimiento.nombre}</TableCell>
                    <TableCell>{establecimiento.direccion}</TableCell>
                    <TableCell>
                      <Chip
                        size="small"
                        label={establecimiento.estadoRegistro}
                        color={establecimiento.estadoRegistro === "ACTIVO" ? "success" : "default"}
                      />
                    </TableCell>
                    <TableCell align="right">
                      <Box className="establecimientos-row-actions">
                        <Button
                          size="small"
                          onClick={() => abrirEdicion(establecimiento)}
                          startIcon={<EditOutlinedIcon />}
                        >
                          {resolveMessage(MessageCodes.COMMON_EDIT)}
                        </Button>
                        <Button
                          size="small"
                          color="error"
                          onClick={() => setConfirmacionEliminacion(establecimiento)}
                          startIcon={<DeleteOutlineOutlinedIcon />}
                        >
                          {resolveMessage(MessageCodes.COMMON_DELETE)}
                        </Button>
                      </Box>
                    </TableCell>
                  </TableRow>
                );
              })
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
        <Box component="form" onSubmit={guardar}>
          <DialogTitle>
            {resolveMessage(
              establecimientoEdicion
                ? MessageCodes.ESTABLECIMIENTO_EDIT_TITLE
                : MessageCodes.ESTABLECIMIENTO_CREATE_TITLE,
            )}
          </DialogTitle>
          <DialogContent>
            <Typography color="text.secondary" className="establecimientos-dialog-description">
              {resolveMessage(MessageCodes.ESTABLECIMIENTO_FORM_DESCRIPTION)}
            </Typography>
            <Box className="establecimientos-form-grid">
              <FormControl fullWidth required>
                <InputLabel id="establecimiento-empresa-label">
                  {resolveMessage(MessageCodes.ESTABLECIMIENTO_COMPANY)}
                </InputLabel>
                <Select
                  labelId="establecimiento-empresa-label"
                  label={resolveMessage(MessageCodes.ESTABLECIMIENTO_COMPANY)}
                  value={formulario.idEmpresa || ""}
                  onChange={(event) => cambiarCampo("idEmpresa", Number(event.target.value))}
                  disabled={cargandoEmpresas || empresas.length === 0}
                >
                  {empresas.map((empresa) => (
                    <MenuItem key={empresa.id} value={empresa.id}>
                      {empresa.ruc} - {empresa.razonSocial}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
              <TextField
                label={resolveMessage(MessageCodes.ESTABLECIMIENTO_CODE)}
                value={formulario.codigo}
                onChange={(event) => cambiarCampo("codigo", event.target.value)}
                inputProps={{ maxLength: 3 }}
                required
                fullWidth
              />
              <TextField
                label={resolveMessage(MessageCodes.ESTABLECIMIENTO_NAME)}
                value={formulario.nombre}
                onChange={(event) => cambiarCampo("nombre", event.target.value)}
                inputProps={{ maxLength: 200 }}
                required
                fullWidth
              />
              <TextField
                label={resolveMessage(MessageCodes.ESTABLECIMIENTO_ADDRESS)}
                value={formulario.direccion}
                onChange={(event) => cambiarCampo("direccion", event.target.value)}
                inputProps={{ maxLength: 500 }}
                required
                fullWidth
              />
            </Box>
          </DialogContent>
          <DialogActions className="establecimientos-dialog-actions">
            <Button onClick={() => setDialogoAbierto(false)} disabled={guardando}>
              {resolveMessage(MessageCodes.COMMON_CANCEL)}
            </Button>
            <Button type="submit" variant="contained" disabled={guardando}>
              {resolveMessage(
                guardando ? MessageCodes.ESTABLECIMIENTO_SAVING : MessageCodes.COMMON_SAVE,
              )}
            </Button>
          </DialogActions>
        </Box>
      </Dialog>

      <Dialog
        open={Boolean(confirmacionEliminacion)}
        onClose={() => !guardando && setConfirmacionEliminacion(null)}
      >
        <DialogTitle>{resolveMessage(MessageCodes.ESTABLECIMIENTO_DELETE_TITLE)}</DialogTitle>
        <DialogContent>
          <Typography>
            {resolveMessage(MessageCodes.ESTABLECIMIENTO_DELETE_CONFIRM_PREFIX)}{" "}
            <strong>{confirmacionEliminacion?.nombre}</strong>?
          </Typography>
          <Typography color="text.secondary">
            {resolveMessage(MessageCodes.ESTABLECIMIENTO_DELETE_CONFIRM_DESCRIPTION)}
          </Typography>
        </DialogContent>
        <DialogActions className="establecimientos-dialog-actions">
          <Button onClick={() => setConfirmacionEliminacion(null)} disabled={guardando}>
            {resolveMessage(MessageCodes.COMMON_CANCEL)}
          </Button>
          <Button
            color="error"
            variant="contained"
            onClick={() => void confirmarEliminacion()}
            disabled={guardando}
          >
            {resolveMessage(
              guardando
                ? MessageCodes.COMMON_DELETING
                : MessageCodes.ESTABLECIMIENTO_DELETE_CONFIRM,
            )}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
