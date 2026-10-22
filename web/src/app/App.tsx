import { useEffect, useState } from "react";
import {
  AppBar,
  Box,
  Button,
  Chip,
  Container,
  Grid,
  Paper,
  Stack,
  Toolbar,
  Typography,
} from "@mui/material";
import ApiRoundedIcon from "@mui/icons-material/ApiRounded";
import ArrowForwardRoundedIcon from "@mui/icons-material/ArrowForwardRounded";
import CheckCircleRoundedIcon from "@mui/icons-material/CheckCircleRounded";
import CodeRoundedIcon from "@mui/icons-material/CodeRounded";
import FactCheckRoundedIcon from "@mui/icons-material/FactCheckRounded";
import LockRoundedIcon from "@mui/icons-material/LockRounded";
import ReceiptLongRoundedIcon from "@mui/icons-material/ReceiptLongRounded";
import SecurityRoundedIcon from "@mui/icons-material/SecurityRounded";
import TimelineRoundedIcon from "@mui/icons-material/TimelineRounded";
import { Link as RouterLink } from "react-router-dom";
import "../styles/home.css";

const capabilities = [
  { title: "Facturación electrónica", description: "Generación y gestión centralizada de comprobantes mediante un contrato canónico.", icon: <ReceiptLongRoundedIcon />, className: "home-capability-blue" },
  { title: "Validación y firma", description: "Validación documental y firma electrónica dentro de un flujo controlado.", icon: <FactCheckRoundedIcon />, className: "home-capability-green" },
  { title: "Integración con el SRI", description: "Envío, consulta y seguimiento de la recepción y autorización de comprobantes.", icon: <SecurityRoundedIcon />, className: "home-capability-purple" },
];

const processSteps = [
  { title: "Recepción de datos", description: "Contrato REST unificado", icon: <CodeRoundedIcon /> },
  { title: "Validación y procesamiento", description: "Reglas y flujo controlado", icon: <SecurityRoundedIcon /> },
  { title: "Seguimiento del comprobante", description: "Estados y evidencias", icon: <TimelineRoundedIcon /> },
];

export default function App() {
  const [currentTime, setCurrentTime] = useState(() => new Date());

  useEffect(() => {
    const intervalId = window.setInterval(() => setCurrentTime(new Date()), 1000);
    return () => window.clearInterval(intervalId);
  }, []);

  const dateLabel = new Intl.DateTimeFormat("es-EC", {
    day: "2-digit", month: "short", year: "numeric",
    hour: "2-digit", minute: "2-digit", hour12: false,
  }).format(currentTime);

  return (
    <Box className="home-page">
      <AppBar position="sticky" elevation={0} className="home-header">
        <Toolbar className="home-toolbar">
          <Box className="home-brand">
            <Box className="home-brand-icon"><img src="/factucore-icon.png" alt="" /></Box>
            <Box>
              <Typography className="home-brand-name">Factu<span>Core</span></Typography>
              <Typography className="home-brand-caption">FACTURACIÓN ELECTRÓNICA</Typography>
            </Box>
          </Box>
          <Box className="home-header-spacer" />
          <Typography className="home-date">{dateLabel}</Typography>
          <Button component={RouterLink} to="/login" variant="contained" endIcon={<ArrowForwardRoundedIcon />} className="home-login-button">
            Iniciar sesión
          </Button>
        </Toolbar>
      </AppBar>

      <Box component="main">
        <Container maxWidth="lg" className="home-hero-container">
          <Grid container spacing={6} alignItems="center">
            <Grid size={{ xs: 12, md: 7 }}>
              <Chip icon={<ApiRoundedIcon />} label="GESTIÓN TRIBUTARIA CENTRALIZADA" className="home-eyebrow-chip" />
              <Typography component="h1" className="home-hero-title">
                La facturación electrónica que <Box component="span" className="home-gradient-text">se adapta a tu negocio.</Box>
              </Typography>
              <Typography className="home-hero-description">
                Integra tu ERP, POS o sistema propio mediante una API REST genérica. Centraliza tus comprobantes y controla el proceso tributario desde una sola plataforma.
              </Typography>
              <Stack direction="row" flexWrap="wrap" useFlexGap gap={1} className="home-benefits">
                {["Independiente del ERP", "API REST genérica", "Control centralizado"].map((label) => (
                  <Chip key={label} icon={<CheckCircleRoundedIcon />} label={label} variant="outlined" className="home-benefit-chip" />
                ))}
              </Stack>
              <Stack direction="row" spacing={1.2} alignItems="center" className="home-security-note">
                <LockRoundedIcon fontSize="small" />
                <Typography variant="body2">Un flujo centralizado, auditable y preparado para crecer.</Typography>
              </Stack>
            </Grid>
            <Grid size={{ xs: 12, md: 5 }}>
              <Paper elevation={0} className="home-process-card">
                <Box className="home-process-heading">
                  <Box>
                    <Typography className="home-card-eyebrow">VISIÓN CENTRALIZADA</Typography>
                    <Typography className="home-process-title">Tu operación, conectada</Typography>
                  </Box>
                  <Box className="home-process-icon"><ApiRoundedIcon /></Box>
                </Box>
                <Stack spacing={2} className="home-process-steps">
                  {processSteps.map((step, index) => (
                    <Stack key={step.title} direction="row" spacing={1.5} alignItems="center" className="home-process-step">
                      <Box className={`home-step-icon home-step-icon-${index + 1}`}>{step.icon}</Box>
                      <Box className="home-step-content">
                        <Typography className="home-step-title">{step.title}</Typography>
                        <Typography className="home-step-description">{step.description}</Typography>
                      </Box>
                    </Stack>
                  ))}
                </Stack>
                <Box className="home-process-footer"><CheckCircleRoundedIcon /><Typography>Un solo punto de control para tu facturación</Typography></Box>
              </Paper>
            </Grid>
          </Grid>
        </Container>

        <Box component="section" className="home-capabilities-section">
          <Container maxWidth="lg">
            <Box className="home-section-heading">
              <Typography className="home-section-eyebrow">PLATAFORMA DE FACTURACIÓN</Typography>
              <Typography component="h2" className="home-section-title">Tu sistema se conecta. FactuCore gestiona el resto.</Typography>
              <Typography className="home-section-description">
                Conecta tu sistema mediante una API REST y centraliza la gestión electrónica sin depender de integraciones particulares para cada ERP.
              </Typography>
            </Box>
            <Grid container spacing={2.5}>
              {capabilities.map((item) => (
                <Grid key={item.title} size={{ xs: 12, md: 4 }}>
                  <Paper variant="outlined" className={`home-capability-card ${item.className}`}>
                    <Box className="home-capability-icon">{item.icon}</Box>
                    <Typography component="h3" className="home-capability-title">{item.title}</Typography>
                    <Typography className="home-capability-description">{item.description}</Typography>
                  </Paper>
                </Grid>
              ))}
            </Grid>
          </Container>
        </Box>
      </Box>
      <Box component="footer" className="home-footer">
        <Container maxWidth="lg" className="home-footer-content">
          <Typography>© {currentTime.getFullYear()} FactuCore · Plataforma de facturación electrónica</Typography>
          <Typography>Seguridad · Trazabilidad · Integración</Typography>
        </Container>
      </Box>
    </Box>
  );
}
