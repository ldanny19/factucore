import { useCallback, useEffect, useMemo, useState, type FormEvent, type ReactElement } from "react";
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
  FormControlLabel,
  IconButton,
  MenuItem,
  Paper,
  Stack,
  Switch,
  Tab,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Tabs,
  TextField,
  Tooltip,
  Typography,
} from "@mui/material";
import AddRoundedIcon from "@mui/icons-material/AddRounded";
import AssignmentIndOutlinedIcon from "@mui/icons-material/AssignmentIndOutlined";
import DeleteOutlineRoundedIcon from "@mui/icons-material/DeleteOutlineRounded";
import EditOutlinedIcon from "@mui/icons-material/EditOutlined";
import EmailOutlinedIcon from "@mui/icons-material/EmailOutlined";
import KeyOutlinedIcon from "@mui/icons-material/KeyOutlined";
import RefreshRoundedIcon from "@mui/icons-material/RefreshRounded";
import ShieldOutlinedIcon from "@mui/icons-material/ShieldOutlined";
import { Link as RouterLink, useLocation } from "react-router-dom";
import { MessageCodes } from "../../../shared/messages/MessageCodes";
import { resolveMessage } from "../../../shared/messages/MessageResolver";
import { APP_ROLES } from "../../../shared/security/authorization";
import {
  keycloakAccessRepository,
  KeycloakAccessError,
} from "../infrastructure/KeycloakAccessRepository";
import { PERMISOS_FACTUCORE } from "../domain/Acceso";
import type { RolKeycloak, UsuarioKeycloak } from "../domain/Acceso";

type SeccionAcceso = "usuarios" | "roles" | "permisos" | "asignacion-roles";
type FormularioUsuario = Pick<UsuarioKeycloak, "username" | "firstName" | "lastName" | "email">;

const SECCIONES: Array<{ id: SeccionAcceso; codigo: keyof typeof MessageCodes; icono: ReactElement }> = [
  { id: "usuarios", codigo: "ACCESS_MODULE_USERS", icono: <AssignmentIndOutlinedIcon /> },
  { id: "roles", codigo: "ACCESS_MODULE_ROLES", icono: <ShieldOutlinedIcon /> },
  { id: "permisos", codigo: "ACCESS_MODULE_PERMISSIONS", icono: <KeyOutlinedIcon /> },
  { id: "asignacion-roles", codigo: "ACCESS_MODULE_ASSIGNMENTS", icono: <AssignmentIndOutlinedIcon /> },
];

function seccionDesdeRuta(pathname: string): SeccionAcceso {
  const seccion = pathname.split("/").filter(Boolean).at(-1);
  return SECCIONES.some(({ id }) => id === seccion) ? (seccion as SeccionAcceso) : "usuarios";
}

function mensajeOperacion(error: unknown): string {
  if (error instanceof KeycloakAccessError && error.status === 401) {
    return resolveMessage(MessageCodes.ACCESS_MODULE_REAUTHENTICATE);
  }
  if (error instanceof KeycloakAccessError && error.status === 403) {
    return resolveMessage(MessageCodes.ACCESS_MODULE_FORBIDDEN);
  }
  if (error instanceof KeycloakAccessError && error.status === 409) {
    return resolveMessage(MessageCodes.ACCESS_MODULE_USER_CONFLICT);
  }
  if (error instanceof Error && error.message === resolveMessage(MessageCodes.ACCESS_MODULE_USERNAME_REQUIRED)) {
    return error.message;
  }
  if (error instanceof Error && error.message === resolveMessage(MessageCodes.ACCESS_MODULE_EMAIL_REQUIRED)) {
    return error.message;
  }
  if (error instanceof TypeError) {
    return resolveMessage(MessageCodes.ACCESS_MODULE_CONNECTION_ERROR);
  }
  return resolveMessage(MessageCodes.ACCESS_MODULE_LOAD_FAILED);
}

function usuarioNombre(usuario: UsuarioKeycloak): string {
  return [usuario.firstName, usuario.lastName].filter(Boolean).join(" ") || "—";
}

export default function AccessManagementPage() {
  const location = useLocation();
  const seccion = seccionDesdeRuta(location.pathname);
  const [usuarios, setUsuarios] = useState<UsuarioKeycloak[]>([]);
  const [roles, setRoles] = useState<RolKeycloak[]>([]);
  const [rolesUsuario, setRolesUsuario] = useState<RolKeycloak[]>([]);
  const [permisosPorRol, setPermisosPorRol] = useState<Record<string, RolKeycloak[]>>({});
  const [busqueda, setBusqueda] = useState("");
  const [idUsuario, setIdUsuario] = useState("");
  const [rolesSeleccionados, setRolesSeleccionados] = useState<string[]>([]);
  const [cargando, setCargando] = useState(false);
  const [guardando, setGuardando] = useState(false);
  const [actualizacion, setActualizacion] = useState(0);
  const [error, setError] = useState<string | null>(null);
  const [aviso, setAviso] = useState<string | null>(null);
  const [dialogoUsuarioAbierto, setDialogoUsuarioAbierto] = useState(false);
  const [usuarioEditando, setUsuarioEditando] = useState<UsuarioKeycloak | null>(null);
  const [formularioUsuario, setFormularioUsuario] = useState<FormularioUsuario>({
    username: "",
    firstName: "",
    lastName: "",
    email: "",
  });
  const [dialogoRolAbierto, setDialogoRolAbierto] = useState(false);
  const [nombreRol, setNombreRol] = useState("");
  const [descripcionRol, setDescripcionRol] = useState("");
  const [rolPorEliminar, setRolPorEliminar] = useState<RolKeycloak | null>(null);

  const cargarDatos = useCallback(async () => {
    setCargando(true);
    setError(null);
    try {
      const [usuariosResult, rolesResult] = await Promise.all([
        seccion === "usuarios" || seccion === "asignacion-roles"
          ? keycloakAccessRepository.listarUsuarios(busqueda, 0, 100)
          : Promise.resolve(null),
        seccion === "roles" || seccion === "permisos" || seccion === "asignacion-roles"
          ? keycloakAccessRepository.listarRoles()
          : Promise.resolve(null),
      ]);
      if (usuariosResult) {
        setUsuarios(usuariosResult);
        if (!idUsuario && seccion === "asignacion-roles" && usuariosResult.length > 0) {
          setIdUsuario(usuariosResult[0].id);
        }
      }
      if (rolesResult) setRoles(rolesResult);

      if (seccion === "permisos" && rolesResult) {
        const rolesAplicacion = rolesResult.filter((rol) => APP_ROLES.includes(rol.name as (typeof APP_ROLES)[number]));
        const permisos = await Promise.all(
          rolesAplicacion.map(async (rol) => [rol.id, await keycloakAccessRepository.listarRolesCompuestos(rol.id)] as const),
        );
        setPermisosPorRol(Object.fromEntries(permisos));
      }
    } catch (exception) {
      setError(mensajeOperacion(exception));
    } finally {
      setCargando(false);
    }
  }, [busqueda, idUsuario, seccion]);

  useEffect(() => {
    void cargarDatos();
  }, [actualizacion, cargarDatos]);

  useEffect(() => {
    if (seccion !== "asignacion-roles" || !idUsuario) {
      setRolesUsuario([]);
      setRolesSeleccionados([]);
      return;
    }
    let vigente = true;
    setCargando(true);
    keycloakAccessRepository
      .listarRolesUsuario(idUsuario)
      .then((resultado) => {
        if (!vigente) return;
        setRolesUsuario(resultado);
        setRolesSeleccionados(resultado.map((rol) => rol.id));
        setError(null);
      })
      .catch((exception: unknown) => {
        if (vigente) setError(mensajeOperacion(exception));
      })
      .finally(() => {
        if (vigente) setCargando(false);
      });
    return () => {
      vigente = false;
    };
  }, [idUsuario, seccion, actualizacion]);

  const permisoNombres = useMemo<string[]>(
    () => PERMISOS_FACTUCORE.map(({ name }) => name),
    [],
  );
  const rolesAdministrables = roles.filter(
    (rol) =>
      !permisoNombres.includes(rol.name) &&
      !rol.name.startsWith("default-roles-") &&
      !["offline_access", "uma_authorization"].includes(rol.name),
  );
  const rolesAplicacion = roles.filter((rol) =>
    APP_ROLES.includes(rol.name as (typeof APP_ROLES)[number]),
  );

  const ejecutar = async (operacion: () => Promise<void>, mensajeExito: string) => {
    setGuardando(true);
    setError(null);
    setAviso(null);
    try {
      await operacion();
      setAviso(mensajeExito);
      setActualizacion((valor) => valor + 1);
    } catch (exception) {
      setError(mensajeOperacion(exception));
    } finally {
      setGuardando(false);
    }
  };

  const abrirNuevoUsuario = () => {
    setUsuarioEditando(null);
    setFormularioUsuario({ username: "", firstName: "", lastName: "", email: "" });
    setDialogoUsuarioAbierto(true);
  };

  const abrirEdicionUsuario = (usuario: UsuarioKeycloak) => {
    setUsuarioEditando(usuario);
    setFormularioUsuario({
      username: usuario.username,
      firstName: usuario.firstName ?? "",
      lastName: usuario.lastName ?? "",
      email: usuario.email ?? "",
    });
    setDialogoUsuarioAbierto(true);
  };

  const guardarUsuario = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const username = formularioUsuario.username.trim();
    const email = formularioUsuario.email?.trim() ?? "";
    if (!username) {
      setError(resolveMessage(MessageCodes.ACCESS_MODULE_USERNAME_REQUIRED));
      return;
    }
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
      setError(resolveMessage(MessageCodes.ACCESS_MODULE_EMAIL_REQUIRED));
      return;
    }
    const perfil = {
      ...formularioUsuario,
      username,
      email,
      firstName: formularioUsuario.firstName?.trim(),
      lastName: formularioUsuario.lastName?.trim(),
    };
    void ejecutar(
      async () => {
        if (usuarioEditando) {
          await keycloakAccessRepository.actualizarUsuario({ ...usuarioEditando, ...perfil });
        } else {
          await keycloakAccessRepository.crearUsuario(perfil);
        }
        setDialogoUsuarioAbierto(false);
      },
      resolveMessage(
        usuarioEditando
          ? MessageCodes.ACCESS_MODULE_USER_UPDATED
          : MessageCodes.ACCESS_MODULE_USER_CREATED,
      ),
    );
  };

  const guardarRol = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const nombre = nombreRol.trim();
    if (!nombre) {
      setError(resolveMessage(MessageCodes.ACCESS_MODULE_ROLE_REQUIRED));
      return;
    }
    void ejecutar(
      async () => {
        await keycloakAccessRepository.crearRol(nombre, descripcionRol.trim());
        setDialogoRolAbierto(false);
        setNombreRol("");
        setDescripcionRol("");
      },
      resolveMessage(MessageCodes.ACCESS_MODULE_ROLE_CREATED),
    );
  };

  const confirmarEliminarRol = () => {
    if (!rolPorEliminar) return;
    void ejecutar(
      async () => {
        await keycloakAccessRepository.eliminarRol(rolPorEliminar);
        setRolPorEliminar(null);
      },
      resolveMessage(MessageCodes.ACCESS_MODULE_ROLE_DELETED),
    );
  };

  const actualizarEstadoUsuario = (usuario: UsuarioKeycloak) => {
    void ejecutar(
      () => keycloakAccessRepository.actualizarUsuario({ ...usuario, enabled: !usuario.enabled }),
      resolveMessage(MessageCodes.ACCESS_MODULE_USER_STATUS_UPDATED),
    );
  };

  const enviarEnlace = (usuario: UsuarioKeycloak) => {
    if (!usuario.email) {
      setError(resolveMessage(MessageCodes.ACCESS_MODULE_EMAIL_REQUIRED));
      return;
    }
    void ejecutar(
      () => keycloakAccessRepository.enviarEnlaceDefinirContrasena(usuario.id),
      resolveMessage(MessageCodes.ACCESS_MODULE_PASSWORD_LINK_SENT),
    );
  };

  const alternarPermiso = (rol: RolKeycloak, permisoName: string) => {
    const permiso = roles.find((item) => item.name === permisoName);
    if (!permiso) {
      const definicion = PERMISOS_FACTUCORE.find((item) => item.name === permisoName);
      if (!definicion) return;
      void ejecutar(
        () => keycloakAccessRepository.crearRol(definicion.name, definicion.description),
        resolveMessage(MessageCodes.ACCESS_MODULE_PERMISSION_CREATED),
      );
      return;
    }
    const asignado = (permisosPorRol[rol.id] ?? []).some((item) => item.id === permiso.id);
    void ejecutar(
      () =>
        asignado
          ? keycloakAccessRepository.retirarPermisoRol(rol.id, permiso)
          : keycloakAccessRepository.asignarPermisoRol(rol.id, permiso),
      resolveMessage(MessageCodes.ACCESS_MODULE_PERMISSION_UPDATED),
    );
  };

  const guardarAsignacionRoles = () => {
    const seleccionados = rolesAdministrables.filter((rol) => rolesSeleccionados.includes(rol.id));
    const actuales = rolesUsuario.filter((rol) => !permisoNombres.includes(rol.name));
    const agregar = seleccionados.filter((rol) => !actuales.some((actual) => actual.id === rol.id));
    const retirar = actuales.filter((rol) => !rolesSeleccionados.includes(rol.id));
    void ejecutar(async () => {
      if (agregar.length) await keycloakAccessRepository.asignarRolesUsuario(idUsuario, agregar);
      if (retirar.length) await keycloakAccessRepository.retirarRolesUsuario(idUsuario, retirar);
    }, resolveMessage(MessageCodes.ACCESS_MODULE_ASSIGNMENTS_UPDATED));
  };

  return (
    <Stack spacing={2.5}>
      <Box>
        <Typography variant="h4" component="h1" fontWeight={800}>
          {resolveMessage(MessageCodes.ACCESS_MODULE_TITLE)}
        </Typography>
        <Typography color="text.secondary" sx={{ mt: 0.5 }}>
          {resolveMessage(MessageCodes.ACCESS_MODULE_DESCRIPTION)}
        </Typography>
      </Box>

      {error && <Alert severity="error" onClose={() => setError(null)}>{error}</Alert>}
      {aviso && <Alert severity="success" onClose={() => setAviso(null)}>{aviso}</Alert>}

      <Paper variant="outlined" sx={{ overflow: "hidden" }}>
        <Tabs
          value={seccion}
          variant="scrollable"
          allowScrollButtonsMobile
          aria-label={resolveMessage(MessageCodes.ACCESS_MODULE_TITLE)}
        >
          {SECCIONES.map(({ id, codigo, icono }) => (
            <Tab
              key={id}
              value={id}
              component={RouterLink}
              to={`/app/accesos/${id}`}
              icon={icono}
              iconPosition="start"
              label={resolveMessage(codigo)}
            />
          ))}
        </Tabs>
      </Paper>

      <Stack direction={{ xs: "column", sm: "row" }} justifyContent="space-between" spacing={1.5}>
        {(seccion === "usuarios" || seccion === "asignacion-roles") && (
          <TextField
            label={resolveMessage(MessageCodes.ACCESS_MODULE_SEARCH)}
            value={busqueda}
            onChange={(event) => setBusqueda(event.target.value)}
            sx={{ width: { xs: "100%", sm: 380 } }}
          />
        )}
        <Stack direction="row" spacing={1} justifyContent="flex-end">
          {seccion === "usuarios" && (
            <Button variant="contained" startIcon={<AddRoundedIcon />} onClick={abrirNuevoUsuario}>
              {resolveMessage(MessageCodes.ACCESS_MODULE_NEW_USER)}
            </Button>
          )}
          {seccion === "roles" && (
            <Button
              variant="contained"
              startIcon={<AddRoundedIcon />}
              onClick={() => setDialogoRolAbierto(true)}
            >
              {resolveMessage(MessageCodes.ACCESS_MODULE_NEW_ROLE)}
            </Button>
          )}
          <Tooltip title={resolveMessage(MessageCodes.ACCESS_MODULE_REFRESH)}>
            <span>
              <IconButton
                aria-label={resolveMessage(MessageCodes.ACCESS_MODULE_REFRESH)}
                onClick={() => setActualizacion((valor) => valor + 1)}
                disabled={cargando}
              >
                <RefreshRoundedIcon />
              </IconButton>
            </span>
          </Tooltip>
        </Stack>
      </Stack>

      {cargando ? (
        <Box display="flex" justifyContent="center" py={6}>
          <CircularProgress />
        </Box>
      ) : (
        <>
          {seccion === "usuarios" && (
            <TableContainer component={Paper} variant="outlined">
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>{resolveMessage(MessageCodes.ACCESS_MODULE_USERNAME)}</TableCell>
                    <TableCell>{resolveMessage(MessageCodes.ACCESS_MODULE_FIRST_NAME)}</TableCell>
                    <TableCell>{resolveMessage(MessageCodes.ACCESS_MODULE_EMAIL)}</TableCell>
                    <TableCell>{resolveMessage(MessageCodes.ACCESS_MODULE_STATUS)}</TableCell>
                    <TableCell align="right">{resolveMessage(MessageCodes.ACCESS_MODULE_ACTIONS)}</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {usuarios.map((usuario) => (
                    <TableRow key={usuario.id} hover>
                      <TableCell>{usuario.username}</TableCell>
                      <TableCell>{usuarioNombre(usuario)}</TableCell>
                      <TableCell>{usuario.email || "—"}</TableCell>
                      <TableCell>
                        <Chip
                          size="small"
                          color={usuario.enabled ? "success" : "default"}
                          label={resolveMessage(
                            usuario.enabled
                              ? MessageCodes.ACCESS_MODULE_ENABLED
                              : MessageCodes.ACCESS_MODULE_DISABLED,
                          )}
                        />
                      </TableCell>
                      <TableCell align="right">
                        <Tooltip title={resolveMessage(MessageCodes.ACCESS_MODULE_EDIT)}>
                          <IconButton aria-label={resolveMessage(MessageCodes.ACCESS_MODULE_EDIT)} onClick={() => abrirEdicionUsuario(usuario)}>
                            <EditOutlinedIcon />
                          </IconButton>
                        </Tooltip>
                        <Tooltip title={resolveMessage(MessageCodes.ACCESS_MODULE_SEND_PASSWORD_LINK)}>
                          <span>
                            <IconButton
                              aria-label={resolveMessage(MessageCodes.ACCESS_MODULE_SEND_PASSWORD_LINK)}
                              onClick={() => enviarEnlace(usuario)}
                              disabled={!usuario.email || guardando}
                            >
                              <EmailOutlinedIcon />
                            </IconButton>
                          </span>
                        </Tooltip>
                        <Tooltip title={resolveMessage(usuario.enabled ? MessageCodes.ACCESS_MODULE_SET_ENABLED : MessageCodes.ACCESS_MODULE_SET_DISABLED)}>
                          <span>
                            <Switch
                              checked={usuario.enabled}
                              onChange={() => actualizarEstadoUsuario(usuario)}
                              disabled={guardando}
                              inputProps={{ "aria-label": resolveMessage(usuario.enabled ? MessageCodes.ACCESS_MODULE_SET_ENABLED : MessageCodes.ACCESS_MODULE_SET_DISABLED) }}
                            />
                          </span>
                        </Tooltip>
                      </TableCell>
                    </TableRow>
                  ))}
                  {usuarios.length === 0 && (
                    <TableRow><TableCell colSpan={5} align="center">{resolveMessage(MessageCodes.ACCESS_MODULE_EMPTY)}</TableCell></TableRow>
                  )}
                </TableBody>
              </Table>
            </TableContainer>
          )}

          {seccion === "roles" && (
            <Stack spacing={2}>
              <Alert severity="info">{resolveMessage(MessageCodes.ACCESS_MODULE_ROLE_POLICY_NOTE)}</Alert>
              <TableContainer component={Paper} variant="outlined">
                <Table>
                  <TableHead>
                    <TableRow>
                      <TableCell>{resolveMessage(MessageCodes.ACCESS_MODULE_ROLE_NAME)}</TableCell>
                      <TableCell>{resolveMessage(MessageCodes.ACCESS_MODULE_ROLE_DESCRIPTION)}</TableCell>
                      <TableCell align="right">{resolveMessage(MessageCodes.ACCESS_MODULE_ACTIONS)}</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {rolesAdministrables.map((rol) => {
                      const protegido = APP_ROLES.includes(rol.name as (typeof APP_ROLES)[number]);
                      return (
                        <TableRow key={rol.id} hover>
                          <TableCell><Chip icon={<ShieldOutlinedIcon />} label={rol.name} variant="outlined" /></TableCell>
                          <TableCell>{rol.description || "—"}</TableCell>
                          <TableCell align="right">
                            <Tooltip title={resolveMessage(MessageCodes.ACCESS_MODULE_DELETE_ROLE)}>
                              <span>
                                <IconButton
                                  aria-label={`${resolveMessage(MessageCodes.ACCESS_MODULE_DELETE_ROLE)}: ${rol.name}`}
                                  onClick={() => setRolPorEliminar(rol)}
                                  disabled={protegido || guardando}
                                >
                                  <DeleteOutlineRoundedIcon />
                                </IconButton>
                              </span>
                            </Tooltip>
                          </TableCell>
                        </TableRow>
                      );
                    })}
                    {rolesAdministrables.length === 0 && (
                      <TableRow><TableCell colSpan={3} align="center">{resolveMessage(MessageCodes.ACCESS_MODULE_EMPTY)}</TableCell></TableRow>
                    )}
                  </TableBody>
                </Table>
              </TableContainer>
            </Stack>
          )}

          {seccion === "permisos" && (
            <Stack spacing={2}>
              <Alert severity="info">{resolveMessage(MessageCodes.ACCESS_MODULE_PERMISSION_CREATE_HELP)}</Alert>
              <TableContainer component={Paper} variant="outlined">
                <Table>
                  <TableHead>
                    <TableRow>
                      <TableCell>{resolveMessage(MessageCodes.ACCESS_MODULE_PERMISSION_ROLE)}</TableCell>
                      {APP_ROLES.map((role) => <TableCell key={role} align="center">{role}</TableCell>)}
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {PERMISOS_FACTUCORE.map((permiso) => {
                      const definicion = roles.find((rol) => rol.name === permiso.name);
                      return (
                        <TableRow key={permiso.name} hover>
                          <TableCell>
                            <Typography fontWeight={700}>{permiso.label}</Typography>
                            <Typography variant="caption" color="text.secondary">{permiso.description}</Typography>
                            {!definicion && (
                              <Button
                                size="small"
                                onClick={() => alternarPermiso({ id: "", name: "" }, permiso.name)}
                                disabled={guardando}
                              >
                                {resolveMessage(MessageCodes.ACCESS_MODULE_CREATE_PERMISSION)}
                              </Button>
                            )}
                          </TableCell>
                          {APP_ROLES.map((roleName) => {
                            const rol = rolesAplicacion.find((item) => item.name === roleName);
                            const asignado = Boolean(
                              definicion &&
                                rol &&
                                (permisosPorRol[rol.id] ?? []).some((item) => item.id === definicion.id),
                            );
                            return (
                              <TableCell key={roleName} align="center">
                                <Switch
                                  checked={asignado}
                                  disabled={!definicion || !rol || guardando}
                                  onChange={() => rol && alternarPermiso(rol, permiso.name)}
                                  inputProps={{ "aria-label": `${permiso.label} · ${roleName}` }}
                                />
                              </TableCell>
                            );
                          })}
                        </TableRow>
                      );
                    })}
                  </TableBody>
                </Table>
              </TableContainer>
            </Stack>
          )}

          {seccion === "asignacion-roles" && (
            <Paper variant="outlined" sx={{ p: { xs: 2, sm: 3 } }}>
              <Stack spacing={2}>
                <TextField
                  select
                  label={resolveMessage(MessageCodes.ACCESS_MODULE_ASSIGN_USER)}
                  value={idUsuario}
                  onChange={(event) => setIdUsuario(event.target.value)}
                >
                  {usuarios.map((usuario) => (
                    <MenuItem key={usuario.id} value={usuario.id}>
                      {usuario.username} — {usuarioNombre(usuario)}
                    </MenuItem>
                  ))}
                </TextField>
                <Typography variant="subtitle1" fontWeight={700}>
                  {resolveMessage(MessageCodes.ACCESS_MODULE_ROLES_FOR_USER)}
                </Typography>
                {usuarios.length === 0 ? (
                  <Typography color="text.secondary">{resolveMessage(MessageCodes.ACCESS_MODULE_EMPTY)}</Typography>
                ) : rolesAdministrables.length === 0 ? (
                  <Typography color="text.secondary">{resolveMessage(MessageCodes.ACCESS_MODULE_NO_ROLES)}</Typography>
                ) : (
                  <Stack direction="row" flexWrap="wrap" useFlexGap gap={1}>
                    {rolesAdministrables.map((rol) => (
                      <FormControlLabel
                        key={rol.id}
                        control={
                          <Switch
                            checked={rolesSeleccionados.includes(rol.id)}
                            onChange={(event) =>
                              setRolesSeleccionados((actuales) =>
                                event.target.checked
                                  ? [...actuales, rol.id]
                                  : actuales.filter((id) => id !== rol.id),
                              )
                            }
                          />
                        }
                        label={rol.name}
                      />
                    ))}
                  </Stack>
                )}
                <Button
                  variant="contained"
                  startIcon={<AssignmentIndOutlinedIcon />}
                  onClick={guardarAsignacionRoles}
                  disabled={!idUsuario || !rolesAdministrables.length || guardando}
                  sx={{ alignSelf: "flex-start" }}
                >
                  {resolveMessage(MessageCodes.ACCESS_MODULE_SAVE_ASSIGNMENTS)}
                </Button>
              </Stack>
            </Paper>
          )}
        </>
      )}

      <Dialog open={dialogoUsuarioAbierto} onClose={() => setDialogoUsuarioAbierto(false)} fullWidth maxWidth="sm">
        <Box component="form" onSubmit={guardarUsuario}>
          <DialogTitle>
            {resolveMessage(usuarioEditando ? MessageCodes.ACCESS_MODULE_EDIT_USER : MessageCodes.ACCESS_MODULE_CREATE_USER)}
          </DialogTitle>
          <DialogContent>
            <Stack spacing={2} sx={{ pt: 1 }}>
              {!usuarioEditando && (
                <Alert severity="info">
                  {resolveMessage(MessageCodes.ACCESS_MODULE_USER_INVITATION_HELP)}
                </Alert>
              )}
              <TextField
                label={resolveMessage(MessageCodes.ACCESS_MODULE_USERNAME)}
                value={formularioUsuario.username}
                onChange={(event) => setFormularioUsuario({ ...formularioUsuario, username: event.target.value })}
                required
                disabled={Boolean(usuarioEditando)}
                autoFocus
              />
              <TextField
                label={resolveMessage(MessageCodes.ACCESS_MODULE_FIRST_NAME)}
                value={formularioUsuario.firstName ?? ""}
                onChange={(event) => setFormularioUsuario({ ...formularioUsuario, firstName: event.target.value })}
              />
              <TextField
                label={resolveMessage(MessageCodes.ACCESS_MODULE_LAST_NAME)}
                value={formularioUsuario.lastName ?? ""}
                onChange={(event) => setFormularioUsuario({ ...formularioUsuario, lastName: event.target.value })}
              />
              <TextField
                label={resolveMessage(MessageCodes.ACCESS_MODULE_EMAIL)}
                type="email"
                value={formularioUsuario.email ?? ""}
                onChange={(event) => setFormularioUsuario({ ...formularioUsuario, email: event.target.value })}
                required
              />
            </Stack>
          </DialogContent>
          <DialogActions sx={{ px: 3, pb: 2 }}>
            <Button onClick={() => setDialogoUsuarioAbierto(false)}>{resolveMessage(MessageCodes.ACCESS_MODULE_CANCEL)}</Button>
            <Button type="submit" variant="contained" disabled={guardando}>{resolveMessage(MessageCodes.ACCESS_MODULE_SAVE)}</Button>
          </DialogActions>
        </Box>
      </Dialog>

      <Dialog open={dialogoRolAbierto} onClose={() => setDialogoRolAbierto(false)} fullWidth maxWidth="sm">
        <Box component="form" onSubmit={guardarRol}>
          <DialogTitle>{resolveMessage(MessageCodes.ACCESS_MODULE_CREATE_ROLE)}</DialogTitle>
          <DialogContent>
            <Stack spacing={2} sx={{ pt: 1 }}>
              <TextField
                label={resolveMessage(MessageCodes.ACCESS_MODULE_ROLE_NAME)}
                value={nombreRol}
                onChange={(event) => setNombreRol(event.target.value)}
                required
                autoFocus
              />
              <TextField
                label={resolveMessage(MessageCodes.ACCESS_MODULE_ROLE_DESCRIPTION)}
                value={descripcionRol}
                onChange={(event) => setDescripcionRol(event.target.value)}
                multiline
                minRows={2}
              />
            </Stack>
          </DialogContent>
          <DialogActions sx={{ px: 3, pb: 2 }}>
            <Button onClick={() => setDialogoRolAbierto(false)}>{resolveMessage(MessageCodes.ACCESS_MODULE_CANCEL)}</Button>
            <Button type="submit" variant="contained" disabled={guardando}>{resolveMessage(MessageCodes.ACCESS_MODULE_SAVE)}</Button>
          </DialogActions>
        </Box>
      </Dialog>

      <Dialog open={Boolean(rolPorEliminar)} onClose={() => setRolPorEliminar(null)}>
        <DialogTitle>{resolveMessage(MessageCodes.ACCESS_MODULE_DELETE_ROLE)}</DialogTitle>
        <DialogContent>
          <Typography>{resolveMessage(MessageCodes.ACCESS_MODULE_CONFIRM_ROLE_DELETE)}</Typography>
          {rolPorEliminar && <Typography fontWeight={700} sx={{ mt: 1 }}>{rolPorEliminar.name}</Typography>}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setRolPorEliminar(null)}>{resolveMessage(MessageCodes.ACCESS_MODULE_CANCEL)}</Button>
          <Button color="error" variant="contained" onClick={confirmarEliminarRol} disabled={guardando}>
            {resolveMessage(MessageCodes.ACCESS_MODULE_DELETE_ROLE)}
          </Button>
        </DialogActions>
      </Dialog>
    </Stack>
  );
}
