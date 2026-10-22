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
  IconButton,
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
import { MessageCodes } from "../../../shared/messages/MessageCodes";
import { resolveMessage } from "../../../shared/messages/MessageResolver";
import type {
  Catalogo,
  CatalogoDatos,
  CatalogoItem,
  CatalogoItemDatos,
} from "../domain/Catalogo";
import { useCatalogoUseCase } from "./hooks/CatalogoUseCaseContext";
import "../../../styles/app.css";

interface CatalogoFormulario {
  codigo: string;
  nombre: string;
  descripcion: string;
}

interface CatalogoItemFormulario extends CatalogoFormulario {
  orden: string;
}

type EliminacionPendiente =
  | { tipo: "catalogo"; registro: Catalogo }
  | { tipo: "item"; registro: CatalogoItem };

const CATALOGO_VACIO: CatalogoFormulario = {
  codigo: "",
  nombre: "",
  descripcion: "",
};

const ITEM_VACIO: CatalogoItemFormulario = {
  ...CATALOGO_VACIO,
  orden: "",
};

function mensajeError(error: unknown): string {
  if (error instanceof Error && error.message.trim()) return error.message;
  return resolveMessage(MessageCodes.COMMON_OPERATION_FAILED);
}

function estadoActivo(estado: string): boolean {
  return estado === "ACTIVO";
}

export default function CatalogosPage() {
  const useCases = useCatalogoUseCase();
  const [catalogos, setCatalogos] = useState<Catalogo[]>([]);
  const [catalogoSeleccionadoId, setCatalogoSeleccionadoId] = useState<number | null>(null);
  const [items, setItems] = useState<CatalogoItem[]>([]);
  const [cargandoCatalogos, setCargandoCatalogos] = useState(true);
  const [cargandoItems, setCargandoItems] = useState(false);
  const [guardando, setGuardando] = useState(false);
  const [operacion, setOperacion] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [aviso, setAviso] = useState<string | null>(null);
  const [dialogoCatalogoAbierto, setDialogoCatalogoAbierto] = useState(false);
  const [dialogoItemAbierto, setDialogoItemAbierto] = useState(false);
  const [catalogoEdicion, setCatalogoEdicion] = useState<Catalogo | null>(null);
  const [itemEdicion, setItemEdicion] = useState<CatalogoItem | null>(null);
  const [catalogoFormulario, setCatalogoFormulario] =
    useState<CatalogoFormulario>(CATALOGO_VACIO);
  const [itemFormulario, setItemFormulario] = useState<CatalogoItemFormulario>(ITEM_VACIO);
  const [eliminacionPendiente, setEliminacionPendiente] =
    useState<EliminacionPendiente | null>(null);

  const catalogoSeleccionado =
    catalogos.find(({ id }) => id === catalogoSeleccionadoId) ?? null;

  const cargarCatalogos = useCallback(async () => {
    setCargandoCatalogos(true);
    setError(null);
    try {
      const resultado = await useCases.listar();
      setCatalogos(resultado);
      setCatalogoSeleccionadoId((actual) =>
        actual !== null && resultado.some(({ id }) => id === actual) ? actual : null,
      );
    } catch (exception) {
      setError(mensajeError(exception));
      setCatalogos([]);
    } finally {
      setCargandoCatalogos(false);
    }
  }, [useCases]);

  const cargarItems = useCallback(
    async (catalogoId: number) => {
      setCargandoItems(true);
      setError(null);
      try {
        setItems(await useCases.listarItems(catalogoId));
      } catch (exception) {
        setItems([]);
        setError(mensajeError(exception));
      } finally {
        setCargandoItems(false);
      }
    },
    [useCases],
  );

  useEffect(() => {
    void cargarCatalogos();
  }, [cargarCatalogos]);

  useEffect(() => {
    if (catalogoSeleccionadoId === null) {
      setItems([]);
      setCargandoItems(false);
      return;
    }
    void cargarItems(catalogoSeleccionadoId);
  }, [catalogoSeleccionadoId, cargarItems]);

  const seleccionarCatalogo = (catalogoId: number) => {
    setError(null);
    setAviso(null);
    setCatalogoSeleccionadoId(catalogoId);
  };

  const abrirCrearCatalogo = () => {
    setCatalogoEdicion(null);
    setCatalogoFormulario(CATALOGO_VACIO);
    setError(null);
    setAviso(null);
    setDialogoCatalogoAbierto(true);
  };

  const abrirEditarCatalogo = (catalogo: Catalogo) => {
    setCatalogoEdicion(catalogo);
    setCatalogoFormulario({
      codigo: catalogo.codigo,
      nombre: catalogo.nombre,
      descripcion: catalogo.descripcion ?? "",
    });
    setError(null);
    setAviso(null);
    setDialogoCatalogoAbierto(true);
  };

  const guardarCatalogo = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError(null);
    setAviso(null);
    const datos: CatalogoDatos = {
      codigo: catalogoFormulario.codigo.trim(),
      nombre: catalogoFormulario.nombre.trim(),
      descripcion: catalogoFormulario.descripcion.trim() || null,
    };
    if (!datos.codigo || !datos.nombre) {
      setError(resolveMessage(MessageCodes.CATALOGO_REQUIRED_FIELDS));
      return;
    }

    setGuardando(true);
    try {
      const guardado = catalogoEdicion
        ? await useCases.actualizar(catalogoEdicion.id, datos)
        : await useCases.crear(datos);
      setAviso(
        resolveMessage(
          catalogoEdicion ? MessageCodes.CATALOGO_UPDATED : MessageCodes.CATALOGO_CREATED,
        ),
      );
      setDialogoCatalogoAbierto(false);
      if (!catalogoEdicion) setCatalogoSeleccionadoId(guardado.id);
      await cargarCatalogos();
    } catch (exception) {
      setError(mensajeError(exception));
    } finally {
      setGuardando(false);
    }
  };

  const abrirCrearItem = () => {
    setItemEdicion(null);
    setItemFormulario(ITEM_VACIO);
    setError(null);
    setAviso(null);
    setDialogoItemAbierto(true);
  };

  const abrirEditarItem = (item: CatalogoItem) => {
    setItemEdicion(item);
    setItemFormulario({
      codigo: item.codigo,
      nombre: item.nombre,
      descripcion: item.descripcion ?? "",
      orden: item.orden === null ? "" : String(item.orden),
    });
    setError(null);
    setAviso(null);
    setDialogoItemAbierto(true);
  };

  const guardarItem = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError(null);
    setAviso(null);
    if (catalogoSeleccionadoId === null) return;

    const ordenTexto = itemFormulario.orden.trim();
    const orden = ordenTexto === "" ? null : Number(ordenTexto);
    if (orden !== null && (!Number.isSafeInteger(orden) || orden < 0)) {
      setError(resolveMessage(MessageCodes.CATALOGO_ORDER_INVALID));
      return;
    }
    const datos: CatalogoItemDatos = {
      idCatalogo: catalogoSeleccionadoId,
      codigo: itemFormulario.codigo.trim(),
      nombre: itemFormulario.nombre.trim(),
      descripcion: itemFormulario.descripcion.trim() || null,
      orden,
    };
    if (!datos.codigo || !datos.nombre) {
      setError(resolveMessage(MessageCodes.CATALOGO_REQUIRED_FIELDS));
      return;
    }

    setGuardando(true);
    try {
      if (itemEdicion) {
        await useCases.actualizarItem(itemEdicion.id, datos);
        setAviso(resolveMessage(MessageCodes.CATALOGO_ITEM_UPDATED));
      } else {
        await useCases.crearItem(datos);
        setAviso(resolveMessage(MessageCodes.CATALOGO_ITEM_CREATED));
      }
      setDialogoItemAbierto(false);
      await cargarItems(catalogoSeleccionadoId);
    } catch (exception) {
      setError(mensajeError(exception));
    } finally {
      setGuardando(false);
    }
  };

  const cambiarEstadoCatalogo = async (catalogo: Catalogo) => {
    setOperacion(`catalogo-${catalogo.id}`);
    setError(null);
    setAviso(null);
    try {
      if (estadoActivo(catalogo.estadoRegistro)) {
        await useCases.inactivar(catalogo.id);
        setAviso(resolveMessage(MessageCodes.CATALOGO_INACTIVATED));
      } else {
        await useCases.reactivar(catalogo.id);
        setAviso(resolveMessage(MessageCodes.CATALOGO_REACTIVATED));
      }
      await cargarCatalogos();
    } catch (exception) {
      setError(mensajeError(exception));
    } finally {
      setOperacion(null);
    }
  };

  const cambiarEstadoItem = async (item: CatalogoItem) => {
    setOperacion(`item-${item.id}`);
    setError(null);
    setAviso(null);
    try {
      if (estadoActivo(item.estadoRegistro)) {
        await useCases.inactivarItem(item.id);
        setAviso(resolveMessage(MessageCodes.CATALOGO_ITEM_INACTIVATED));
      } else {
        await useCases.reactivarItem(item.id);
        setAviso(resolveMessage(MessageCodes.CATALOGO_ITEM_REACTIVATED));
      }
      await cargarItems(item.idCatalogo);
    } catch (exception) {
      setError(mensajeError(exception));
    } finally {
      setOperacion(null);
    }
  };

  const eliminarRegistro = async () => {
    if (!eliminacionPendiente) return;
    setGuardando(true);
    setError(null);
    setAviso(null);
    try {
      if (eliminacionPendiente.tipo === "catalogo") {
        await useCases.eliminar(eliminacionPendiente.registro.id);
        if (catalogoSeleccionadoId === eliminacionPendiente.registro.id) {
          setCatalogoSeleccionadoId(null);
        }
        setAviso(resolveMessage(MessageCodes.CATALOGO_DELETED));
        setEliminacionPendiente(null);
        await cargarCatalogos();
      } else {
        await useCases.eliminarItem(eliminacionPendiente.registro.id);
        setAviso(resolveMessage(MessageCodes.CATALOGO_ITEM_DELETED));
        setEliminacionPendiente(null);
        await cargarItems(eliminacionPendiente.registro.idCatalogo);
      }
    } catch (exception) {
      setError(mensajeError(exception));
    } finally {
      setGuardando(false);
    }
  };

  const estadoChip = (estado: string) => (
    <Chip
      size="small"
      color={estadoActivo(estado) ? "success" : "default"}
      label={estado}
      variant={estadoActivo(estado) ? "filled" : "outlined"}
    />
  );

  return (
    <Box className="catalogos-page">
      <Box className="catalogos-page-heading">
        <Box>
          <Typography className="page-title" component="h1" variant="h4">
            {resolveMessage(MessageCodes.CATALOGO_PAGE_TITLE)}
          </Typography>
          <Typography color="text.secondary">
            {resolveMessage(MessageCodes.CATALOGO_PAGE_DESCRIPTION)}
          </Typography>
        </Box>
        <Button
          variant="contained"
          startIcon={<AddOutlinedIcon />}
          onClick={abrirCrearCatalogo}
        >
          {resolveMessage(MessageCodes.CATALOGO_NEW)}
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

      <Paper variant="outlined" className="catalogos-panel">
        <Box className="catalogos-panel-heading">
          <Box>
            <Typography variant="h6" component="h2">
              {resolveMessage(MessageCodes.CATALOGO_LIST_TITLE)}
            </Typography>
            <Typography variant="body2" color="text.secondary">
              {resolveMessage(MessageCodes.CATALOGO_LIST_DESCRIPTION)}
            </Typography>
          </Box>
          <IconButton
            aria-label={resolveMessage(MessageCodes.COMMON_REFRESH)}
            onClick={() => void cargarCatalogos()}
            disabled={cargandoCatalogos}
          >
            <RefreshOutlinedIcon />
          </IconButton>
        </Box>
        <TableContainer>
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell>{resolveMessage(MessageCodes.CATALOGO_CODE)}</TableCell>
                <TableCell>{resolveMessage(MessageCodes.CATALOGO_NAME)}</TableCell>
                <TableCell>{resolveMessage(MessageCodes.CATALOGO_DESCRIPTION)}</TableCell>
                <TableCell>{resolveMessage(MessageCodes.COMMON_STATUS)}</TableCell>
                <TableCell align="right">{resolveMessage(MessageCodes.COMMON_ACTIONS)}</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {cargandoCatalogos ? (
                <TableRow>
                  <TableCell colSpan={5} align="center">
                    <CircularProgress size={26} />
                  </TableCell>
                </TableRow>
              ) : catalogos.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={5} align="center">
                    {resolveMessage(MessageCodes.CATALOGO_EMPTY)}
                  </TableCell>
                </TableRow>
              ) : (
                catalogos.map((catalogo) => (
                  <TableRow
                    key={catalogo.id}
                    hover
                    selected={catalogoSeleccionadoId === catalogo.id}
                    onClick={() => seleccionarCatalogo(catalogo.id)}
                    sx={{ cursor: "pointer" }}
                  >
                    <TableCell>{catalogo.codigo}</TableCell>
                    <TableCell>{catalogo.nombre}</TableCell>
                    <TableCell>{catalogo.descripcion || "—"}</TableCell>
                    <TableCell>{estadoChip(catalogo.estadoRegistro)}</TableCell>
                    <TableCell align="right" onClick={(event) => event.stopPropagation()}>
                      <IconButton
                        aria-label={resolveMessage(MessageCodes.COMMON_EDIT)}
                        onClick={() => abrirEditarCatalogo(catalogo)}
                        disabled={Boolean(operacion)}
                      >
                        <EditOutlinedIcon />
                      </IconButton>
                      <IconButton
                        aria-label={
                          estadoActivo(catalogo.estadoRegistro)
                            ? resolveMessage(MessageCodes.COMMON_DEACTIVATE)
                            : resolveMessage(MessageCodes.COMMON_REACTIVATE)
                        }
                        onClick={() => void cambiarEstadoCatalogo(catalogo)}
                        disabled={Boolean(operacion)}
                      >
                        {estadoActivo(catalogo.estadoRegistro) ? (
                          <BlockOutlinedIcon />
                        ) : (
                          <ReplayOutlinedIcon />
                        )}
                      </IconButton>
                      <IconButton
                        aria-label={resolveMessage(MessageCodes.COMMON_DELETE)}
                        onClick={() => setEliminacionPendiente({ tipo: "catalogo", registro: catalogo })}
                        disabled={Boolean(operacion)}
                        color="error"
                      >
                        <DeleteOutlineOutlinedIcon />
                      </IconButton>
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </TableContainer>
      </Paper>

      <Paper variant="outlined" className="catalogos-panel">
        <Box className="catalogos-panel-heading">
          <Box>
            <Typography variant="h6" component="h2">
              {resolveMessage(MessageCodes.CATALOGO_ITEMS_TITLE)}
            </Typography>
            <Typography variant="body2" color="text.secondary">
              {catalogoSeleccionado
                ? `${catalogoSeleccionado.codigo} — ${catalogoSeleccionado.nombre}`
                : resolveMessage(MessageCodes.CATALOGO_ITEMS_PROMPT)}
            </Typography>
          </Box>
          <Button
            variant="outlined"
            startIcon={<AddOutlinedIcon />}
            onClick={abrirCrearItem}
            disabled={!catalogoSeleccionado || !estadoActivo(catalogoSeleccionado.estadoRegistro)}
          >
            {resolveMessage(MessageCodes.CATALOGO_ITEM_NEW)}
          </Button>
        </Box>
        {catalogoSeleccionado ? (
          <TableContainer>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>{resolveMessage(MessageCodes.CATALOGO_CODE)}</TableCell>
                  <TableCell>{resolveMessage(MessageCodes.CATALOGO_NAME)}</TableCell>
                  <TableCell>{resolveMessage(MessageCodes.CATALOGO_ORDER)}</TableCell>
                  <TableCell>{resolveMessage(MessageCodes.COMMON_STATUS)}</TableCell>
                  <TableCell align="right">{resolveMessage(MessageCodes.COMMON_ACTIONS)}</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {cargandoItems ? (
                  <TableRow>
                    <TableCell colSpan={5} align="center">
                      <CircularProgress size={26} />
                    </TableCell>
                  </TableRow>
                ) : items.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={5} align="center">
                      {resolveMessage(MessageCodes.CATALOGO_ITEMS_EMPTY)}
                    </TableCell>
                  </TableRow>
                ) : (
                  items.map((item) => (
                    <TableRow key={item.id} hover>
                      <TableCell>{item.codigo}</TableCell>
                      <TableCell>{item.nombre}</TableCell>
                      <TableCell>{item.orden ?? "—"}</TableCell>
                      <TableCell>{estadoChip(item.estadoRegistro)}</TableCell>
                      <TableCell align="right">
                        <IconButton
                          aria-label={resolveMessage(MessageCodes.COMMON_EDIT)}
                          onClick={() => abrirEditarItem(item)}
                          disabled={Boolean(operacion)}
                        >
                          <EditOutlinedIcon />
                        </IconButton>
                        <IconButton
                          aria-label={
                            estadoActivo(item.estadoRegistro)
                              ? resolveMessage(MessageCodes.COMMON_DEACTIVATE)
                              : resolveMessage(MessageCodes.COMMON_REACTIVATE)
                          }
                          onClick={() => void cambiarEstadoItem(item)}
                          disabled={Boolean(operacion)}
                        >
                          {estadoActivo(item.estadoRegistro) ? (
                            <BlockOutlinedIcon />
                          ) : (
                            <ReplayOutlinedIcon />
                          )}
                        </IconButton>
                        <IconButton
                          aria-label={resolveMessage(MessageCodes.COMMON_DELETE)}
                          onClick={() => setEliminacionPendiente({ tipo: "item", registro: item })}
                          disabled={Boolean(operacion)}
                          color="error"
                        >
                          <DeleteOutlineOutlinedIcon />
                        </IconButton>
                      </TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
          </TableContainer>
        ) : null}
      </Paper>

      <Dialog
        open={dialogoCatalogoAbierto}
        onClose={() => !guardando && setDialogoCatalogoAbierto(false)}
        fullWidth
        maxWidth="sm"
      >
        <Box component="form" onSubmit={guardarCatalogo}>
          <DialogTitle>
            {resolveMessage(
              catalogoEdicion
                ? MessageCodes.CATALOGO_EDIT_TITLE
                : MessageCodes.CATALOGO_CREATE_TITLE,
            )}
          </DialogTitle>
          <DialogContent>
            <TextField
              autoFocus
              margin="dense"
              fullWidth
              required
              label={resolveMessage(MessageCodes.CATALOGO_CODE)}
              value={catalogoFormulario.codigo}
              onChange={(event) =>
                setCatalogoFormulario((actual) => ({ ...actual, codigo: event.target.value }))
              }
              inputProps={{ maxLength: 100 }}
            />
            <TextField
              margin="dense"
              fullWidth
              required
              label={resolveMessage(MessageCodes.CATALOGO_NAME)}
              value={catalogoFormulario.nombre}
              onChange={(event) =>
                setCatalogoFormulario((actual) => ({ ...actual, nombre: event.target.value }))
              }
              inputProps={{ maxLength: 300 }}
            />
            <TextField
              margin="dense"
              fullWidth
              multiline
              minRows={2}
              label={resolveMessage(MessageCodes.CATALOGO_DESCRIPTION)}
              value={catalogoFormulario.descripcion}
              onChange={(event) =>
                setCatalogoFormulario((actual) => ({
                  ...actual,
                  descripcion: event.target.value,
                }))
              }
              inputProps={{ maxLength: 500 }}
            />
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setDialogoCatalogoAbierto(false)} disabled={guardando}>
              {resolveMessage(MessageCodes.COMMON_CANCEL)}
            </Button>
            <Button type="submit" variant="contained" disabled={guardando}>
              {guardando
                ? resolveMessage(MessageCodes.COMMON_SAVING)
                : resolveMessage(MessageCodes.COMMON_SAVE)}
            </Button>
          </DialogActions>
        </Box>
      </Dialog>

      <Dialog
        open={dialogoItemAbierto}
        onClose={() => !guardando && setDialogoItemAbierto(false)}
        fullWidth
        maxWidth="sm"
      >
        <Box component="form" onSubmit={guardarItem}>
          <DialogTitle>
            {resolveMessage(
              itemEdicion
                ? MessageCodes.CATALOGO_ITEM_EDIT_TITLE
                : MessageCodes.CATALOGO_ITEM_CREATE_TITLE,
            )}
          </DialogTitle>
          <DialogContent>
            <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5, mb: 1 }}>
              {catalogoSeleccionado?.codigo} — {catalogoSeleccionado?.nombre}
            </Typography>
            <TextField
              autoFocus
              margin="dense"
              fullWidth
              required
              label={resolveMessage(MessageCodes.CATALOGO_CODE)}
              value={itemFormulario.codigo}
              onChange={(event) =>
                setItemFormulario((actual) => ({ ...actual, codigo: event.target.value }))
              }
              inputProps={{ maxLength: 100 }}
            />
            <TextField
              margin="dense"
              fullWidth
              required
              label={resolveMessage(MessageCodes.CATALOGO_NAME)}
              value={itemFormulario.nombre}
              onChange={(event) =>
                setItemFormulario((actual) => ({ ...actual, nombre: event.target.value }))
              }
              inputProps={{ maxLength: 300 }}
            />
            <TextField
              margin="dense"
              fullWidth
              type="number"
              label={resolveMessage(MessageCodes.CATALOGO_ORDER)}
              value={itemFormulario.orden}
              onChange={(event) =>
                setItemFormulario((actual) => ({ ...actual, orden: event.target.value }))
              }
              inputProps={{ min: 0, step: 1 }}
            />
            <TextField
              margin="dense"
              fullWidth
              multiline
              minRows={2}
              label={resolveMessage(MessageCodes.CATALOGO_DESCRIPTION)}
              value={itemFormulario.descripcion}
              onChange={(event) =>
                setItemFormulario((actual) => ({
                  ...actual,
                  descripcion: event.target.value,
                }))
              }
              inputProps={{ maxLength: 500 }}
            />
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setDialogoItemAbierto(false)} disabled={guardando}>
              {resolveMessage(MessageCodes.COMMON_CANCEL)}
            </Button>
            <Button type="submit" variant="contained" disabled={guardando}>
              {guardando
                ? resolveMessage(MessageCodes.COMMON_SAVING)
                : resolveMessage(MessageCodes.COMMON_SAVE)}
            </Button>
          </DialogActions>
        </Box>
      </Dialog>

      <Dialog
        open={eliminacionPendiente !== null}
        onClose={() => !guardando && setEliminacionPendiente(null)}
      >
        <DialogTitle>{resolveMessage(MessageCodes.COMMON_DELETE_CONFIRM_TITLE)}</DialogTitle>
        <DialogContent>
          <Typography>
            {resolveMessage(
              eliminacionPendiente?.tipo === "catalogo"
                ? MessageCodes.CATALOGO_DELETE_CONFIRM
                : MessageCodes.CATALOGO_ITEM_DELETE_CONFIRM,
              { name: eliminacionPendiente?.registro.nombre ?? "" },
            )}
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setEliminacionPendiente(null)} disabled={guardando}>
            {resolveMessage(MessageCodes.COMMON_CANCEL)}
          </Button>
          <Button
            variant="contained"
            color="error"
            onClick={() => void eliminarRegistro()}
            disabled={guardando}
          >
            {guardando
              ? resolveMessage(MessageCodes.COMMON_DELETING)
              : resolveMessage(MessageCodes.COMMON_DELETE)}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
