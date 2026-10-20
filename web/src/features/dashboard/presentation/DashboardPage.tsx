import { Box, Chip, Grid, Paper, Stack, Typography } from "@mui/material";
import DashboardRoundedIcon from "@mui/icons-material/DashboardRounded";
import BusinessRoundedIcon from "@mui/icons-material/BusinessRounded";
import ReceiptLongRoundedIcon from "@mui/icons-material/ReceiptLongRounded";
import AutorenewRoundedIcon from "@mui/icons-material/AutorenewRounded";
import { Link as RouterLink } from "react-router-dom";

const summaries = [
  { label: "Empresas registradas", value: "—", helper: "Pendiente de integración", icon: <BusinessRoundedIcon />, className: "dashboard-icon-blue" },
  { label: "Comprobantes emitidos", value: "—", helper: "Pendiente de integración", icon: <ReceiptLongRoundedIcon />, className: "dashboard-icon-green" },
  { label: "Procesos pendientes", value: "—", helper: "Pendiente de integración", icon: <AutorenewRoundedIcon />, className: "dashboard-icon-amber" },
];

export default function DashboardPage() {
  return (
    <Box className="dashboard-page">
      <Stack direction="row" alignItems="center" spacing={1.5} className="dashboard-title-row">
        <Box className="page-title-icon"><DashboardRoundedIcon /></Box>
        <Box>
          <Typography variant="h4" component="h1" className="page-title">Inicio</Typography>
          <Typography color="text.secondary">Resumen general de FactuCore.</Typography>
        </Box>
      </Stack>
      <Typography color="text.secondary" className="dashboard-intro">
        Selecciona una opción del menú lateral para ingresar a cada módulo.
      </Typography>
      <Grid container spacing={2.5}>
        {summaries.map((item) => (
          <Grid key={item.label} size={{ xs: 12, sm: 6, lg: 4 }}>
            <Paper variant="outlined" className="dashboard-metric">
              <Stack direction="row" alignItems="center" justifyContent="space-between">
                <Typography color="text.secondary" fontWeight={650}>{item.label}</Typography>
                <Box className={`dashboard-metric-icon ${item.className}`}>{item.icon}</Box>
              </Stack>
              <Typography className="dashboard-metric-value">{item.value}</Typography>
              <Typography variant="caption" color="text.secondary">{item.helper}</Typography>
            </Paper>
          </Grid>
        ))}
      </Grid>
      <Paper variant="outlined" className="dashboard-welcome">
        <Typography variant="h6" className="dashboard-welcome-title">Bienvenido a FactuCore</Typography>
        <Typography color="text.secondary" className="dashboard-welcome-description">
          El encabezado, el menú lateral y esta área de contenido forman la estructura base del portal.
        </Typography>
        <Chip component={RouterLink} clickable to="/app/empresas" icon={<BusinessRoundedIcon />} label="Ir a Empresas" color="primary" variant="outlined" className="dashboard-company-link" />
      </Paper>
    </Box>
  );
}
