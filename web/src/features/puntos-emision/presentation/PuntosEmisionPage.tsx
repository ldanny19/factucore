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
import DeleteOutlineOutlinedIcon from "@mui/icons-material/DeleteOutlineOutlined";
import EditOutlinedIcon from "@mui/icons-material/EditOutlined";
import RefreshOutlinedIcon from "@mui/icons-material/RefreshOutlined";
import SearchOutlinedIcon from "@mui/icons-material/SearchOutlined";
import { useEmpresaUseCases } from "../../empresas/presentation/hooks/EmpresaUseCasesContext";
import type { Empresa } from "../../empresas/domain/Empresa";
import { useEstablecimientoUseCase } from "../../establecimientos/presentation/hooks/EstablecimientoUseCaseContext";
import type { Establecimiento } from "../../establecimientos/domain/Establecimiento";
import { MessageCodes } from "../../../shared/messages/MessageCodes";
import { resolveMessage } from "../../../shared/messages/MessageResolver";
import type { PuntoEmision, PuntoEmisionDatos } from "../domain/PuntoEmision";
import { usePuntoEmisionUseCase } from "./hooks/PuntoEmisionUseCaseContext";
import "../../../styles/app.css";

const FORMULARIO_VACIO: PuntoEmisionDatos = {
  idEmpresa: 0,
  idEstablecimiento: 0,
  codigo: "",
  nombre: "",
};

function mensajeError(error: unknown): string {
  if (error instanceof Error && error.message.trim()) {
    return error.message;
  }
  return resolveMessage(MessageCodes.COMMON_OPERATION_FAILED);
}

export default function PuntosEmisionPage() {
  const puntoEmisionUseCases = usePuntoEmisionUseCase();
  const empresaUseCases = useEmpresaUseCases();
  const establecimientoUseCases = useEstablecimientoUseCase();
  const [empresas, setEmpresas] = useState<Empresa[]>([]);
  const [establecimientos, setEstablecimientos] = useState<Establecimiento[]>([]);
  const [establecimientoEdicionExtra, setEstablecimientoEdicionExtra] =
    useState<Establecimiento | null>(null);
  const [puntosEmision, setPuntosEmision] = useState<PuntoEmision[]>([]);
  const [empresaFiltroId, setEmpresaFiltroId] = useState<number | "">("");
  const [establecimientoFiltroId, setEstablecimientoFiltroId] = useState<number | "">("");
  const [cargandoEmpresas, setCargandoEmpresas] = useState(true);
  const [cargandoEstablecimientos, setCargandoEstablecimientos] = useState(false);
  const [cargandoPuntos, setCargandoPuntos] = useState(false);
  const [guardando, setGuardando] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [aviso, setAviso] = useState<string | null>(null);
  const [dialogoAbierto, setDialogoAbierto] = useState(false);
  const [puntoEmisionEdicion, setPuntoEmisionEdicion] = useState<PuntoEmision | null>(null);
  const [formulario, setFormulario] = useState<PuntoEmisionDatos>(FORMULARIO_VACIO);
  const [idBusqueda, setIdBusqueda] = useState("");
  const [confirmacionEliminacion, setConfirmacionEliminacion] = useState<PuntoEmision | null>(null);

  const empresasPorId = useMemo(
    () => new Map(empresas.map((empresa) => [empresa.id, empresa])),
    [empresas],
  );
  const establecimientosPorId = useMemo(
    () =>
      new Map(
        [
          ...establecimientos,
          ...(establecimientoEdicionExtra ? [establecimientoEdicionExtra] : []),
        ].map((establecimiento) => [establecimiento.id, establecimiento]),
      ),
    [establecimientos, establecimientoEdicionExtra],
  );
  const establecimientosFormulario = useMemo(() => {
    if (
      !establecimientoEdicionExtra ||
      establecimientos.some(({ id }) => id === establecimientoEdicionExtra.id)
    ) {
      return establecimientos;
    }
    return [...establecimientos, establecimientoEdicionExtra];
  }, [establecimientos, establecimientoEdicionExtra]);

  const cargarPuntosEmision = useCallback(
    async (idEstablecimiento: number) => {
      setCargandoPuntos(true);
      setError(null);
      try {
        setPuntosEmision(await puntoEmisionUseCases.listar(idEstablecimiento));
      } catch (exception) {
        setError(mensajeError(exception));
        setPuntosEmision([]);
      } finally {
        setCargandoPuntos(false);
      }
    },
    [puntoEmisionUseCases],
  );

  const cargarEmpresas = useCallback(async () => {
    setCargandoEmpresas(true);
    setError(null);
    try {
      const resultado = await empresaUseCases.listar();
      setEmpresas(resultado);
    } catch (exception) {
      setError(mensajeError(exception));
    } finally {
      setCargandoEmpresas(false);
    }
  }, [empresaUseCases]);

  const cargarEstablecimientos = useCallback(
    async (idEmpresa: number) => {
      setCargandoEstablecimientos(true);
      setEstablecimientoFiltroId("");
      setPuntosEmision([]);
      setError(null);
      try {
        const resultado = await establecimientoUseCases.listar(idEmpresa);
        setEstablecimientos(resultado);
      } catch (exception) {
        setError(mensajeError(exception));
        setEstablecimientos([]);
      } finally {
        setCargandoEstablecimientos(false);
      }
    },
    [establecimientoUseCases],
  );

  useEffect(() => {
    void cargarEmpresas();
  }, [cargarEmpresas]);

  useEffect(() => {
    if (empresaFiltroId === "") {
      setEstablecimientos([]);
      setEstablecimientoFiltroId("");
      setPuntosEmision([]);
      setCargandoEstablecimientos(false);
      return;
    }
    void cargarEstablecimientos(empresaFiltroId);
  }, [empresaFiltroId, cargarEstablecimientos]);

  useEffect(() => {
    if (establecimientoFiltroId === "") {
      setPuntosEmision([]);
      setCargandoPuntos(false);
      return;
    }
    void cargarPuntosEmision(establecimientoFiltroId);
  }, [establecimientoFiltroId, cargarPuntosEmision]);

  const abrirCrear = () => {
    const establecimiento = establecimientosPorId.get(Number(establecimientoFiltroId));
    setPuntoEmisionEdicion(null);
    setEstablecimientoEdicionExtra(null);
    setFormulario({
      ...FORMULARIO_VACIO,
      idEmpresa: establecimiento?.idEmpresa ?? 0,
      idEstablecimiento: establecimiento?.id ?? 0,
    });
    setError(null);
    setAviso(null);
    setDialogoAbierto(true);
  };

  const abrirEdicion = async (puntoEmision: PuntoEmision) => {
    setError(null);
    setAviso(null);
    try {
      const establecimiento = await establecimientoUseCases.obtener(puntoEmision.idEstablecimiento);
      setEstablecimientoEdicionExtra(establecimiento);
      setPuntoEmisionEdicion(puntoEmision);
      setFormulario({
        idEmpresa: establecimiento.idEmpresa,
        idEstablecimiento: establecimiento.id,
        codigo: puntoEmision.codigo,
        nombre: puntoEmision.nombre,
      });
      setDialogoAbierto(true);
    } catch (exception) {
      setError(mensajeError(exception));
    }
  };

  const guardar = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError(null);
    setAviso(null);

    const datos: PuntoEmisionDatos = {
      ...formulario,
      codigo: formulario.codigo.trim(),
      nombre: formulario.nombre.trim(),
    };
    if (!Number.isSafeInteger(datos.idEmpresa) || datos.idEmpresa <= 0) {
      setError(resolveMessage(MessageCodes.PUNTO_EMISION_COMPANY_REQUIRED));
      return;
    }
    const establecimiento = establecimientosPorId.get(datos.idEstablecimiento);
    if (!establecimiento || establecimiento.idEmpresa !== datos.idEmpresa) {
      setError(resolveMessage(MessageCodes.PUNTO_EMISION_ESTABLISHMENT_REQUIRED));
      return;
    }
    if (!datos.codigo || datos.codigo.length > 3) {
      setError(resolveMessage(MessageCodes.PUNTO_EMISION_CODE_REQUIRED));
      return;
    }
    if (!datos.nombre || datos.nombre.length > 300) {
      setError(resolveMessage(MessageCodes.PUNTO_EMISION_NAME_REQUIRED));
      return;
    }

    setGuardando(true);
    try {
      if (puntoEmisionEdicion) {
        await puntoEmisionUseCases.actualizar(puntoEmisionEdicion.id, datos);
        setAviso(resolveMessage(MessageCodes.PUNTO_EMISION_UPDATED));
      } else {
        await puntoEmisionUseCases.crear(datos);
        setAviso(resolveMessage(MessageCodes.PUNTO_EMISION_CREATED));
      }
      setDialogoAbierto(false);
      setEstablecimientoEdicionExtra(null);
      setIdBusqueda("");
      if (establecimientoFiltroId !== "") {
        await cargarPuntosEmision(establecimientoFiltroId);
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
    const id = Number(idBusqueda);
    if (!Number.isSafeInteger(id) || id <= 0) {
      setError(resolveMessage(MessageCodes.PUNTO_EMISION_ID_INVALID));
      return;
    }

    setCargandoPuntos(true);
    try {
      const puntoEmision = await puntoEmisionUseCases.obtener(id);
      setPuntosEmision([puntoEmision]);
      setAviso(resolveMessage(MessageCodes.PUNTO_EMISION_FOUND, { id: puntoEmision.id }));
    } catch (exception) {
      setPuntosEmision([]);
      setError(mensajeError(exception));
    } finally {
      setCargandoPuntos(false);
    }
  };

  const buscarTodos = async () => {
    setIdBusqueda("");
    setAviso(null);
    if (establecimientoFiltroId !== "") {
      await cargarPuntosEmision(establecimientoFiltroId);
    }
  };

  const confirmarEliminacion = async () => {
    if (!confirmacionEliminacion) return;
    setGuardando(true);
    setError(null);
    setAviso(null);
    try {
      await puntoEmisionUseCases.eliminar(confirmacionEliminacion.id);
      setAviso(
        resolveMessage(MessageCodes.PUNTO_EMISION_DELETED, {
          name: confirmacionEliminacion.nombre,
        }),
      );
      setConfirmacionEliminacion(null);
      if (establecimientoFiltroId !== "") {
        await cargarPuntosEmision(establecimientoFiltroId);
      }
    } catch (exception) {
      setError(mensajeError(exception));
    } finally {
      setGuardando(false);
    }
  };

  const cambiarCampo = (campo: keyof PuntoEmisionDatos, valor: string | number) => {
    setFormulario((actual) => ({ ...actual, [campo]: valor }));
  };

  return (
    <Box className="puntos-emision-page">
      <Box className="puntos-emision-page-heading">
        <Box>
          <Typography className="page-title" component="h1" variant="h4">
            {resolveMessage(MessageCodes.PUNTO_EMISION_PAGE_TITLE)}
          </Typography>
          <Typography color="text.secondary">
            {resolveMessage(MessageCodes.PUNTO_EMISION_PAGE_DESCRIPTION)}
          </Typography>
        </Box>
        <Button
          variant="contained"
          startIcon={<AddBusinessOutlinedIcon />}
          onClick={abrirCrear}
          disabled={cargandoEmpresas || establecimientos.length === 0}
        >
          {resolveMessage(MessageCodes.PUNTO_EMISION_NEW)}
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

      <Paper component="section" variant="outlined" className="puntos-emision-search-panel">
        <Typography variant="h6" component="h2">
          {resolveMessage(MessageCodes.PUNTO_EMISION_SEARCH_TITLE)}
        </Typography>
        <Box component="form" className="puntos-emision-search-form" onSubmit={buscarPorId}>
          <FormControl size="small" sx={{ minWidth: 260 }}>
            <InputLabel id="punto-emision-empresa-label" shrink>
              {resolveMessage(MessageCodes.ESTABLECIMIENTO_COMPANY)}
            </InputLabel>
            <Select
              labelId="punto-emision-empresa-label"
              label={resolveMessage(MessageCodes.ESTABLECIMIENTO_COMPANY)}
              value={empresaFiltroId}
              displayEmpty
              onChange={(event) => {
                const value = event.target.value;
                setEmpresaFiltroId(String(value ?? "") === "" ? "" : Number(value));
              }}
              disabled={cargandoEmpresas || empresas.length === 0}
            >
              <MenuItem value="">{resolveMessage(MessageCodes.COMMON_SELECT_OPTION)}</MenuItem>
              {empresas.map((empresa) => (
                <MenuItem key={empresa.id} value={empresa.id}>
                  {empresa.ruc} - {empresa.razonSocial}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
          <FormControl size="small" sx={{ minWidth: 260 }}>
            <InputLabel id="punto-emision-establecimiento-label" shrink>
              {resolveMessage(MessageCodes.PUNTO_EMISION_ESTABLISHMENT)}
            </InputLabel>
            <Select
              labelId="punto-emision-establecimiento-label"
              label={resolveMessage(MessageCodes.PUNTO_EMISION_ESTABLISHMENT)}
              value={establecimientoFiltroId}
              displayEmpty
              onChange={(event) => {
                const value = event.target.value;
                setEstablecimientoFiltroId(
                  String(value ?? "") === "" ? "" : Number(value),
                );
              }}
              disabled={cargandoEstablecimientos || establecimientos.length === 0}
            >
              <MenuItem value="">{resolveMessage(MessageCodes.COMMON_SELECT_OPTION)}</MenuItem>
              {establecimientos.map((establecimiento) => (
                <MenuItem key={establecimiento.id} value={establecimiento.id}>
                  {establecimiento.codigo} - {establecimiento.nombre}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
          <TextField
            label={resolveMessage(MessageCodes.PUNTO_EMISION_ID_LABEL)}
            type="number"
            size="small"
            value={idBusqueda}
            onChange={(event) => setIdBusqueda(event.target.value)}
            inputProps={{ min: 1, step: 1 }}
          />
          <Button type="submit" variant="outlined" startIcon={<SearchOutlinedIcon />}>
            {resolveMessage(MessageCodes.COMMON_SEARCH)}
          </Button>
          <Button
            type="button"
            variant="outlined"
            startIcon={<RefreshOutlinedIcon />}
            onClick={() => void buscarTodos()}
            disabled={cargandoPuntos || establecimientoFiltroId === ""}
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

      <Box className="puntos-emision-list-heading">
        <Box>
          <Typography variant="h6" component="h2">
            {resolveMessage(MessageCodes.PUNTO_EMISION_LIST_TITLE)}
          </Typography>
          <Typography variant="body2" color="text.secondary">
            {puntosEmision.length} {resolveMessage(MessageCodes.PUNTO_EMISION_RECORDS)}
          </Typography>
        </Box>
        <Button
          variant="text"
          startIcon={<RefreshOutlinedIcon />}
          onClick={() => void buscarTodos()}
          disabled={cargandoPuntos || establecimientoFiltroId === ""}
        >
          {resolveMessage(MessageCodes.COMMON_REFRESH)}
        </Button>
      </Box>

      <TableContainer component={Paper} variant="outlined" className="puntos-emision-table-panel">
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>{resolveMessage(MessageCodes.PUNTO_EMISION_ID_LABEL)}</TableCell>
              <TableCell>{resolveMessage(MessageCodes.ESTABLECIMIENTO_COMPANY)}</TableCell>
              <TableCell>{resolveMessage(MessageCodes.PUNTO_EMISION_ESTABLISHMENT)}</TableCell>
              <TableCell>{resolveMessage(MessageCodes.PUNTO_EMISION_CODE)}</TableCell>
              <TableCell>{resolveMessage(MessageCodes.PUNTO_EMISION_NAME)}</TableCell>
              <TableCell>{resolveMessage(MessageCodes.COMMON_STATUS)}</TableCell>
              <TableCell align="right">{resolveMessage(MessageCodes.COMMON_ACTIONS)}</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {cargandoPuntos ? (
              <TableRow>
                <TableCell colSpan={7} align="center">
                  <CircularProgress size={26} />
                </TableCell>
              </TableRow>
            ) : puntosEmision.length === 0 ? (
              <TableRow>
                <TableCell colSpan={7} align="center">
                  {resolveMessage(MessageCodes.PUNTO_EMISION_EMPTY)}
                </TableCell>
              </TableRow>
            ) : (
              puntosEmision.map((puntoEmision) => {
                const establecimiento = establecimientosPorId.get(puntoEmision.idEstablecimiento);
                const empresa = establecimiento
                  ? empresasPorId.get(establecimiento.idEmpresa)
                  : undefined;
                return (
                  <TableRow hover key={puntoEmision.id}>
                    <TableCell>{puntoEmision.id}</TableCell>
                    <TableCell>
                      {empresa
                        ? `${empresa.ruc} - ${empresa.razonSocial}`
                        : (establecimiento?.idEmpresa ?? "—")}
                    </TableCell>
                    <TableCell>
                      {establecimiento
                        ? `${establecimiento.codigo} - ${establecimiento.nombre}`
                        : puntoEmision.idEstablecimiento}
                    </TableCell>
                    <TableCell>{puntoEmision.codigo}</TableCell>
                    <TableCell>{puntoEmision.nombre}</TableCell>
                    <TableCell>
                      <Chip
                        size="small"
                        label={puntoEmision.estadoRegistro}
                        color={puntoEmision.estadoRegistro === "ACTIVO" ? "success" : "default"}
                      />
                    </TableCell>
                    <TableCell align="right">
                      <Box className="puntos-emision-row-actions">
                        <Button
                          size="small"
                          onClick={() => void abrirEdicion(puntoEmision)}
                          startIcon={<EditOutlinedIcon />}
                        >
                          {resolveMessage(MessageCodes.COMMON_EDIT)}
                        </Button>
                        <Button
                          size="small"
                          color="error"
                          onClick={() => setConfirmacionEliminacion(puntoEmision)}
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
              puntoEmisionEdicion
                ? MessageCodes.PUNTO_EMISION_EDIT_TITLE
                : MessageCodes.PUNTO_EMISION_CREATE_TITLE,
            )}
          </DialogTitle>
          <DialogContent>
            <Typography className="puntos-emision-dialog-description" color="text.secondary">
              {resolveMessage(MessageCodes.PUNTO_EMISION_FORM_DESCRIPTION)}
            </Typography>
            <Box className="puntos-emision-form-grid">
              <FormControl fullWidth>
                <InputLabel id="punto-emision-form-empresa-label" shrink>
                  {resolveMessage(MessageCodes.ESTABLECIMIENTO_COMPANY)}
                </InputLabel>
                <Select
                  labelId="punto-emision-form-empresa-label"
                  label={resolveMessage(MessageCodes.ESTABLECIMIENTO_COMPANY)}
                  value={formulario.idEmpresa || ""}
                  displayEmpty
                  onChange={(event) => {
                    const empresaId = Number(event.target.value);
                    cambiarCampo("idEmpresa", empresaId);
                    cambiarCampo("idEstablecimiento", 0);
                    setEstablecimientoEdicionExtra(null);
                  }}
                  disabled
                >
                  <MenuItem value="">{resolveMessage(MessageCodes.COMMON_SELECT_OPTION)}</MenuItem>
                  {empresas.map((empresa) => (
                    <MenuItem key={empresa.id} value={empresa.id}>
                      {empresa.ruc} - {empresa.razonSocial}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
              <FormControl fullWidth>
                <InputLabel id="punto-emision-form-establecimiento-label" shrink>
                  {resolveMessage(MessageCodes.PUNTO_EMISION_ESTABLISHMENT)}
                </InputLabel>
                <Select
                  labelId="punto-emision-form-establecimiento-label"
                  label={resolveMessage(MessageCodes.PUNTO_EMISION_ESTABLISHMENT)}
                  value={formulario.idEstablecimiento || ""}
                  displayEmpty
                  onChange={(event) => {
                    const value = event.target.value;
                    const establecimientoId =
                      String(value ?? "") === "" ? null : Number(value);
                    const establecimiento = establecimientosFormulario.find(({ id }) =>
                      id === establecimientoId,
                    );
                    if (establecimiento) {
                      cambiarCampo("idEstablecimiento", establecimiento.id);
                      cambiarCampo("idEmpresa", establecimiento.idEmpresa);
                    } else if (establecimientoId === null) {
                      cambiarCampo("idEstablecimiento", 0);
                    }
                  }}
                >
                  <MenuItem value="">{resolveMessage(MessageCodes.COMMON_SELECT_OPTION)}</MenuItem>
                  {establecimientosFormulario
                    .filter(({ idEmpresa }) => idEmpresa === formulario.idEmpresa)
                    .map((establecimiento) => (
                      <MenuItem key={establecimiento.id} value={establecimiento.id}>
                        {establecimiento.codigo} - {establecimiento.nombre}
                      </MenuItem>
                    ))}
                </Select>
              </FormControl>
              <TextField
                label={resolveMessage(MessageCodes.PUNTO_EMISION_CODE)}
                value={formulario.codigo}
                onChange={(event) => cambiarCampo("codigo", event.target.value)}
                required
                inputProps={{ maxLength: 3 }}
                helperText={resolveMessage(MessageCodes.PUNTO_EMISION_CODE_REQUIRED)}
              />
              <TextField
                label={resolveMessage(MessageCodes.PUNTO_EMISION_NAME)}
                value={formulario.nombre}
                onChange={(event) => cambiarCampo("nombre", event.target.value)}
                required
                inputProps={{ maxLength: 300 }}
              />
            </Box>
          </DialogContent>
          <DialogActions className="puntos-emision-dialog-actions">
            <Button onClick={() => setDialogoAbierto(false)} disabled={guardando}>
              {resolveMessage(MessageCodes.COMMON_CANCEL)}
            </Button>
            <Button type="submit" variant="contained" disabled={guardando}>
              {guardando
                ? resolveMessage(MessageCodes.PUNTO_EMISION_SAVING)
                : resolveMessage(MessageCodes.COMMON_SAVE)}
            </Button>
          </DialogActions>
        </Box>
      </Dialog>

      <Dialog
        open={Boolean(confirmacionEliminacion)}
        onClose={() => !guardando && setConfirmacionEliminacion(null)}
        fullWidth
        maxWidth="xs"
      >
        <DialogTitle>{resolveMessage(MessageCodes.PUNTO_EMISION_DELETE_TITLE)}</DialogTitle>
        <DialogContent>
          <Typography>
            {resolveMessage(MessageCodes.PUNTO_EMISION_DELETE_CONFIRM_PREFIX)}{" "}
            <strong>{confirmacionEliminacion?.nombre}</strong>?
          </Typography>
          <Typography color="text.secondary" sx={{ mt: 1 }}>
            {resolveMessage(MessageCodes.PUNTO_EMISION_DELETE_CONFIRM_DESCRIPTION)}
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setConfirmacionEliminacion(null)} disabled={guardando}>
            {resolveMessage(MessageCodes.COMMON_CANCEL)}
          </Button>
          <Button
            color="error"
            variant="contained"
            onClick={() => void confirmarEliminacion()}
            disabled={guardando}
          >
            {guardando
              ? resolveMessage(MessageCodes.COMMON_DELETING)
              : resolveMessage(MessageCodes.PUNTO_EMISION_DELETE_CONFIRM)}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
