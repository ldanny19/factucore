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
import DashboardRoundedIcon from "@mui/icons-material/DashboardRounded";
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

const DRAWER_WIDTH = 258;

type MenuItem = {
  label: string;
  description: string;
  path: string;
  icon: ReactNode;
  children?: { label: string; path: string }[];
};

const menuItems: MenuItem[] = [
  { label: "Inicio", description: "Resumen general", path: "/app", icon: <DashboardRoundedIcon /> },
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

function getUserName(profile: Record<string, unknown> | undefined) {
  const value = profile?.name ?? profile?.preferred_username ?? profile?.email;
  return typeof value === "string" && value.trim() ? value : "Usuario";
}

export default function AppLayout() {
  const auth = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [mobileOpen, setMobileOpen] = useState(false);
  const [openGroups, setOpenGroups] = useState<string[]>([]);
  const [now, setNow] = useState(() => new Date());

  useEffect(() => {
    const timer = window.setInterval(() => setNow(new Date()), 1000);
    return () => window.clearInterval(timer);
  }, []);

  useEffect(() => {
    const activeGroup = menuItems.find(
      (item) => item.children?.some((child) => child.path === location.pathname),
    );
    if (activeGroup) {
      setOpenGroups((current) =>
        current.includes(activeGroup.label) ? current : [...current, activeGroup.label],
      );
    }
  }, [location.pathname]);

  const userName = getUserName(auth.user?.profile as Record<string, unknown> | undefined);
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
          <Typography className="app-brand-caption">FACTURACIÓN ELECTRÓNICA</Typography>
        </Box>
      </Box>
      <Divider className="app-sidebar-divider" />
      <Typography className="app-menu-caption">MENÚ PRINCIPAL</Typography>
      <List className="app-navigation" disablePadding>
        {menuItems.map((item) => {
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
                <Typography className="app-user-role">Sesión activa</Typography>
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
          sx={{ display: { xs: "block", md: "none" }, "& .MuiDrawer-paper": { width: { xs: "min(258px, 86vw)", sm: DRAWER_WIDTH }, height: "100dvh", maxHeight: "100vh", overflowY: "auto", overscrollBehavior: "contain" } }}
        >
          {sidebar}
        </Drawer>
        <Drawer
          variant="permanent"
          open
          sx={{ display: { xs: "none", md: "block" }, "& .MuiDrawer-paper": { width: DRAWER_WIDTH, height: "100dvh", maxHeight: "100vh", overflowY: "auto", overscrollBehavior: "contain" } }}
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
