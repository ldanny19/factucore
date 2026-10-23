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
import AddOutlinedIcon from "@mui/icons-material/AddOutlined";
import BlockOutlinedIcon from "@mui/icons-material/BlockOutlined";
import DeleteOutlineOutlinedIcon from "@mui/icons-material/DeleteOutlineOutlined";
import EditOutlinedIcon from "@mui/icons-material/EditOutlined";
import ReplayOutlinedIcon from "@mui/icons-material/ReplayOutlined";
import RefreshOutlinedIcon from "@mui/icons-material/RefreshOutlined";
import { useEmpresaUseCases } from "../../empresas/presentation/hooks/EmpresaUseCasesContext";
import type { Empresa } from "../../empresas/domain/Empresa";
import { useEstablecimientoUseCase } from "../../establecimientos/presentation/hooks/EstablecimientoUseCaseContext";
import type { Establecimiento } from "../../establecimientos/domain/Establecimiento";
import { usePuntoEmisionUseCase } from "../../puntos-emision/presentation/hooks/PuntoEmisionUseCaseContext";
import type { PuntoEmision } from "../../puntos-emision/domain/PuntoEmision";
import { MessageCodes } from "../../../shared/messages/MessageCodes";
import { resolveMessage } from "../../../shared/messages/MessageResolver";
import type { Secuencial, SecuencialDatos } from "../domain/Secuencial";
import { useSecuencialUseCase } from "./hooks/SecuencialUseCaseContext";
import "../../../styles/app.css";

const MAXIMO_SECUENCIAL = 999_999_999;

interface SecuencialFormulario {
  codigoDocumento: string;
  ultimoSecuencial: string;
}

const FORMULARIO_VACIO: SecuencialFormulario = {
  codigoDocumento: "",
  ultimoSecuencial: "0",
};

function mensajeError(error: unknown): string {
  if (error instanceof Error && error.message.trim()) {
    return error.message;
  }
  return resolveMessage(MessageCodes.COMMON_OPERATION_FAILED);
}

export default function SecuencialesPage() {
  const secuencialUseCases = useSecuencialUseCase();
  const empresaUseCases = useEmpresaUseCases();
  const establecimientoUseCases = useEstablecimientoUseCase();
  const puntoEmisionUseCases = usePuntoEmisionUseCase();
  const [empresas, setEmpresas] = useState<Empresa[]>([]);
  const [establecimientos, setEstablecimientos] = useState<Establecimiento[]>([]);
  const [puntosEmision, setPuntosEmision] = useState<PuntoEmision[]>([]);
  const [secuenciales, setSecuenciales] = useState<Secuencial[]>([]);
  const [empresaFiltroId, setEmpresaFiltroId] = useState<number | "">("");
  const [establecimientoFiltroId, setEstablecimientoFiltroId] = useState<number | "">("");
  const [puntoEmisionFiltroId, setPuntoEmisionFiltroId] = useState<number | "">("");
  const [cargandoEmpresas, setCargandoEmpresas] = useState(true);
  const [cargandoEstablecimientos, setCargandoEstablecimientos] = useState(false);
  const [cargandoPuntos, setCargandoPuntos] = useState(false);
  const [cargandoSecuenciales, setCargandoSecuenciales] = useState(true);
  const [guardando, setGuardando] = useState(false);
  const [operacionFilaId, setOperacionFilaId] = useState<number | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [aviso, setAviso] = useState<string | null>(null);
  const [dialogoAbierto, setDialogoAbierto] = useState(false);
  const [secuencialEdicion, setSecuencialEdicion] = useState<Secuencial | null>(null);
  const [formulario, setFormulario] = useState<SecuencialFormulario>(FORMULARIO_VACIO);
  const [confirmacionEliminacion, setConfirmacionEliminacion] = useState<Secuencial | null>(null);

  const empresasPorId = useMemo(
    () => new Map(empresas.map((empresa) => [empresa.id, empresa])),
    [empresas],
  );
  const establecimientosPorId = useMemo(
    () => new Map(establecimientos.map((establecimiento) => [establecimiento.id, establecimiento])),
    [establecimientos],
  );
  const puntosEmisionPorId = useMemo(
    () => new Map(puntosEmision.map((puntoEmision) => [puntoEmision.id, puntoEmision])),
    [puntosEmision],
  );
  const secuencialesVisibles = useMemo(
    () =>
      secuenciales.filter(
        (secuencial) =>
          puntoEmisionFiltroId === "" || secuencial.idPuntoEmision === puntoEmisionFiltroId,
      ),
    [secuenciales, puntoEmisionFiltroId],
  );
  const empresaSeleccionada =
    empresaFiltroId === "" ? undefined : empresasPorId.get(empresaFiltroId);
  const establecimientoSeleccionado =
    establecimientoFiltroId === ""
      ? undefined
      : establecimientosPorId.get(establecimientoFiltroId);
  const puntoEmisionSeleccionado =
    puntoEmisionFiltroId === "" ? undefined : puntosEmisionPorId.get(puntoEmisionFiltroId);

  const cargarSecuenciales = useCallback(async () => {
    setCargandoSecuenciales(true);
    try {
      setSecuenciales(await secuencialUseCases.listar());
    } catch (exception) {
      setError(mensajeError(exception));
      setSecuenciales([]);
    } finally {
      setCargandoSecuenciales(false);
    }
  }, [secuencialUseCases]);

  useEffect(() => {
    let vigente = true;
    const cargar = async () => {
      setCargandoEmpresas(true);
      try {
        const resultado = await empresaUseCases.listar();
        if (!vigente) return;
        setEmpresas(resultado);
      } catch (exception) {
        if (vigente) setError(mensajeError(exception));
      } finally {
        if (vigente) setCargandoEmpresas(false);
      }
    };
    void cargar();
    return () => {
      vigente = false;
    };
  }, [empresaUseCases]);

  useEffect(() => {
    let vigente = true;
    setEstablecimientos([]);
    setEstablecimientoFiltroId("");
    setPuntosEmision([]);
    setPuntoEmisionFiltroId("");

    if (empresaFiltroId === "") {
      setCargandoEstablecimientos(false);
      return () => {
        vigente = false;
      };
    }

    const cargar = async () => {
      setCargandoEstablecimientos(true);
      try {
        const resultado = await establecimientoUseCases.listar(empresaFiltroId);
        if (!vigente) return;
        setEstablecimientos(resultado);
      } catch (exception) {
        if (vigente) setError(mensajeError(exception));
      } finally {
        if (vigente) setCargandoEstablecimientos(false);
      }
    };
    void cargar();
    return () => {
      vigente = false;
    };
  }, [empresaFiltroId, establecimientoUseCases]);

  useEffect(() => {
    let vigente = true;
    setPuntosEmision([]);
    setPuntoEmisionFiltroId("");

    if (establecimientoFiltroId === "") {
      setCargandoPuntos(false);
      return () => {
        vigente = false;
      };
    }

    const cargar = async () => {
      setCargandoPuntos(true);
      try {
        const resultado = await puntoEmisionUseCases.listar(establecimientoFiltroId);
        if (!vigente) return;
        setPuntosEmision(resultado);
      } catch (exception) {
        if (vigente) setError(mensajeError(exception));
      } finally {
        if (vigente) setCargandoPuntos(false);
      }
    };
    void cargar();
    return () => {
      vigente = false;
    };
  }, [establecimientoFiltroId, puntoEmisionUseCases]);

  useEffect(() => {
    void cargarSecuenciales();
  }, [cargarSecuenciales]);

  const abrirCrear = () => {
    setSecuencialEdicion(null);
    setFormulario(FORMULARIO_VACIO);
    setError(null);
    setAviso(null);
    setDialogoAbierto(true);
  };

  const abrirEdicion = (secuencial: Secuencial) => {
    setSecuencialEdicion(secuencial);
    setFormulario({
      codigoDocumento: secuencial.codigoDocumento,
      ultimoSecuencial: String(secuencial.ultimoSecuencial),
    });
    setError(null);
    setAviso(null);
    setDialogoAbierto(true);
  };

  const guardar = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError(null);
    setAviso(null);

    const puntoEmisionId = secuencialEdicion?.idPuntoEmision ?? puntoEmisionFiltroId;
    const ultimoSecuencial = Number(formulario.ultimoSecuencial);
    if (!empresaSeleccionada || !Number.isSafeInteger(empresaSeleccionada.id)) {
      setError(resolveMessage(MessageCodes.SECUENCIAL_COMPANY_REQUIRED));
      return;
    }
    if (
      !puntoEmisionSeleccionado ||
      puntoEmisionSeleccionado.id !== puntoEmisionId ||
      puntoEmisionSeleccionado.idEstablecimiento !== establecimientoFiltroId
    ) {
      setError(resolveMessage(MessageCodes.SECUENCIAL_POINT_REQUIRED));
      return;
    }
    if (!/^\d{2}$/.test(formulario.codigoDocumento)) {
      setError(resolveMessage(MessageCodes.SECUENCIAL_DOCUMENT_CODE_INVALID));
      return;
    }
    if (
      !/^\d+$/.test(formulario.ultimoSecuencial) ||
      !Number.isSafeInteger(ultimoSecuencial) ||
      ultimoSecuencial < 0 ||
      ultimoSecuencial > MAXIMO_SECUENCIAL
    ) {
      setError(resolveMessage(MessageCodes.SECUENCIAL_LAST_NUMBER_INVALID));
      return;
    }

    const datos: SecuencialDatos = {
      idEmpresa: empresaSeleccionada.id,
      idPuntoEmision: puntoEmisionId,
      codigoDocumento: formulario.codigoDocumento,
      ultimoSecuencial,
    };

    setGuardando(true);
    try {
      if (secuencialEdicion) {
        await secuencialUseCases.actualizar(secuencialEdicion.id, datos);
        setAviso(resolveMessage(MessageCodes.SECUENCIAL_UPDATED));
      } else {
        await secuencialUseCases.crear(datos);
        setAviso(resolveMessage(MessageCodes.SECUENCIAL_CREATED));
      }
      setDialogoAbierto(false);
      await cargarSecuenciales();
    } catch (exception) {
      setError(mensajeError(exception));
    } finally {
      setGuardando(false);
    }
  };

  const cambiarEstado = async (secuencial: Secuencial) => {
    setOperacionFilaId(secuencial.id);
    setError(null);
    setAviso(null);
    try {
      if (secuencial.estadoRegistro === "ACTIVO") {
        await secuencialUseCases.inactivar(secuencial.id);
        setAviso(resolveMessage(MessageCodes.SECUENCIAL_INACTIVATED));
      } else {
        await secuencialUseCases.reactivar(secuencial.id);
        setAviso(resolveMessage(MessageCodes.SECUENCIAL_REACTIVATED));
      }
      await cargarSecuenciales();
    } catch (exception) {
      setError(mensajeError(exception));
    } finally {
      setOperacionFilaId(null);
    }
  };

  const confirmarEliminacion = async () => {
    if (!confirmacionEliminacion) return;
    setGuardando(true);
    setError(null);
    setAviso(null);
    try {
      await secuencialUseCases.eliminar(confirmacionEliminacion.id);
      setAviso(
        resolveMessage(MessageCodes.SECUENCIAL_DELETED, {
          code: confirmacionEliminacion.codigoDocumento,
        }),
      );
      setConfirmacionEliminacion(null);
      await cargarSecuenciales();
    } catch (exception) {
      setError(mensajeError(exception));
    } finally {
      setGuardando(false);
    }
  };

  const cambiarCampo = (campo: keyof SecuencialFormulario, valor: string) => {
    setFormulario((actual) => ({ ...actual, [campo]: valor }));
  };

  return (
    <Box className="secuenciales-page">
      <Box className="secuenciales-page-heading">
        <Box>
          <Typography className="page-title" component="h1" variant="h4">
            {resolveMessage(MessageCodes.SECUENCIAL_PAGE_TITLE)}
          </Typography>
          <Typography color="text.secondary">
            {resolveMessage(MessageCodes.SECUENCIAL_PAGE_DESCRIPTION)}
          </Typography>
        </Box>
        <Button
          variant="contained"
          startIcon={<AddOutlinedIcon />}
          onClick={abrirCrear}
          disabled={
            cargandoEmpresas ||
            cargandoEstablecimientos ||
            cargandoPuntos ||
            !puntoEmisionSeleccionado
          }
        >
          {resolveMessage(MessageCodes.SECUENCIAL_NEW)}
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

      <Paper component="section" variant="outlined" className="secuenciales-filter-panel">
        <Typography variant="h6" component="h2">
          {resolveMessage(MessageCodes.SECUENCIAL_FILTER_TITLE)}
        </Typography>
        <Box className="secuenciales-filter-form">
          <FormControl size="small" sx={{ minWidth: 240 }}>
            <InputLabel id="secuencial-empresa-label" shrink>
              {resolveMessage(MessageCodes.ESTABLECIMIENTO_COMPANY)}
            </InputLabel>
            <Select
              labelId="secuencial-empresa-label"
              label={resolveMessage(MessageCodes.ESTABLECIMIENTO_COMPANY)}
              value={empresaFiltroId}
              displayEmpty
              onChange={(event) => {
                setError(null);
                setAviso(null);
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
          <FormControl size="small" sx={{ minWidth: 240 }}>
            <InputLabel id="secuencial-establecimiento-label" shrink>
              {resolveMessage(MessageCodes.PUNTO_EMISION_ESTABLISHMENT)}
            </InputLabel>
            <Select
              labelId="secuencial-establecimiento-label"
              label={resolveMessage(MessageCodes.PUNTO_EMISION_ESTABLISHMENT)}
              value={establecimientoFiltroId}
              displayEmpty
              onChange={(event) => {
                setError(null);
                setAviso(null);
                const value = event.target.value;
                setEstablecimientoFiltroId(String(value ?? "") === "" ? "" : Number(value));
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
          <FormControl size="small" sx={{ minWidth: 240 }}>
            <InputLabel id="secuencial-punto-emision-label" shrink>
              {resolveMessage(MessageCodes.SECUENCIAL_POINT)}
            </InputLabel>
            <Select
              labelId="secuencial-punto-emision-label"
              label={resolveMessage(MessageCodes.SECUENCIAL_POINT)}
              value={puntoEmisionFiltroId}
              displayEmpty
              onChange={(event) => {
                setError(null);
                setAviso(null);
                const value = event.target.value;
                setPuntoEmisionFiltroId(String(value ?? "") === "" ? "" : Number(value));
              }}
              disabled={cargandoPuntos || puntosEmision.length === 0}
            >
              <MenuItem value="">{resolveMessage(MessageCodes.COMMON_SELECT_OPTION)}</MenuItem>
              {puntosEmision.map((puntoEmision) => (
                <MenuItem key={puntoEmision.id} value={puntoEmision.id}>
                  {puntoEmision.codigo} - {puntoEmision.nombre}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
        </Box>
      </Paper>

      <Box className="secuenciales-list-heading">
        <Box>
          <Typography variant="h6" component="h2">
            {resolveMessage(MessageCodes.SECUENCIAL_LIST_TITLE)}
          </Typography>
          <Typography variant="body2" color="text.secondary">
            {secuencialesVisibles.length} {resolveMessage(MessageCodes.SECUENCIAL_RECORDS)}
          </Typography>
        </Box>
        <Button
          variant="text"
          startIcon={<RefreshOutlinedIcon />}
          onClick={() => void cargarSecuenciales()}
          disabled={cargandoSecuenciales}
        >
          {resolveMessage(MessageCodes.COMMON_REFRESH)}
        </Button>
      </Box>

      <TableContainer component={Paper} variant="outlined" className="secuenciales-table-panel">
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>{resolveMessage(MessageCodes.SECUENCIAL_ID_LABEL)}</TableCell>
              <TableCell>{resolveMessage(MessageCodes.ESTABLECIMIENTO_COMPANY)}</TableCell>
              <TableCell>{resolveMessage(MessageCodes.PUNTO_EMISION_ESTABLISHMENT)}</TableCell>
              <TableCell>{resolveMessage(MessageCodes.SECUENCIAL_POINT)}</TableCell>
              <TableCell>{resolveMessage(MessageCodes.SECUENCIAL_DOCUMENT_CODE)}</TableCell>
              <TableCell>{resolveMessage(MessageCodes.SECUENCIAL_LAST_NUMBER)}</TableCell>
              <TableCell>{resolveMessage(MessageCodes.COMMON_STATUS)}</TableCell>
              <TableCell align="right">{resolveMessage(MessageCodes.COMMON_ACTIONS)}</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {cargandoSecuenciales ? (
              <TableRow>
                <TableCell colSpan={8} align="center">
                  <CircularProgress size={26} />
                </TableCell>
              </TableRow>
            ) : secuencialesVisibles.length === 0 ? (
              <TableRow>
                <TableCell colSpan={8} align="center">
                  {resolveMessage(MessageCodes.SECUENCIAL_EMPTY)}
                </TableCell>
              </TableRow>
            ) : (
              secuencialesVisibles.map((secuencial) => (
                <TableRow hover key={secuencial.id}>
                  <TableCell>{secuencial.id}</TableCell>
                  <TableCell>
                    {empresaSeleccionada
                      ? `${empresaSeleccionada.ruc} - ${empresaSeleccionada.razonSocial}`
                      : "—"}
                  </TableCell>
                  <TableCell>
                    {establecimientoSeleccionado
                      ? `${establecimientoSeleccionado.codigo} - ${establecimientoSeleccionado.nombre}`
                      : secuencial.idPuntoEmision}
                  </TableCell>
                  <TableCell>
                    {puntoEmisionSeleccionado
                      ? `${puntoEmisionSeleccionado.codigo} - ${puntoEmisionSeleccionado.nombre}`
                      : secuencial.idPuntoEmision}
                  </TableCell>
                  <TableCell>{secuencial.codigoDocumento}</TableCell>
                  <TableCell>{secuencial.ultimoSecuencial}</TableCell>
                  <TableCell>
                    <Chip
                      size="small"
                      label={secuencial.estadoRegistro}
                      color={secuencial.estadoRegistro === "ACTIVO" ? "success" : "default"}
                    />
                  </TableCell>
                  <TableCell align="right">
                    <Box className="secuenciales-row-actions">
                      <Button
                        size="small"
                        onClick={() => abrirEdicion(secuencial)}
                        startIcon={<EditOutlinedIcon />}
                      >
                        {resolveMessage(MessageCodes.COMMON_EDIT)}
                      </Button>
                      <Button
                        size="small"
                        onClick={() => void cambiarEstado(secuencial)}
                        disabled={operacionFilaId !== null || guardando}
                        startIcon={
                          secuencial.estadoRegistro === "ACTIVO" ? (
                            <BlockOutlinedIcon />
                          ) : (
                            <ReplayOutlinedIcon />
                          )
                        }
                      >
                        {resolveMessage(
                          secuencial.estadoRegistro === "ACTIVO"
                            ? MessageCodes.SECUENCIAL_INACTIVATE
                            : MessageCodes.SECUENCIAL_REACTIVATE,
                        )}
                      </Button>
                      <Button
                        size="small"
                        color="error"
                        onClick={() => setConfirmacionEliminacion(secuencial)}
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

      <Dialog
        open={dialogoAbierto}
        onClose={() => !guardando && setDialogoAbierto(false)}
        fullWidth
        maxWidth="sm"
      >
        <Box component="form" onSubmit={guardar}>
          <DialogTitle>
            {resolveMessage(
              secuencialEdicion
                ? MessageCodes.SECUENCIAL_EDIT_TITLE
                : MessageCodes.SECUENCIAL_CREATE_TITLE,
            )}
          </DialogTitle>
          <DialogContent>
            <Typography className="secuenciales-dialog-description" color="text.secondary">
              {resolveMessage(MessageCodes.SECUENCIAL_FORM_DESCRIPTION)}
            </Typography>
            <Box className="secuenciales-form-grid">
              <TextField
                label={resolveMessage(MessageCodes.ESTABLECIMIENTO_COMPANY)}
                value={
                  empresaSeleccionada
                    ? `${empresaSeleccionada.ruc} - ${empresaSeleccionada.razonSocial}`
                    : ""
                }
                disabled
                fullWidth
              />
              <TextField
                label={resolveMessage(MessageCodes.PUNTO_EMISION_ESTABLISHMENT)}
                value={
                  establecimientoSeleccionado
                    ? `${establecimientoSeleccionado.codigo} - ${establecimientoSeleccionado.nombre}`
                    : ""
                }
                disabled
                fullWidth
              />
              <TextField
                label={resolveMessage(MessageCodes.SECUENCIAL_POINT)}
                value={
                  puntoEmisionSeleccionado
                    ? `${puntoEmisionSeleccionado.codigo} - ${puntoEmisionSeleccionado.nombre}`
                    : ""
                }
                disabled
                fullWidth
              />
              <TextField
                label={resolveMessage(MessageCodes.SECUENCIAL_DOCUMENT_CODE)}
                value={formulario.codigoDocumento}
                onChange={(event) => cambiarCampo("codigoDocumento", event.target.value)}
                required
                inputProps={{ maxLength: 2, inputMode: "numeric", pattern: "[0-9]{2}" }}
                helperText={resolveMessage(MessageCodes.SECUENCIAL_DOCUMENT_CODE_HELP)}
              />
              <TextField
                label={resolveMessage(MessageCodes.SECUENCIAL_LAST_NUMBER)}
                type="number"
                value={formulario.ultimoSecuencial}
                onChange={(event) => cambiarCampo("ultimoSecuencial", event.target.value)}
                required
                inputProps={{ min: 0, max: MAXIMO_SECUENCIAL, step: 1 }}
                helperText={resolveMessage(MessageCodes.SECUENCIAL_LAST_NUMBER_HELP)}
              />
            </Box>
          </DialogContent>
          <DialogActions className="secuenciales-dialog-actions">
            <Button onClick={() => setDialogoAbierto(false)} disabled={guardando}>
              {resolveMessage(MessageCodes.COMMON_CANCEL)}
            </Button>
            <Button type="submit" variant="contained" disabled={guardando}>
              {guardando
                ? resolveMessage(MessageCodes.SECUENCIAL_SAVING)
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
        <DialogTitle>{resolveMessage(MessageCodes.SECUENCIAL_DELETE_TITLE)}</DialogTitle>
        <DialogContent>
          <Typography>
            {resolveMessage(MessageCodes.SECUENCIAL_DELETE_CONFIRM_PREFIX)}{" "}
            <strong>{confirmacionEliminacion?.codigoDocumento}</strong>?
          </Typography>
          <Typography color="text.secondary" sx={{ mt: 1 }}>
            {resolveMessage(MessageCodes.SECUENCIAL_DELETE_CONFIRM_DESCRIPTION)}
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
              : resolveMessage(MessageCodes.SECUENCIAL_DELETE_CONFIRM)}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
