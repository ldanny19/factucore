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
  Stack,
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
import type { Empresa, EmpresaDatos } from "../domain/Empresa";
import "../../../styles/app.css";

const FORMULARIO_VACIO: EmpresaDatos = {
  ruc: "",
  razonSocial: "",
  nombreComercial: "",
  direccionMatriz: "",
  obligadoContabilidad: "SI",
  contribuyenteRimpe: null,
};

function mensajeError(error: unknown): string {
  if (typeof error === "object" && error !== null && "response" in error) {
    const response = (error as { response?: { data?: { mensaje?: string; message?: string; codigo?: string } } }).response;
    return response?.data?.mensaje || response?.data?.message || response?.data?.codigo || "No fue posible completar la operación.";
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
  const [formulario, setFormulario] = useState<EmpresaDatos>(FORMULARIO_VACIO);
  const [idBusqueda, setIdBusqueda] = useState("");
  const [resultadoBusqueda, setResultadoBusqueda] = useState<Empresa | null>(null);
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
      contribuyenteRimpe: empresa.contribuyenteRimpe,
    });
    setError(null);
    setAviso(null);
    setDialogoAbierto(true);
  };

  const cambiarCampo = (campo: keyof EmpresaDatos, valor: string) => {
    setFormulario((actual) => ({
      ...actual,
      [campo]: campo === "contribuyenteRimpe" ? (valor || null) : valor,
    }));
  };

  const guardar = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError(null);
    setAviso(null);

    const datos: EmpresaDatos = {
      ...formulario,
      ruc: formulario.ruc.trim(),
      razonSocial: formulario.razonSocial.trim(),
      nombreComercial: formulario.nombreComercial?.trim() || null,
      direccionMatriz: formulario.direccionMatriz.trim(),
      contribuyenteRimpe: formulario.contribuyenteRimpe || null,
    };

    if (!/^\d{13}$/.test(datos.ruc)) {
      setError("El RUC debe contener exactamente 13 dígitos.");
      return;
    }
    if (!datos.razonSocial || !datos.direccionMatriz) {
      setError("Completa la razón social y la dirección de la matriz.");
      return;
    }

    setGuardando(true);
    try {
      if (empresaEdicion) {
        await empresaUseCases.actualizar(empresaEdicion.id, datos);
        setAviso("La empresa se actualizó correctamente.");
      } else {
        await empresaUseCases.crear(datos);
        setAviso("La empresa se creó correctamente.");
      }
      setDialogoAbierto(false);
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
    setResultadoBusqueda(null);
    const id = Number(idBusqueda);
    if (!Number.isSafeInteger(id) || id <= 0) {
      setError("Ingresa un ID de empresa válido.");
      return;
    }
    try {
      const empresa = await empresaUseCases.obtener(id);
      setResultadoBusqueda(empresa);
      setAviso(`Empresa ${empresa.id} encontrada.`);
    } catch (exception) {
      setError(mensajeError(exception));
    }
  };

  const confirmarEliminacion = async () => {
    if (!confirmacionEliminacion) return;
    setGuardando(true);
    setError(null);
    setAviso(null);
    try {
      await empresaUseCases.eliminar(confirmacionEliminacion.id);
      setAviso(`La empresa ${confirmacionEliminacion.razonSocial} fue eliminada lógicamente.`);
      setConfirmacionEliminacion(null);
      if (resultadoBusqueda?.id === confirmacionEliminacion.id) setResultadoBusqueda(null);
      await cargarEmpresas();
    } catch (exception) {
      setError(mensajeError(exception));
    } finally {
      setGuardando(false);
    }
  };

  const empresaParaMostrar = resultadoBusqueda;

  return (
    <Box className="empresas-page">
      <Box className="empresas-page-heading">
        <Box>
          <Typography className="page-title" component="h1" variant="h4">Empresas</Typography>
          <Typography color="text.secondary">
            Administra las empresas registradas en FactuCore.
          </Typography>
        </Box>
        <Button variant="contained" startIcon={<AddBusinessOutlinedIcon />} onClick={abrirCrear}>
          Nueva empresa
        </Button>
      </Box>

      {error && <Alert severity="error" onClose={() => setError(null)} className="empresas-feedback">{error}</Alert>}
      {aviso && <Alert severity="success" onClose={() => setAviso(null)} className="empresas-feedback">{aviso}</Alert>}

      <Paper component="section" variant="outlined" className="empresas-search-panel">
        <Typography variant="h6" component="h2">Buscar empresa por ID</Typography>
        <Box component="form" className="empresas-search-form" onSubmit={buscarPorId}>
          <TextField
            label="ID de empresa"
            type="number"
            size="small"
            value={idBusqueda}
            onChange={(event) => setIdBusqueda(event.target.value)}
            inputProps={{ min: 1, step: 1 }}
          />
          <Button type="submit" variant="outlined" startIcon={<SearchOutlinedIcon />}>Buscar</Button>
          <Button type="button" variant="text" startIcon={<RefreshOutlinedIcon />} onClick={() => { setIdBusqueda(""); setResultadoBusqueda(null); setError(null); setAviso(null); }}>
            Limpiar búsqueda
          </Button>
        </Box>
        {empresaParaMostrar && (
          <Box className="empresas-search-result">
            <Typography variant="subtitle2">Resultado de búsqueda</Typography>
            <Typography><strong>{empresaParaMostrar.razonSocial}</strong> · RUC {empresaParaMostrar.ruc} · ID {empresaParaMostrar.id}</Typography>
            <Stack direction="row" spacing={1} className="empresas-row-actions">
              <Button size="small" startIcon={<EditOutlinedIcon />} onClick={() => abrirEdicion(empresaParaMostrar)}>Modificar</Button>
              <Button size="small" color="error" startIcon={<DeleteOutlineOutlinedIcon />} onClick={() => setConfirmacionEliminacion(empresaParaMostrar)}>Eliminar</Button>
            </Stack>
          </Box>
        )}
      </Paper>

      <Box className="empresas-list-heading">
        <Box>
          <Typography variant="h6" component="h2">Listado de empresas</Typography>
          <Typography variant="body2" color="text.secondary">
            {empresas.length} registro(s). Se incluyen empresas activas e inactivas, no las eliminadas.
          </Typography>
        </Box>
        <Button variant="text" startIcon={<RefreshOutlinedIcon />} onClick={() => void cargarEmpresas()} disabled={cargando}>
          Actualizar
        </Button>
      </Box>

      <TableContainer component={Paper} variant="outlined" className="empresas-table-panel">
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>ID</TableCell>
              <TableCell>RUC</TableCell>
              <TableCell>Razón social</TableCell>
              <TableCell>Nombre comercial</TableCell>
              <TableCell>Obligado a llevar contabilidad</TableCell>
              <TableCell>Estado</TableCell>
              <TableCell align="right">Acciones</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {cargando ? (
              <TableRow><TableCell colSpan={7} align="center"><CircularProgress size={26} /></TableCell></TableRow>
            ) : empresas.length === 0 ? (
              <TableRow><TableCell colSpan={7} align="center">No hay empresas registradas.</TableCell></TableRow>
            ) : empresas.map((empresa) => (
              <TableRow hover key={empresa.id}>
                <TableCell>{empresa.id}</TableCell>
                <TableCell>{empresa.ruc}</TableCell>
                <TableCell>
                  <Typography variant="body2" fontWeight={700}>{empresa.razonSocial}</Typography>
                  <Typography variant="caption" color="text.secondary">{empresa.direccionMatriz}</Typography>
                </TableCell>
                <TableCell>{empresa.nombreComercial || "—"}</TableCell>
                <TableCell>{empresa.obligadoContabilidad}</TableCell>
                <TableCell>
                  <Chip size="small" label={empresa.estadoRegistro} color={empresa.estadoRegistro === "ACTIVO" ? "success" : "default"} />
                </TableCell>
                <TableCell align="right">
                  <Box className="empresas-row-actions">
                    <Button size="small" onClick={() => abrirEdicion(empresa)} startIcon={<EditOutlinedIcon />}>Editar</Button>
                    <Button size="small" color="error" onClick={() => setConfirmacionEliminacion(empresa)} startIcon={<DeleteOutlineOutlinedIcon />}>Eliminar</Button>
                  </Box>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      <Dialog open={dialogoAbierto} onClose={() => !guardando && setDialogoAbierto(false)} fullWidth maxWidth="md">
        <Box component="form" onSubmit={guardar}>
          <DialogTitle>{empresaEdicion ? "Modificar empresa" : "Crear empresa"}</DialogTitle>
          <DialogContent>
            <Typography color="text.secondary" className="empresas-dialog-description">
              Los campos se validan antes de enviarse a la API de FactuCore.
            </Typography>
            <Box className="empresas-form-grid">
              <TextField
                label="RUC"
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
                label="Nombre comercial"
                value={formulario.nombreComercial ?? ""}
                onChange={(event) => cambiarCampo("nombreComercial", event.target.value)}
                inputProps={{ maxLength: 300 }}
                fullWidth
              />
              <TextField
                label="Dirección de la matriz"
                value={formulario.direccionMatriz}
                onChange={(event) => cambiarCampo("direccionMatriz", event.target.value)}
                inputProps={{ maxLength: 500 }}
                required
                fullWidth
              />
              <FormControl fullWidth>
                <InputLabel id="obligado-contabilidad-label">Obligado a llevar contabilidad</InputLabel>
                <Select
                  labelId="obligado-contabilidad-label"
                  label="Obligado a llevar contabilidad"
                  value={formulario.obligadoContabilidad}
                  onChange={(event) => cambiarCampo("obligadoContabilidad", event.target.value)}
                >
                  <MenuItem value="SI">Sí</MenuItem>
                  <MenuItem value="NO">No</MenuItem>
                </Select>
              </FormControl>
              <FormControl fullWidth>
                <InputLabel id="contribuyente-rimpe-label">Régimen tributario RIMPE</InputLabel>
                <Select
                  labelId="contribuyente-rimpe-label"
                  label="Régimen tributario RIMPE"
                  value={formulario.contribuyenteRimpe ?? ""}
                  onChange={(event) => cambiarCampo("contribuyenteRimpe", event.target.value)}
                >
                  <MenuItem value="">No aplica</MenuItem>
                  <MenuItem value="CONTRIBUYENTE RÉGIMEN RIMPE">Contribuyente régimen RIMPE</MenuItem>
                </Select>
              </FormControl>
            </Box>
          </DialogContent>
          <DialogActions className="empresas-dialog-actions">
            <Button onClick={() => setDialogoAbierto(false)} disabled={guardando}>Cancelar</Button>
            <Button type="submit" variant="contained" disabled={guardando}>
              {guardando ? "Guardando..." : empresaEdicion ? "Guardar cambios" : "Crear empresa"}
            </Button>
          </DialogActions>
        </Box>
      </Dialog>

      <Dialog open={Boolean(confirmacionEliminacion)} onClose={() => !guardando && setConfirmacionEliminacion(null)}>
        <DialogTitle>Eliminar empresa</DialogTitle>
        <DialogContent>
          <Typography>
            ¿Confirmas la eliminación lógica de <strong>{confirmacionEliminacion?.razonSocial}</strong>?
            El registro se conservará en la base de datos, pero dejará de aparecer en el listado.
          </Typography>
        </DialogContent>
        <DialogActions className="empresas-dialog-actions">
          <Button onClick={() => setConfirmacionEliminacion(null)} disabled={guardando}>Cancelar</Button>
          <Button color="error" variant="contained" onClick={() => void confirmarEliminacion()} disabled={guardando}>
            {guardando ? "Eliminando..." : "Confirmar eliminación"}
          </Button>
        </DialogActions>
      </Dialog>

      <Typography className="empresas-page-date" variant="caption" color="text.secondary">
        Consulta realizada el {fechaLocalActual()}
      </Typography>
    </Box>
  );
}
