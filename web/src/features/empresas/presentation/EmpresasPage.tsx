import { useCallback, useEffect, useState, type FormEvent } from "react";
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
import { useEmpresaUseCases } from "./hooks/EmpresaUseCasesContext";
import { MessageCodes } from "../../../shared/messages/MessageCodes";
import { resolveMessage } from "../../../shared/messages/MessageResolver";
import type { Empresa, EmpresaDatos } from "../domain/Empresa";
import "../../../styles/app.css";

interface EmpresaFormulario extends Omit<EmpresaDatos, "obligadoContabilidad"> {
  obligadoContabilidad: EmpresaDatos["obligadoContabilidad"] | "";
}

const RIMPE_NO_APLICA = "__NO_APLICA__";

const FORMULARIO_VACIO: EmpresaFormulario = {
  ruc: "",
  razonSocial: "",
  nombreComercial: "",
  direccionMatriz: "",
  obligadoContabilidad: "",
  contribuyenteRimpe: null,
};

function mensajeError(error: unknown): string {
  if (typeof error === "object" && error !== null && "response" in error) {
    const response = (error as { response?: { data?: { mensaje?: string; message?: string; codigo?: string } } }).response;
    return response?.data?.mensaje || response?.data?.message || response?.data?.codigo || resolveMessage(MessageCodes.COMMON_OPERATION_FAILED);
  }
  return error instanceof Error ? error.message : "No fue posible completar la operación.";
}

function fechaLocalActual(): string {
  const now = new Date();
  const pad = (value: number, length = 2) => String(value).padStart(length, "0");
  return `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())}`;
}

export default function EmpresasPage() {
  const empresaUseCases = useEmpresaUseCases();
  const [empresas, setEmpresas] = useState<Empresa[]>([]);
  const [cargando, setCargando] = useState(true);
  const [guardando, setGuardando] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [aviso, setAviso] = useState<string | null>(null);
  const [dialogoAbierto, setDialogoAbierto] = useState(false);
  const [empresaEdicion, setEmpresaEdicion] = useState<Empresa | null>(null);
  const [formulario, setFormulario] = useState<EmpresaFormulario>(FORMULARIO_VACIO);
  const [idBusqueda, setIdBusqueda] = useState("");
  const [confirmacionEliminacion, setConfirmacionEliminacion] = useState<Empresa | null>(null);

  const cargarEmpresas = useCallback(async () => {
    setCargando(true);
    setError(null);
    try {
      setEmpresas(await empresaUseCases.listar());
    } catch (exception) {
      setError(mensajeError(exception));
    } finally {
      setCargando(false);
    }
  }, [empresaUseCases]);

  useEffect(() => {
    void cargarEmpresas();
  }, [cargarEmpresas]);

  const abrirCrear = () => {
    setEmpresaEdicion(null);
    setFormulario(FORMULARIO_VACIO);
    setError(null);
    setAviso(null);
    setDialogoAbierto(true);
  };

  const abrirEdicion = (empresa: Empresa) => {
    setEmpresaEdicion(empresa);
    setFormulario({
      ruc: empresa.ruc,
      razonSocial: empresa.razonSocial,
      nombreComercial: empresa.nombreComercial ?? "",
      direccionMatriz: empresa.direccionMatriz,
      obligadoContabilidad: empresa.obligadoContabilidad,
      contribuyenteRimpe: empresa.contribuyenteRimpe ?? RIMPE_NO_APLICA,
    });
    setError(null);
    setAviso(null);
    setDialogoAbierto(true);
  };

  const cambiarCampo = (campo: keyof EmpresaFormulario, valor: string) => {
    setFormulario((actual) => ({
      ...actual,
      [campo]: campo === "contribuyenteRimpe" ? (valor || null) : valor,
    }));
  };

  const guardar = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError(null);
    setAviso(null);

    if (!formulario.obligadoContabilidad) {
      setError(resolveMessage(MessageCodes.EMPRESA_ACCOUNTING_VALUE_REQUIRED));
      return;
    }

    const datos: EmpresaDatos = {
      ...formulario,
      obligadoContabilidad:
        formulario.obligadoContabilidad === "SI" ? "SI" : "NO",
      ruc: formulario.ruc.trim(),
      razonSocial: formulario.razonSocial.trim(),
      nombreComercial: formulario.nombreComercial?.trim() || null,
      direccionMatriz: formulario.direccionMatriz.trim(),
      contribuyenteRimpe:
        formulario.contribuyenteRimpe === RIMPE_NO_APLICA
          ? null
          : formulario.contribuyenteRimpe || null,
    };

    if (!/^\d{13}$/.test(datos.ruc)) {
      setError(resolveMessage(MessageCodes.EMPRESA_RUC_INVALID));
      return;
    }
    if (!datos.razonSocial || !datos.direccionMatriz) {
      setError(resolveMessage(MessageCodes.EMPRESA_REQUIRED_FIELDS));
      return;
    }

    setGuardando(true);
    try {
      if (empresaEdicion) {
        await empresaUseCases.actualizar(empresaEdicion.id, datos);
        setAviso(resolveMessage(MessageCodes.EMPRESA_UPDATED));
      } else {
        await empresaUseCases.crear(datos);
        setAviso(resolveMessage(MessageCodes.EMPRESA_CREATED));
      }
      setDialogoAbierto(false);
      setIdBusqueda("");
      await cargarEmpresas();
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
    setEmpresas([]);
    const id = Number(idBusqueda);
    if (!Number.isSafeInteger(id) || id <= 0) {
      setError(resolveMessage(MessageCodes.EMPRESA_ID_INVALID));
      return;
    }
    setCargando(true);
    try {
      const empresa = await empresaUseCases.obtener(id);
      setEmpresas([empresa]);
      setAviso(resolveMessage(MessageCodes.EMPRESA_FOUND, { id: empresa.id }));
    } catch (exception) {
      setEmpresas([]);
      setError(mensajeError(exception));
    } finally {
      setCargando(false);
    }
  };

  const buscarTodas = async () => {
    setIdBusqueda("");
    setAviso(null);
    await cargarEmpresas();
  };

  const confirmarEliminacion = async () => {
    if (!confirmacionEliminacion) return;
    setGuardando(true);
    setError(null);
    setAviso(null);
    try {
      await empresaUseCases.eliminar(confirmacionEliminacion.id);
      setAviso(resolveMessage(MessageCodes.EMPRESA_DELETED, { name: confirmacionEliminacion.razonSocial }));
      setConfirmacionEliminacion(null);
      await cargarEmpresas();
    } catch (exception) {
      setError(mensajeError(exception));
    } finally {
      setGuardando(false);
    }
  };


  return (
    <Box className="empresas-page">
      <Box className="empresas-page-heading">
        <Box>
          <Typography className="page-title" component="h1" variant="h4">{resolveMessage(MessageCodes.EMPRESA_PAGE_TITLE)}</Typography>
          <Typography color="text.secondary">
            {resolveMessage(MessageCodes.EMPRESA_PAGE_DESCRIPTION)}
          </Typography>
        </Box>
        <Button variant="contained" startIcon={<AddBusinessOutlinedIcon />} onClick={abrirCrear}>
          {resolveMessage(MessageCodes.EMPRESA_NEW)}
        </Button>
      </Box>

      {error && <Alert severity="error" onClose={() => setError(null)} className="empresas-feedback">{error}</Alert>}
      {aviso && <Alert severity="success" onClose={() => setAviso(null)} className="empresas-feedback">{aviso}</Alert>}

      <Paper component="section" variant="outlined" className="empresas-search-panel">
        <Typography variant="h6" component="h2">{resolveMessage(MessageCodes.EMPRESA_SEARCH_TITLE)}</Typography>
        <Box component="form" className="empresas-search-form" onSubmit={buscarPorId}>
          <TextField
            label={resolveMessage(MessageCodes.EMPRESA_ID_LABEL)}
            type="number"
            size="small"
            value={idBusqueda}
            onChange={(event) => setIdBusqueda(event.target.value)}
            inputProps={{ min: 1, step: 1 }}
          />
          <Button type="submit" variant="outlined" startIcon={<SearchOutlinedIcon />}>{resolveMessage(MessageCodes.COMMON_SEARCH)}</Button>
          <Button type="button" variant="outlined" startIcon={<RefreshOutlinedIcon />} onClick={() => void buscarTodas()} disabled={cargando}>
            {resolveMessage(MessageCodes.COMMON_REFRESH)}
          </Button>
          <Button type="button" variant="text" onClick={() => { setIdBusqueda(""); setError(null); setAviso(null); }}>
            {resolveMessage(MessageCodes.COMMON_CLEAR_SEARCH)}
          </Button>
        </Box>
      </Paper>

      <Box className="empresas-list-heading">
        <Box>
          <Typography variant="h6" component="h2">{resolveMessage(MessageCodes.EMPRESA_LIST_TITLE)}</Typography>
          <Typography variant="body2" color="text.secondary">
            {empresas.length} registro(s) en la consulta actual.
          </Typography>
        </Box>
        <Button variant="text" startIcon={<RefreshOutlinedIcon />} onClick={() => void cargarEmpresas()} disabled={cargando}>
          {resolveMessage(MessageCodes.COMMON_REFRESH)}
        </Button>
      </Box>

      <TableContainer component={Paper} variant="outlined" className="empresas-table-panel">
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>{resolveMessage(MessageCodes.EMPRESA_ID_LABEL)}</TableCell>
              <TableCell>{resolveMessage(MessageCodes.EMPRESA_RUC)}</TableCell>
              <TableCell>{resolveMessage(MessageCodes.EMPRESA_LEGAL_NAME)}</TableCell>
              <TableCell>{resolveMessage(MessageCodes.EMPRESA_ADDRESS_REQUIRED)}</TableCell>
              <TableCell>{resolveMessage(MessageCodes.EMPRESA_TRADE_NAME)}</TableCell>
              <TableCell>{resolveMessage(MessageCodes.EMPRESA_ACCOUNTING_REQUIRED)}</TableCell>
              <TableCell>{resolveMessage(MessageCodes.COMMON_STATUS)}</TableCell>
              <TableCell align="right">{resolveMessage(MessageCodes.COMMON_ACTIONS)}</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {cargando ? (
              <TableRow><TableCell colSpan={8} align="center"><CircularProgress size={26} /></TableCell></TableRow>
            ) : empresas.length === 0 ? (
              <TableRow><TableCell colSpan={8} align="center">{resolveMessage(MessageCodes.EMPRESA_EMPTY)}</TableCell></TableRow>
            ) : empresas.map((empresa) => (
              <TableRow hover key={empresa.id}>
                <TableCell>{empresa.id}</TableCell>
                <TableCell>{empresa.ruc}</TableCell>
                <TableCell>{empresa.razonSocial}</TableCell>
                <TableCell>{empresa.direccionMatriz}</TableCell>
                <TableCell>{empresa.nombreComercial || "—"}</TableCell>
                <TableCell>{empresa.obligadoContabilidad}</TableCell>
                <TableCell>
                  <Chip size="small" label={empresa.estadoRegistro} color={empresa.estadoRegistro === "ACTIVO" ? "success" : "default"} />
                </TableCell>
                <TableCell align="right">
                  <Box className="empresas-row-actions">
                    <Button size="small" onClick={() => abrirEdicion(empresa)} startIcon={<EditOutlinedIcon />}>{resolveMessage(MessageCodes.COMMON_EDIT)}</Button>
                    <Button size="small" color="error" onClick={() => setConfirmacionEliminacion(empresa)} startIcon={<DeleteOutlineOutlinedIcon />}>{resolveMessage(MessageCodes.COMMON_DELETE)}</Button>
                  </Box>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      <Dialog open={dialogoAbierto} onClose={() => !guardando && setDialogoAbierto(false)} fullWidth maxWidth="md">
        <Box component="form" onSubmit={guardar}>
          <DialogTitle>{resolveMessage(empresaEdicion ? MessageCodes.EMPRESA_EDIT_TITLE : MessageCodes.EMPRESA_CREATE_TITLE)}</DialogTitle>
          <DialogContent>
            <Typography color="text.secondary" className="empresas-dialog-description">
              {resolveMessage(MessageCodes.EMPRESA_FORM_DESCRIPTION)}
            </Typography>
            <Box className="empresas-form-grid">
              <TextField
                label={resolveMessage(MessageCodes.EMPRESA_RUC)}
                value={formulario.ruc}
                onChange={(event) => cambiarCampo("ruc", event.target.value)}
                inputProps={{ maxLength: 13, inputMode: "numeric" }}
                required
                fullWidth
              />
              <TextField
                label="Razón social"
                value={formulario.razonSocial}
                onChange={(event) => cambiarCampo("razonSocial", event.target.value)}
                inputProps={{ maxLength: 300 }}
                required
                fullWidth
              />
              <TextField
                label={resolveMessage(MessageCodes.EMPRESA_TRADE_NAME)}
                value={formulario.nombreComercial ?? ""}
                onChange={(event) => cambiarCampo("nombreComercial", event.target.value)}
                inputProps={{ maxLength: 300 }}
                fullWidth
              />
              <TextField
                label={resolveMessage(MessageCodes.EMPRESA_ADDRESS_REQUIRED)}
                value={formulario.direccionMatriz}
                onChange={(event) => cambiarCampo("direccionMatriz", event.target.value)}
                inputProps={{ maxLength: 500 }}
                required
                fullWidth
              />
              <FormControl fullWidth required>
                <InputLabel id="obligado-contabilidad-label" shrink>{resolveMessage(MessageCodes.EMPRESA_ACCOUNTING_REQUIRED)}</InputLabel>
                <Select
                  labelId="obligado-contabilidad-label"
                  label="Obligado a llevar contabilidad"
                  value={formulario.obligadoContabilidad}
                  displayEmpty
                  onChange={(event) =>
                    cambiarCampo("obligadoContabilidad", String(event.target.value ?? ""))
                  }
                  required
                >
                  <MenuItem value="">{resolveMessage(MessageCodes.COMMON_SELECT_OPTION)}</MenuItem>
                  <MenuItem value="SI">{resolveMessage(MessageCodes.COMMON_YES)}</MenuItem>
                  <MenuItem value="NO">{resolveMessage(MessageCodes.COMMON_NO)}</MenuItem>
                </Select>
              </FormControl>
              <FormControl fullWidth>
                <InputLabel id="contribuyente-rimpe-label" shrink>{resolveMessage(MessageCodes.EMPRESA_RIMPE)}</InputLabel>
                <Select
                  labelId="contribuyente-rimpe-label"
                  label="Régimen tributario RIMPE"
                  value={formulario.contribuyenteRimpe ?? ""}
                  displayEmpty
                  onChange={(event) =>
                    cambiarCampo("contribuyenteRimpe", String(event.target.value ?? ""))
                  }
                >
                  <MenuItem value="">{resolveMessage(MessageCodes.COMMON_SELECT_OPTION)}</MenuItem>
                  <MenuItem value={RIMPE_NO_APLICA}>
                    {resolveMessage(MessageCodes.EMPRESA_NO_APLICA)}
                  </MenuItem>
                  <MenuItem value="CONTRIBUYENTE RÉGIMEN RIMPE">{resolveMessage(MessageCodes.EMPRESA_RIMPE)}</MenuItem>
                </Select>
              </FormControl>
            </Box>
          </DialogContent>
          <DialogActions className="empresas-dialog-actions">
            <Button onClick={() => setDialogoAbierto(false)} disabled={guardando}>{resolveMessage(MessageCodes.COMMON_CANCEL)}</Button>
            <Button type="submit" variant="contained" disabled={guardando}>
              {guardando ? "Guardando..." : empresaEdicion ? "Guardar cambios" : "Crear empresa"}
            </Button>
          </DialogActions>
        </Box>
      </Dialog>

      <Dialog open={Boolean(confirmacionEliminacion)} onClose={() => !guardando && setConfirmacionEliminacion(null)}>
        <DialogTitle>{resolveMessage(MessageCodes.EMPRESA_DELETE_TITLE)}</DialogTitle>
        <DialogContent>
          <Typography>
            {resolveMessage(MessageCodes.EMPRESA_DELETE_CONFIRM_PREFIX)} <strong>{confirmacionEliminacion?.razonSocial}</strong>?
            {resolveMessage(MessageCodes.EMPRESA_DELETE_CONFIRM_DESCRIPTION)}
          </Typography>
        </DialogContent>
        <DialogActions className="empresas-dialog-actions">
          <Button onClick={() => setConfirmacionEliminacion(null)} disabled={guardando}>{resolveMessage(MessageCodes.COMMON_CANCEL)}</Button>
          <Button color="error" variant="contained" onClick={() => void confirmarEliminacion()} disabled={guardando}>
            {resolveMessage(guardando ? MessageCodes.COMMON_DELETING : MessageCodes.EMPRESA_DELETE_CONFIRM_TITLE)}
          </Button>
        </DialogActions>
      </Dialog>

      <Typography className="empresas-page-date" variant="caption" color="text.secondary">
        {resolveMessage(MessageCodes.EMPRESA_QUERY_DATE, { date: fechaLocalActual() })}
      </Typography>
    </Box>
  );
}
