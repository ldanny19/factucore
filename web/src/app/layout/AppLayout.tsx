import { useEffect, useState } from "react";
import {
  AppBar,
  Avatar,
  Box,
  Divider,
  Drawer,
  IconButton,
  List,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Menu,
  MenuItem,
  Toolbar,
  Tooltip,
  Typography,
} from "@mui/material";
import MenuRoundedIcon from "@mui/icons-material/MenuRounded";
import DashboardRoundedIcon from "@mui/icons-material/DashboardRounded";
import BusinessRoundedIcon from "@mui/icons-material/BusinessRounded";
import ReceiptLongRoundedIcon from "@mui/icons-material/ReceiptLongRounded";
import SettingsRoundedIcon from "@mui/icons-material/SettingsRounded";
import LogoutRoundedIcon from "@mui/icons-material/LogoutRounded";
import AccountCircleRoundedIcon from "@mui/icons-material/AccountCircleRounded";
import { Link as RouterLink, Outlet, useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "react-oidc-context";
import "../../styles/app-layout.css";

const DRAWER_WIDTH = 258;

const menuItems = [
  { label: "Inicio", description: "Resumen general", path: "/app", icon: <DashboardRoundedIcon /> },
  { label: "Empresas", description: "Datos empresariales", path: "/app/empresas", icon: <BusinessRoundedIcon /> },
  { label: "Comprobantes", description: "Documentos electrónicos", path: "/app/comprobantes", icon: <ReceiptLongRoundedIcon /> },
  { label: "Configuración", description: "Preferencias del sistema", path: "/app/configuracion", icon: <SettingsRoundedIcon /> },
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
  const [menuAnchor, setMenuAnchor] = useState<HTMLElement | null>(null);
  const [now, setNow] = useState(() => new Date());

  useEffect(() => {
    const timer = window.setInterval(() => setNow(new Date()), 1000);
    return () => window.clearInterval(timer);
  }, []);

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
    setMenuAnchor(null);
    sessionStorage.removeItem("factucore.access_token");
    if (auth.isAuthenticated) {
      await auth.signoutRedirect();
      return;
    }
    navigate("/login", { replace: true });
  };

  const sidebar = (
    <Box className="app-sidebar-content">
      <Box className="app-brand">
        <Box className="app-brand-mark"><ReceiptLongRoundedIcon /></Box>
        <Box>
          <Typography className="app-brand-name">FactuCore</Typography>
          <Typography className="app-brand-caption">PORTAL EMPRESARIAL</Typography>
        </Box>
      </Box>
      <Divider className="app-sidebar-divider" />
      <Typography className="app-menu-caption">MENÚ PRINCIPAL</Typography>
      <List className="app-navigation" disablePadding>
        {menuItems.map((item) => {
          const selected = item.path === "/app"
            ? location.pathname === item.path
            : location.pathname === item.path || location.pathname.startsWith(`${item.path}/`);
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
          <IconButton
            aria-label="Abrir menú"
            onClick={() => setMobileOpen(true)}
            className="app-mobile-menu"
          >
            <MenuRoundedIcon />
          </IconButton>
          <Box className="app-header-context">
            <Typography className="app-header-section">
              {menuItems.find((item) => item.path === location.pathname)?.label ?? "FactuCore"}
            </Typography>
            <Typography className="app-header-date">{dateLabel} · {timeLabel}</Typography>
          </Box>
          <Box className="app-header-spacer" />
          <Tooltip title="Cuenta de usuario">
            <IconButton onClick={(event) => setMenuAnchor(event.currentTarget)} className="app-user-button">
              <Avatar className="app-user-avatar">{userName.charAt(0).toUpperCase()}</Avatar>
              <Box className="app-user-details">
                <Typography className="app-user-name">{userName}</Typography>
                <Typography className="app-user-role">Sesión activa</Typography>
              </Box>
            </IconButton>
          </Tooltip>
          <Menu
            anchorEl={menuAnchor}
            open={Boolean(menuAnchor)}
            onClose={() => setMenuAnchor(null)}
            anchorOrigin={{ vertical: "bottom", horizontal: "right" }}
            transformOrigin={{ vertical: "top", horizontal: "right" }}
          >
            <MenuItem onClick={() => setMenuAnchor(null)}><AccountCircleRoundedIcon fontSize="small" sx={{ mr: 1 }} />Mi cuenta</MenuItem>
            <MenuItem onClick={handleLogout}><LogoutRoundedIcon fontSize="small" sx={{ mr: 1 }} />Cerrar sesión</MenuItem>
          </Menu>
        </Toolbar>
      </AppBar>

      <Box component="nav" aria-label="Navegación principal">
        <Drawer
          variant="temporary"
          open={mobileOpen}
          onClose={() => setMobileOpen(false)}
          ModalProps={{ keepMounted: true }}
          sx={{ display: { xs: "block", md: "none" }, "& .MuiDrawer-paper": { width: DRAWER_WIDTH } }}
        >
          {sidebar}
        </Drawer>
        <Drawer
          variant="permanent"
          open
          sx={{ display: { xs: "none", md: "block" }, "& .MuiDrawer-paper": { width: DRAWER_WIDTH } }}
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
