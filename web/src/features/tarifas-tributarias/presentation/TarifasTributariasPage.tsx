import { useCallback, useMemo, useState, type FormEvent } from "react";
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
import DeleteOutlineOutlinedIcon from "@mui/icons-material/DeleteOutlined";
import EditOutlinedIcon from "@mui/icons-material/EditOutlined";
import ReplayOutlinedIcon from "@mui/icons-material/ReplayOutlined";
import RefreshOutlinedIcon from "@mui/icons-material/RefreshOutlined";
import SearchOutlinedIcon from "@mui/icons-material/SearchOutlined";
import { MessageCodes } from "../../../shared/messages/MessageCodes";
import { resolveMessage } from "../../../shared/messages/MessageResolver";
import type {
  TarifaTributaria,
  TarifaTributariaDatos,
  TarifaTributariaFiltro,
} from "../domain/TarifaTributaria";
import { useTarifaTributariaUseCase } from "./hooks/TarifaTributariaUseCaseContext";
import "../../../styles/app.css";

const MAXIMA_TARIFA = 9999.9999;

function fechaLocal(): string {
  const now = new Date();
  const pad = (part: number) => String(part).padStart(2, "0");
  return `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())}`;
}

interface TarifaFormulario {
  idCatalogoItem: string;
  codigoImpuesto: string;
  codigoPorcentaje: string;
  tarifa: string;
  fechaVigenciaDesde: string;
  fechaVigenciaHasta: string;
  observacion: string;
}

const FORMULARIO_VACIO: TarifaFormulario = {
  idCatalogoItem: "",
  codigoImpuesto: "",
  codigoPorcentaje: "",
  tarifa: "",
  fechaVigenciaDesde: fechaLocal(),
  fechaVigenciaHasta: "",
  observacion: "",
};

function mensajeError(error: unknown): string {
  if (error instanceof Error && error.message.trim()) {
    return error.message;
  }
  return resolveMessage(MessageCodes.COMMON_OPERATION_FAILED);
}

export default function TarifasTributariasPage() {
  const useCases = useTarifaTributariaUseCase();
  const [codigoImpuesto, setCodigoImpuesto] = useState("");
  const [codigoPorcentaje, setCodigoPorcentaje] = useState("");
  const [fechaConsulta, setFechaConsulta] = useState(fechaLocal());
  const [filtroAplicado, setFiltroAplicado] = useState<TarifaTributariaFiltro | null>(null);
  const [tarifas, setTarifas] = useState<TarifaTributaria[]>([]);
  const [tarifaVigente, setTarifaVigente] = useState<TarifaTributaria | null>(null);
  const [cargando, setCargando] = useState(false);
  const [consultandoVigente, setConsultandoVigente] = useState(false);
  const [guardando, setGuardando] = useState(false);
  const [operacionFilaId, setOperacionFilaId] = useState<number | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [aviso, setAviso] = useState<string | null>(null);
  const [dialogoAbierto, setDialogoAbierto] = useState(false);
  const [edicion, setEdicion] = useState<TarifaTributaria | null>(null);
  const [formulario, setFormulario] = useState<TarifaFormulario>(FORMULARIO_VACIO);
  const [confirmacionEliminacion, setConfirmacionEliminacion] =
    useState<TarifaTributaria | null>(null);

  const tarifaVigenteResumen = useMemo(() => {
    if (!tarifaVigente) return null;
    return `${tarifaVigente.codigoImpuesto}/${tarifaVigente.codigoPorcentaje}: ${tarifaVigente.tarifa}%`;
  }, [tarifaVigente]);

  const cargar = useCallback(
    async (filtro = filtroAplicado) => {
      if (!filtro) {
        setTarifas([]);
        return;
      }
      setCargando(true);
      setError(null);
      try {
        setTarifas(await useCases.listar(filtro));
      } catch (exception) {
        setError(mensajeError(exception));
        setTarifas([]);
      } finally {
        setCargando(false);
      }
    },
    [filtroAplicado, useCases],
  );

  const buscar = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError(null);
    setAviso(null);
    setTarifaVigente(null);
    const impuesto = codigoImpuesto.trim();
    const porcentaje = codigoPorcentaje.trim();
    if (!/^\d{1,2}$/.test(impuesto) || !/^\d{1,4}$/.test(porcentaje)) {
      setError(resolveMessage(MessageCodes.TARIFA_CODES_INVALID));
      return;
    }
    const filtro = { codigoImpuesto: impuesto, codigoPorcentaje: porcentaje };
    setFiltroAplicado(filtro);
    await cargar(filtro);
  };

  const consultarVigente = async () => {
    setError(null);
    setAviso(null);
    const impuesto = codigoImpuesto.trim();
    const porcentaje = codigoPorcentaje.trim();
    if (!/^\d{1,2}$/.test(impuesto) || !/^\d{1,4}$/.test(porcentaje) || !fechaConsulta) {
      setError(resolveMessage(MessageCodes.TARIFA_VIGENTE_FILTER_INVALID));
      return;
    }
    setConsultandoVigente(true);
    try {
      const resultado = await useCases.obtenerVigente({
        codigoImpuesto: impuesto,
        codigoPorcentaje: porcentaje,
        fecha: fechaConsulta,
      });
      setTarifaVigente(resultado);
      setAviso(
        resolveMessage(
          resultado
            ? MessageCodes.TARIFA_VIGENTE_FOUND
            : MessageCodes.TARIFA_VIGENTE_NOT_FOUND,
          resultado ? { rate: resultado.tarifa, date: fechaConsulta } : { date: fechaConsulta },
        ),
      );
    } catch (exception) {
      setError(mensajeError(exception));
      setTarifaVigente(null);
    } finally {
      setConsultandoVigente(false);
    }
  };

  const abrirCrear = () => {
    setEdicion(null);
    setFormulario({
      ...FORMULARIO_VACIO,
      codigoImpuesto: filtroAplicado?.codigoImpuesto ?? codigoImpuesto.trim(),
      codigoPorcentaje: filtroAplicado?.codigoPorcentaje ?? codigoPorcentaje.trim(),
      fechaVigenciaDesde: fechaLocal(),
    });
    setError(null);
    setAviso(null);
    setDialogoAbierto(true);
  };

  const abrirEdicion = (tarifa: TarifaTributaria) => {
    setEdicion(tarifa);
    setFormulario({
      idCatalogoItem: String(tarifa.idCatalogoItem),
      codigoImpuesto: tarifa.codigoImpuesto,
      codigoPorcentaje: tarifa.codigoPorcentaje,
      tarifa: String(tarifa.tarifa),
      fechaVigenciaDesde: tarifa.fechaVigenciaDesde,
      fechaVigenciaHasta: tarifa.fechaVigenciaHasta ?? "",
      observacion: tarifa.observacion ?? "",
    });
    setError(null);
    setAviso(null);
    setDialogoAbierto(true);
  };

  const guardar = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError(null);
    setAviso(null);
    const idCatalogoItem = Number(formulario.idCatalogoItem);
    const tarifa = Number(formulario.tarifa);
    const impuesto = formulario.codigoImpuesto.trim();
    const porcentaje = formulario.codigoPorcentaje.trim();
    if (!Number.isSafeInteger(idCatalogoItem) || idCatalogoItem <= 0) {
      setError(resolveMessage(MessageCodes.TARIFA_CATALOG_ITEM_INVALID));
      return;
    }
    if (!/^\d{1,2}$/.test(impuesto) || !/^\d{1,4}$/.test(porcentaje)) {
      setError(resolveMessage(MessageCodes.TARIFA_CODES_INVALID));
      return;
    }
    if (
      !formulario.tarifa.trim() ||
      !Number.isFinite(tarifa) ||
      tarifa < 0 ||
      tarifa > MAXIMA_TARIFA ||
      !/^\d+(\.\d{1,4})?$/.test(formulario.tarifa)
    ) {
      setError(resolveMessage(MessageCodes.TARIFA_RATE_INVALID));
      return;
    }
    if (
      !formulario.fechaVigenciaDesde ||
      (formulario.fechaVigenciaHasta &&
        formulario.fechaVigenciaHasta < formulario.fechaVigenciaDesde)
    ) {
      setError(resolveMessage(MessageCodes.TARIFA_DATE_RANGE_INVALID));
      return;
    }
    if (formulario.observacion.length > 500) {
      setError(resolveMessage(MessageCodes.TARIFA_OBSERVATION_INVALID));
      return;
    }

    const datos: TarifaTributariaDatos = {
      idCatalogoItem,
      codigoImpuesto: impuesto,
      codigoPorcentaje: porcentaje,
      tarifa,
      fechaVigenciaDesde: formulario.fechaVigenciaDesde,
      fechaVigenciaHasta: formulario.fechaVigenciaHasta || null,
      observacion: formulario.observacion.trim(),
    };

    setGuardando(true);
    try {
      if (edicion) {
        await useCases.actualizar(edicion.id, datos);
        setAviso(resolveMessage(MessageCodes.TARIFA_UPDATED));
      } else {
        await useCases.crear(datos);
        setAviso(resolveMessage(MessageCodes.TARIFA_CREATED));
      }
      setFiltroAplicado({
        codigoImpuesto: impuesto,
        codigoPorcentaje: porcentaje,
      });
      setCodigoImpuesto(impuesto);
      setCodigoPorcentaje(porcentaje);
      setDialogoAbierto(false);
      setTarifaVigente(null);
      await cargar({ codigoImpuesto: impuesto, codigoPorcentaje: porcentaje });
    } catch (exception) {
      setError(mensajeError(exception));
    } finally {
      setGuardando(false);
    }
  };

  const cambiarEstado = async (tarifa: TarifaTributaria) => {
    setOperacionFilaId(tarifa.id);
    setError(null);
    setAviso(null);
    try {
      if (tarifa.estadoRegistro === "ACTIVO") {
        await useCases.inactivar(tarifa.id);
        setAviso(resolveMessage(MessageCodes.TARIFA_INACTIVATED));
      } else {
        await useCases.reactivar(tarifa.id);
        setAviso(resolveMessage(MessageCodes.TARIFA_REACTIVATED));
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
        resolveMessage(MessageCodes.TARIFA_DELETED, {
          code: `${confirmacionEliminacion.codigoImpuesto}/${confirmacionEliminacion.codigoPorcentaje}`,
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

  const cambiarCampo = (campo: keyof TarifaFormulario, valor: string) => {
    setFormulario((actual) => ({ ...actual, [campo]: valor }));
  };

  return (
    <Box className="tarifas-tributarias-page">
      <Box className="tarifas-tributarias-heading">
        <Box>
          <Typography className="page-title" component="h1" variant="h4">
            {resolveMessage(MessageCodes.TARIFA_PAGE_TITLE)}
          </Typography>
          <Typography color="text.secondary">
            {resolveMessage(MessageCodes.TARIFA_PAGE_DESCRIPTION)}
          </Typography>
        </Box>
        <Button
          variant="contained"
          startIcon={<AddOutlinedIcon />}
          onClick={abrirCrear}
          disabled={!codigoImpuesto.trim() || !codigoPorcentaje.trim()}
        >
          {resolveMessage(MessageCodes.TARIFA_NEW)}
        </Button>
      </Box>

      {error && <Alert severity="error" onClose={() => setError(null)}>{error}</Alert>}
      {aviso && <Alert severity="success" onClose={() => setAviso(null)}>{aviso}</Alert>}

      <Paper variant="outlined" className="tarifas-tributarias-search-panel">
        <Typography variant="h6" component="h2">
          {resolveMessage(MessageCodes.TARIFA_SEARCH_TITLE)}
        </Typography>
        <Box component="form" className="tarifas-tributarias-search-form" onSubmit={buscar}>
          <TextField
            label={resolveMessage(MessageCodes.TARIFA_TAX_CODE)}
            value={codigoImpuesto}
            onChange={(event) => setCodigoImpuesto(event.target.value)}
            required
            inputProps={{ maxLength: 2, inputMode: "numeric" }}
          />
          <TextField
            label={resolveMessage(MessageCodes.TARIFA_PERCENT_CODE)}
            value={codigoPorcentaje}
            onChange={(event) => setCodigoPorcentaje(event.target.value)}
            required
            inputProps={{ maxLength: 4, inputMode: "numeric" }}
          />
          <Button type="submit" variant="outlined" startIcon={<SearchOutlinedIcon />} disabled={cargando}>
            {resolveMessage(MessageCodes.COMMON_SEARCH)}
          </Button>
          <Button
            type="button"
            variant="outlined"
            startIcon={<RefreshOutlinedIcon />}
            onClick={() => void cargar()}
            disabled={!filtroAplicado || cargando}
          >
            {resolveMessage(MessageCodes.COMMON_REFRESH)}
          </Button>
        </Box>
        <Box className="tarifas-tributarias-current-form">
          <TextField
            label={resolveMessage(MessageCodes.TARIFA_QUERY_DATE)}
            type="date"
            value={fechaConsulta}
            onChange={(event) => setFechaConsulta(event.target.value)}
            InputLabelProps={{ shrink: true }}
          />
          <Button
            variant="text"
            onClick={() => void consultarVigente()}
            disabled={consultandoVigente}
          >
            {consultandoVigente
              ? resolveMessage(MessageCodes.TARIFA_QUERYING_CURRENT)
              : resolveMessage(MessageCodes.TARIFA_QUERY_CURRENT)}
          </Button>
        </Box>
        {tarifaVigenteResumen && (
          <Alert severity="info">
            {resolveMessage(MessageCodes.TARIFA_CURRENT_SUMMARY, {
              result: tarifaVigenteResumen,
              date: fechaConsulta,
            })}
          </Alert>
        )}
      </Paper>

      <Box className="tarifas-tributarias-list-heading">
        <Box>
          <Typography variant="h6" component="h2">
            {resolveMessage(MessageCodes.TARIFA_LIST_TITLE)}
          </Typography>
          <Typography variant="body2" color="text.secondary">
            {tarifas.length} {resolveMessage(MessageCodes.TARIFA_RECORDS)}
          </Typography>
        </Box>
      </Box>

      <TableContainer component={Paper} variant="outlined" className="tarifas-tributarias-table">
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>{resolveMessage(MessageCodes.TARIFA_TAX_CODE)}</TableCell>
              <TableCell>{resolveMessage(MessageCodes.TARIFA_PERCENT_CODE)}</TableCell>
              <TableCell>{resolveMessage(MessageCodes.TARIFA_CATALOG_ITEM_ID)}</TableCell>
              <TableCell>{resolveMessage(MessageCodes.TARIFA_RATE)}</TableCell>
              <TableCell>{resolveMessage(MessageCodes.TARIFA_VALID_FROM)}</TableCell>
              <TableCell>{resolveMessage(MessageCodes.TARIFA_VALID_UNTIL)}</TableCell>
              <TableCell>{resolveMessage(MessageCodes.COMMON_STATUS)}</TableCell>
              <TableCell align="right">{resolveMessage(MessageCodes.COMMON_ACTIONS)}</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {cargando ? (
              <TableRow><TableCell colSpan={8} align="center"><CircularProgress size={26} /></TableCell></TableRow>
            ) : !filtroAplicado ? (
              <TableRow><TableCell colSpan={8} align="center">{resolveMessage(MessageCodes.TARIFA_SEARCH_PROMPT)}</TableCell></TableRow>
            ) : tarifas.length === 0 ? (
              <TableRow><TableCell colSpan={8} align="center">{resolveMessage(MessageCodes.TARIFA_EMPTY)}</TableCell></TableRow>
            ) : (
              tarifas.map((tarifa) => (
                <TableRow hover key={tarifa.id}>
                  <TableCell>{tarifa.codigoImpuesto}</TableCell>
                  <TableCell>{tarifa.codigoPorcentaje}</TableCell>
                  <TableCell>{tarifa.idCatalogoItem}</TableCell>
                  <TableCell>{tarifa.tarifa}%</TableCell>
                  <TableCell>{tarifa.fechaVigenciaDesde}</TableCell>
                  <TableCell>{tarifa.fechaVigenciaHasta ?? "—"}</TableCell>
                  <TableCell>
                    <Chip
                      size="small"
                      label={tarifa.estadoRegistro}
                      color={tarifa.estadoRegistro === "ACTIVO" ? "success" : "default"}
                    />
                  </TableCell>
                  <TableCell align="right">
                    <Box className="tarifas-tributarias-row-actions">
                      <Button size="small" onClick={() => abrirEdicion(tarifa)} startIcon={<EditOutlinedIcon />}>
                        {resolveMessage(MessageCodes.COMMON_EDIT)}
                      </Button>
                      <Button
                        size="small"
                        onClick={() => void cambiarEstado(tarifa)}
                        disabled={operacionFilaId !== null || guardando}
                        startIcon={tarifa.estadoRegistro === "ACTIVO" ? <BlockOutlinedIcon /> : <ReplayOutlinedIcon />}
                      >
                        {resolveMessage(tarifa.estadoRegistro === "ACTIVO" ? MessageCodes.TARIFA_INACTIVATE : MessageCodes.TARIFA_REACTIVATE)}
                      </Button>
                      <Button
                        size="small"
                        color="error"
                        onClick={() => setConfirmacionEliminacion(tarifa)}
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
            {resolveMessage(edicion ? MessageCodes.TARIFA_EDIT_TITLE : MessageCodes.TARIFA_CREATE_TITLE)}
          </DialogTitle>
          <DialogContent>
            <Typography className="tarifas-tributarias-dialog-description" color="text.secondary">
              {resolveMessage(MessageCodes.TARIFA_FORM_DESCRIPTION)}
            </Typography>
            <Box className="tarifas-tributarias-form-grid">
              <TextField
                label={resolveMessage(MessageCodes.TARIFA_CATALOG_ITEM_ID)}
                type="number"
                value={formulario.idCatalogoItem}
                onChange={(event) => cambiarCampo("idCatalogoItem", event.target.value)}
                required
                inputProps={{ min: 1, step: 1 }}
                helperText={resolveMessage(MessageCodes.TARIFA_CATALOG_ITEM_HELP)}
              />
              <TextField
                label={resolveMessage(MessageCodes.TARIFA_TAX_CODE)}
                value={formulario.codigoImpuesto}
                onChange={(event) => cambiarCampo("codigoImpuesto", event.target.value)}
                required
                inputProps={{ maxLength: 2, inputMode: "numeric" }}
              />
              <TextField
                label={resolveMessage(MessageCodes.TARIFA_PERCENT_CODE)}
                value={formulario.codigoPorcentaje}
                onChange={(event) => cambiarCampo("codigoPorcentaje", event.target.value)}
                required
                inputProps={{ maxLength: 4, inputMode: "numeric" }}
              />
              <TextField
                label={resolveMessage(MessageCodes.TARIFA_RATE)}
                type="number"
                value={formulario.tarifa}
                onChange={(event) => cambiarCampo("tarifa", event.target.value)}
                required
                inputProps={{ min: 0, max: MAXIMA_TARIFA, step: 0.0001 }}
              />
              <TextField
                label={resolveMessage(MessageCodes.TARIFA_VALID_FROM)}
                type="date"
                value={formulario.fechaVigenciaDesde}
                onChange={(event) => cambiarCampo("fechaVigenciaDesde", event.target.value)}
                required
                InputLabelProps={{ shrink: true }}
              />
              <TextField
                label={resolveMessage(MessageCodes.TARIFA_VALID_UNTIL)}
                type="date"
                value={formulario.fechaVigenciaHasta}
                onChange={(event) => cambiarCampo("fechaVigenciaHasta", event.target.value)}
                InputLabelProps={{ shrink: true }}
              />
              <TextField
                label={resolveMessage(MessageCodes.TARIFA_OBSERVATION)}
                value={formulario.observacion}
                onChange={(event) => cambiarCampo("observacion", event.target.value)}
                inputProps={{ maxLength: 500 }}
                multiline
                minRows={2}
                sx={{ gridColumn: "1 / -1" }}
              />
            </Box>
          </DialogContent>
          <DialogActions className="tarifas-tributarias-dialog-actions">
            <Button onClick={() => setDialogoAbierto(false)} disabled={guardando}>
              {resolveMessage(MessageCodes.COMMON_CANCEL)}
            </Button>
            <Button type="submit" variant="contained" disabled={guardando}>
              {guardando ? resolveMessage(MessageCodes.TARIFA_SAVING) : resolveMessage(MessageCodes.COMMON_SAVE)}
            </Button>
          </DialogActions>
        </Box>
      </Dialog>

      <Dialog open={Boolean(confirmacionEliminacion)} onClose={() => !guardando && setConfirmacionEliminacion(null)} fullWidth maxWidth="xs">
        <DialogTitle>{resolveMessage(MessageCodes.TARIFA_DELETE_TITLE)}</DialogTitle>
        <DialogContent>
          <Typography>
            {resolveMessage(MessageCodes.TARIFA_DELETE_CONFIRM_PREFIX)}{" "}
            <strong>
              {confirmacionEliminacion?.codigoImpuesto}/{confirmacionEliminacion?.codigoPorcentaje}
            </strong>?
          </Typography>
          <Typography color="text.secondary" sx={{ mt: 1 }}>
            {resolveMessage(MessageCodes.TARIFA_DELETE_CONFIRM_DESCRIPTION)}
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setConfirmacionEliminacion(null)} disabled={guardando}>
            {resolveMessage(MessageCodes.COMMON_CANCEL)}
          </Button>
          <Button color="error" variant="contained" onClick={() => void eliminar()} disabled={guardando}>
            {guardando ? resolveMessage(MessageCodes.COMMON_DELETING) : resolveMessage(MessageCodes.TARIFA_DELETE_CONFIRM)}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
