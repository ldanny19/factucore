import { useEffect, useState, type ReactNode } from "react";
import {
  AppBar,
  Avatar,
  Box,
  Button,
  Collapse,
  Divider,
  Drawer,
  IconButton,
  List,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Toolbar,
  Typography,
} from "@mui/material";
import MenuRoundedIcon from "@mui/icons-material/MenuRounded";
import BusinessRoundedIcon from "@mui/icons-material/BusinessRounded";
import ReceiptLongRoundedIcon from "@mui/icons-material/ReceiptLongRounded";
import SettingsRoundedIcon from "@mui/icons-material/SettingsRounded";
import AccountTreeRoundedIcon from "@mui/icons-material/AccountTreeRounded";
import HistoryRoundedIcon from "@mui/icons-material/HistoryRounded";
import AssessmentRoundedIcon from "@mui/icons-material/AssessmentRounded";
import AdminPanelSettingsRoundedIcon from "@mui/icons-material/AdminPanelSettingsRounded";
import ExpandLessRoundedIcon from "@mui/icons-material/ExpandLessRounded";
import ExpandMoreRoundedIcon from "@mui/icons-material/ExpandMoreRounded";
import LogoutRoundedIcon from "@mui/icons-material/LogoutRounded";
import { Link as RouterLink, Outlet, useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "react-oidc-context";
import "../../styles/app.css";
import { canAccessPath, getAccessContext, getPrimaryRole } from "../../shared/security/authorization";

const DRAWER_WIDTH = 258;

type MenuItem = {
  label: string;
  description: string;
  path: string;
  icon: ReactNode;
  children?: { label: string; path: string }[];
};

const menuItems: MenuItem[] = [
  {
    label: "Administración",
    description: "Estructura empresarial",
    path: "/app/empresas",
    icon: <BusinessRoundedIcon />,
    children: [
      { label: "Empresas", path: "/app/empresas" },
      { label: "Establecimientos", path: "/app/establecimientos" },
      { label: "Puntos de emisión", path: "/app/puntos-emision" },
      { label: "Secuenciales", path: "/app/secuenciales" },
      { label: "Certificados de firma electrónica", path: "/app/certificados-firma" },
    ],
  },
  {
    label: "Configuración",
    description: "Parámetros tributarios y empresariales",
    path: "/app/configuracion/empresa",
    icon: <SettingsRoundedIcon />,
    children: [
      { label: "Configuración de empresa", path: "/app/configuracion/empresa" },
      { label: "Tarifas tributarias", path: "/app/tarifas-tributarias" },
      { label: "Catálogos tributarios", path: "/app/catalogos-tributarios" },
    ],
  },
  {
    label: "Motor",
    description: "Definiciones y transformación documental",
    path: "/app/definiciones-documentales",
    icon: <AccountTreeRoundedIcon />,
    children: [
      { label: "Definiciones documentales y XSD", path: "/app/definiciones-documentales" },
      { label: "Mapeos XSD", path: "/app/mapeos-xsd" },
    ],
  },
  {
    label: "Auditoría",
    description: "Trazabilidad y evidencias",
    path: "/app/auditoria/historial",
    icon: <HistoryRoundedIcon />,
    children: [
      { label: "Historial de acciones", path: "/app/auditoria/historial" },
      { label: "Trazabilidad de comprobantes y procesos", path: "/app/auditoria/trazabilidad" },
      { label: "Evidencias de procesamiento", path: "/app/auditoria/evidencias" },
    ],
  },
  {
    label: "Reportes de comprobantes",
    description: "Consulta y seguimiento",
    path: "/app/reportes-comprobantes/resumen",
    icon: <AssessmentRoundedIcon />,
    children: [
      { label: "Resumen del día", path: "/app/reportes-comprobantes/resumen" },
      { label: "Consulta de comprobantes", path: "/app/reportes-comprobantes/consulta" },
      { label: "Seguimiento de estados", path: "/app/reportes-comprobantes/seguimiento" },
      { label: "Pendientes o fallidos", path: "/app/reportes-comprobantes/pendientes-fallidos" },
      { label: "Filtros y búsqueda", path: "/app/reportes-comprobantes/busqueda" },
    ],
  },
  {
    label: "Administración de accesos",
    description: "Usuarios, roles y permisos",
    path: "/app/accesos/usuarios",
    icon: <AdminPanelSettingsRoundedIcon />,
    children: [
      { label: "Usuarios", path: "/app/accesos/usuarios" },
      { label: "Roles", path: "/app/accesos/roles" },
      { label: "Permisos", path: "/app/accesos/permisos" },
      { label: "Asignación de roles a usuarios", path: "/app/accesos/asignacion-roles" },
    ],
  },
];

interface UserClaims {
  name?: string;
  preferred_username?: string;
  given_name?: string;
  family_name?: string;
  email?: string;
}

function getUserName(profile: Record<string, unknown> | undefined, token: string | null) {
  let claims: UserClaims = {};
  try {
    if (token) {
      const payload = token.split(".")[1]?.replace(/-/g, "+").replace(/_/g, "/");
      if (payload) claims = JSON.parse(window.atob(payload.padEnd(Math.ceil(payload.length / 4) * 4, "="))) as UserClaims;
    }
  } catch {
    claims = {};
  }

  const candidates = [
    profile?.preferred_username,
    claims.preferred_username,
    profile?.name,
    claims.name,
    [claims.given_name, claims.family_name].filter(Boolean).join(" "),
    profile?.email,
    claims.email,
  ];
  const value = candidates.find((candidate) => typeof candidate === "string" && candidate.trim());
  return typeof value === "string" ? value : "Usuario";
}

export default function AppLayout() {
  const auth = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [mobileOpen, setMobileOpen] = useState(false);
  const [openGroups, setOpenGroups] = useState<string[]>([]);
  const [now, setNow] = useState(() => new Date());
  const accessContext = getAccessContext(auth.user?.access_token);
  const primaryRole = getPrimaryRole(accessContext.roles);
  const visibleMenuItems = menuItems.flatMap((item) => {
    if (item.children?.length) {
      const children = item.children.filter((child) => canAccessPath(child.path, accessContext.roles));
      return children.length > 0 ? [{ ...item, children, path: children[0].path }] : [];
    }
    return canAccessPath(item.path, accessContext.roles) ? [item] : [];
  });

  useEffect(() => {
    const timer = window.setInterval(() => setNow(new Date()), 1000);
    return () => window.clearInterval(timer);
  }, []);

  useEffect(() => {
    const activeGroup = visibleMenuItems.find(
      (item) => item.children?.some((child) => child.path === location.pathname),
    );
    if (activeGroup) {
      setOpenGroups((current) =>
        current.includes(activeGroup.label) ? current : [...current, activeGroup.label],
      );
    }
  }, [location.pathname, accessContext.roles.join(",")]);

  const userName = getUserName(auth.user?.profile as Record<string, unknown> | undefined, accessContext.token);
  const dateLabel = new Intl.DateTimeFormat("es-EC", {
    weekday: "short",
    day: "2-digit",
    month: "short",
    year: "numeric",
  }).format(now);
  const timeLabel = new Intl.DateTimeFormat("es-EC", {
    hour: "2-digit",
    minute: "2-digit",
    second: "2-digit",
    hour12: false,
  }).format(now);

  const handleLogout = async () => {
    sessionStorage.removeItem("factucore.access_token");
    if (auth.isAuthenticated) {
      await auth.signoutRedirect();
      return;
    }
    navigate("/login", { replace: true });
  };

  const toggleGroup = (label: string) => {
    setOpenGroups((current) =>
      current.includes(label) ? current.filter((item) => item !== label) : [...current, label],
    );
  };

  const sidebar = (
    <Box className="app-sidebar-content">
      <Box className="app-brand">
        <Box className="app-brand-mark"><ReceiptLongRoundedIcon /></Box>
        <Box>
          <Typography className="app-brand-name">FactuCore</Typography>
          <Typography className="app-brand-caption">MENÚ PRINCIPAL</Typography>
        </Box>
      </Box>
      <Divider className="app-sidebar-divider" />
      <List className="app-navigation" disablePadding>
        {visibleMenuItems.map((item) => {
          const hasChildren = Boolean(item.children?.length);
          const selected = !hasChildren && location.pathname === item.path;
          const expanded = openGroups.includes(item.label);

          if (hasChildren) {
            return (
              <Box key={item.label}>
                <ListItemButton
                  selected={false}
                  onClick={() => toggleGroup(item.label)}
                  aria-expanded={expanded}
                  className="app-navigation-item app-navigation-group"
                >
                  <ListItemIcon className="app-navigation-icon">{item.icon}</ListItemIcon>
                  <ListItemText
                    primary={item.label}
                    secondary={item.description}
                    primaryTypographyProps={{ fontWeight: selected ? 750 : 600 }}
                    secondaryTypographyProps={{ fontSize: "0.72rem" }}
                  />
                  {expanded ? <ExpandLessRoundedIcon /> : <ExpandMoreRoundedIcon />}
                </ListItemButton>
                <Collapse in={expanded} timeout="auto" unmountOnExit>
                  <List disablePadding className="app-subnavigation">
                    {item.children?.map((child) => {
                      const childSelected = location.pathname === child.path;
                      return (
                        <ListItemButton
                          key={child.path}
                          component={RouterLink}
                          to={child.path}
                          selected={childSelected}
                          onClick={() => setMobileOpen(false)}
                          className="app-navigation-item app-subnavigation-item"
                        >
                          <ListItemText
                            primary={child.label}
                            primaryTypographyProps={{ fontWeight: childSelected ? 750 : 550 }}
                          />
                        </ListItemButton>
                      );
                    })}
                  </List>
                </Collapse>
              </Box>
            );
          }

          return (
            <ListItemButton
              key={item.path}
              component={RouterLink}
              to={item.path}
              selected={selected}
              onClick={() => setMobileOpen(false)}
              className="app-navigation-item"
            >
              <ListItemIcon className="app-navigation-icon">{item.icon}</ListItemIcon>
              <ListItemText
                primary={item.label}
                secondary={item.description}
                primaryTypographyProps={{ fontWeight: selected ? 750 : 600 }}
                secondaryTypographyProps={{ fontSize: "0.72rem" }}
              />
            </ListItemButton>
          );
        })}
      </List>
      <Box className="app-sidebar-footer">
        <Typography variant="caption">FactuCore</Typography>
        <Typography variant="caption">Portal de gestión electrónica</Typography>
      </Box>
    </Box>
  );

  return (
    <Box className="app-shell">
      <AppBar position="fixed" elevation={0} className="app-header">
        <Toolbar className="app-header-toolbar">
          <Box className="app-header-brand">
            <Box className="app-header-logo"><ReceiptLongRoundedIcon /></Box>
          </Box>
          <IconButton
            aria-label="Abrir menú"
            onClick={() => setMobileOpen(true)}
            className="app-mobile-menu"
          >
            <MenuRoundedIcon />
          </IconButton>
          <Box className="app-header-system">
            <Typography className="app-header-system-name">FactuCore</Typography>
            <Typography className="app-header-system-slogan">Facturación electrónica empresarial</Typography>
          </Box>
          <Box className="app-header-spacer" />
          <Box className="app-header-session">
            <Box className="app-header-clock">
              <Typography className="app-header-date">{dateLabel}</Typography>
              <Typography className="app-header-time">{timeLabel}</Typography>
            </Box>
            <Divider orientation="vertical" flexItem className="app-header-session-divider" />
            <Box className="app-header-user">
              <Avatar className="app-user-avatar">{userName.charAt(0).toUpperCase()}</Avatar>
              <Box className="app-user-details">
                <Typography className="app-user-name">{userName}</Typography>
                <Typography className="app-user-role">{primaryRole ?? "Sesión activa"}</Typography>
              </Box>
            </Box>
            <Button
              variant="outlined"
              startIcon={<LogoutRoundedIcon />}
              onClick={handleLogout}
              className="app-logout-button"
            >
              Salir
            </Button>
          </Box>
        </Toolbar>
      </AppBar>
      <Box component="nav" aria-label="Navegación principal">
        <Drawer
          variant="temporary"
          open={mobileOpen}
          onClose={() => setMobileOpen(false)}
          ModalProps={{ keepMounted: true }}
          className="app-drawer app-drawer-mobile"
        >
          {sidebar}
        </Drawer>
        <Drawer
          variant="permanent"
          open
          className="app-drawer app-drawer-desktop"
        >
          {sidebar}
        </Drawer>
      </Box>

      <Box component="main" className="app-main">
        <Box className="app-content"><Outlet /></Box>
        <Box component="footer" className="app-footer">
          <Typography variant="caption">© {now.getFullYear()} FactuCore · Plataforma de facturación electrónica</Typography>
        </Box>
      </Box>
    </Box>
  );
}
