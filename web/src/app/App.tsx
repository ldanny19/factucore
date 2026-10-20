import { useEffect, useState } from "react";
import {
  AppBar,
  Box,
  Button,
  Chip,
  Container,
  Divider,
  Grid,
  Paper,
  Stack,
  Toolbar,
  Typography,
} from "@mui/material";
import ArrowForwardRoundedIcon from "@mui/icons-material/ArrowForwardRounded";
import ApiRoundedIcon from "@mui/icons-material/ApiRounded";
import AutorenewRoundedIcon from "@mui/icons-material/AutorenewRounded";
import CheckCircleRoundedIcon from "@mui/icons-material/CheckCircleRounded";
import CodeRoundedIcon from "@mui/icons-material/CodeRounded";
import DescriptionRoundedIcon from "@mui/icons-material/DescriptionRounded";
import EastRoundedIcon from "@mui/icons-material/EastRounded";
import FactCheckRoundedIcon from "@mui/icons-material/FactCheckRounded";
import InsightsRoundedIcon from "@mui/icons-material/InsightsRounded";
import IntegrationInstructionsRoundedIcon from "@mui/icons-material/IntegrationInstructionsRounded";
import LockRoundedIcon from "@mui/icons-material/LockRounded";
import ReceiptLongRoundedIcon from "@mui/icons-material/ReceiptLongRounded";
import ReplayRoundedIcon from "@mui/icons-material/ReplayRounded";
import SecurityRoundedIcon from "@mui/icons-material/SecurityRounded";
import TimelineRoundedIcon from "@mui/icons-material/TimelineRounded";
import VerifiedRoundedIcon from "@mui/icons-material/VerifiedRounded";
import { Link as RouterLink } from "react-router-dom";

const capabilities = [
  {
    title: "Facturación electrónica",
    description:
      "Generación y gestión centralizada de comprobantes electrónicos desde un contrato canónico.",
    icon: <ReceiptLongRoundedIcon />,
    color: "#2563eb",
    background: "#eaf1ff",
  },
  {
    title: "Validación y firma",
    description:
      "Validación documental y firma electrónica dentro de un flujo controlado y trazable.",
    icon: <FactCheckRoundedIcon />,
    color: "#0f9f8f",
    background: "#e5f8f4",
  },
  {
    title: "Integración con el SRI",
    description:
      "Envío, consulta y seguimiento de la recepción y autorización de los comprobantes.",
    icon: <IntegrationInstructionsRoundedIcon />,
    color: "#7c3aed",
    background: "#f1eaff",
  },
  {
    title: "Trazabilidad y auditoría",
    description:
      "Consulta de estados, historial de eventos y evidencias asociadas a cada documento.",
    icon: <TimelineRoundedIcon />,
    color: "#d97706",
    background: "#fff3df",
  },
  {
    title: "Reprocesamiento controlado",
    description:
      "Reintentos y continuidad del flujo sin repetir innecesariamente etapas completadas.",
    icon: <ReplayRoundedIcon />,
    color: "#2563eb",
    background: "#eaf1ff",
  },
  {
    title: "RIDE y documentos",
    description:
      "Preparación de la representación impresa y disponibilidad de documentos generados.",
    icon: <DescriptionRoundedIcon />,
    color: "#0f9f8f",
    background: "#e5f8f4",
  },
];

const invoices = [
  {
    number: "FAC-001284",
    customer: "Comercial Andina",
    date: "19 oct 2026",
    amount: "$ 248,50",
    status: "Autorizado",
    color: "#0f9f6e",
    background: "#e7f8ef",
  },
  {
    number: "FAC-001283",
    customer: "Distribuidora del Pacífico",
    date: "19 oct 2026",
    amount: "$ 516,00",
    status: "Pendiente",
    color: "#b7791f",
    background: "#fff5df",
  },
  {
    number: "NCR-000142",
    customer: "Servicios Integrales",
    date: "19 oct 2026",
    amount: "$ 75,20",
    status: "Con incidencia",
    color: "#c24141",
    background: "#fff0f0",
  },
];

function MetricCard({
  label,
  value,
  helper,
  icon,
  color,
  background,
}: {
  label: string;
  value: string;
  helper: string;
  icon: React.ReactNode;
  color: string;
  background: string;
}) {
  return (
    <Paper
      variant="outlined"
      sx={{
        p: 2.2,
        height: "100%",
        borderColor: "#e6ebf3",
        borderRadius: 3,
        boxShadow: "0 4px 18px rgba(25, 45, 85, 0.035)",
      }}
    >
      <Stack direction="row" alignItems="center" justifyContent="space-between" spacing={1}>
        <Typography variant="body2" color="text.secondary" fontWeight={600}>
          {label}
        </Typography>
        <Box
          sx={{
            width: 38,
            height: 38,
            display: "grid",
            placeItems: "center",
            color,
            backgroundColor: background,
            borderRadius: 2.5,
          }}
        >
          {icon}
        </Box>
      </Stack>
      <Typography sx={{ mt: 1.3, fontSize: "2rem", lineHeight: 1.1, fontWeight: 800, color: "#172b4d" }}>
        {value}
      </Typography>
      <Typography variant="caption" color="text.secondary">
        {helper}
      </Typography>
    </Paper>
  );
}

export default function App() {
  const [currentTime, setCurrentTime] = useState(() => new Date());

  useEffect(() => {
    const intervalId = window.setInterval(() => setCurrentTime(new Date()), 1000);
    return () => window.clearInterval(intervalId);
  }, []);

  const timeLabel = new Intl.DateTimeFormat("es-EC", {
    hour: "2-digit",
    minute: "2-digit",
    second: "2-digit",
    hour12: false,
  }).format(currentTime);

  const dateLabel = new Intl.DateTimeFormat("es-EC", {
    day: "2-digit",
    month: "short",
    year: "numeric",
  }).format(currentTime);

  return (
    <Box sx={{ minHeight: "100vh", color: "#172b4d", background: "#fbfcff" }}>
      <AppBar
        position="sticky"
        color="transparent"
        elevation={0}
        sx={{
          borderBottom: "1px solid #e8edf5",
          backgroundColor: "rgba(255,255,255,0.94)",
          backdropFilter: "blur(14px)",
        }}
      >
        <Toolbar sx={{ width: "100%", maxWidth: 1240, mx: "auto", px: { xs: 2, md: 3 }, py: 1 }}>
          <Box
            sx={{
              width: 44,
              height: 44,
              borderRadius: 2.5,
              display: "grid",
              placeItems: "center",
              background: "linear-gradient(135deg, #2563eb, #0ea5a4)",
              color: "white",
              mr: 1.5,
              boxShadow: "0 7px 16px rgba(37,99,235,.2)",
            }}
          >
            <ReceiptLongRoundedIcon />
          </Box>
          <Box>
            <Typography sx={{ fontWeight: 850, fontSize: "1.25rem", lineHeight: 1.15, color: "#173765" }}>
              FactuCore
            </Typography>
            <Typography sx={{ fontSize: ".68rem", letterSpacing: 1.05, fontWeight: 700, color: "#70809a" }}>
              FACTURACIÓN ELECTRÓNICA
            </Typography>
          </Box>
          <Box sx={{ flexGrow: 1 }} />
          <Stack
            direction="row"
            spacing={1}
            alignItems="center"
            sx={{ mr: { xs: 1, sm: 2 }, display: { xs: "none", sm: "flex" } }}
          >
            <Box sx={{ width: 7, height: 7, borderRadius: "50%", bgcolor: "#0f9f6e" }} />
            <Typography variant="caption" sx={{ color: "#526783", fontWeight: 600 }}>
              {dateLabel} · {timeLabel}
            </Typography>
          </Stack>
          <Button
            component={RouterLink}
            to="/login"
            variant="contained"
            disableElevation
            endIcon={<EastRoundedIcon />}
            sx={{
              borderRadius: 2.5,
              px: 2.3,
              py: 1.1,
              textTransform: "none",
              fontWeight: 750,
              background: "linear-gradient(100deg, #2563eb, #315ee8)",
              boxShadow: "0 7px 18px rgba(37,99,235,.18)",
            }}
          >
            Iniciar sesión
          </Button>
        </Toolbar>
      </AppBar>

      <Box
        component="main"
        sx={{
          overflow: "hidden",
          background:
            "radial-gradient(ellipse at 80% 6%, rgba(14,165,164,.09), transparent 34%), radial-gradient(ellipse at 8% 8%, rgba(37,99,235,.08), transparent 32%), #fbfcff",
        }}
      >
        <Container maxWidth="lg" sx={{ pt: { xs: 7, md: 11 }, pb: { xs: 7, md: 10 } }}>
          <Grid container spacing={{ xs: 5, md: 7 }} alignItems="center">
            <Grid size={{ xs: 12, md: 7 }}>
              <Chip
                icon={<InsightsRoundedIcon />}
                label="GESTIÓN TRIBUTARIA INTELIGENTE"
                sx={{
                  mb: 2.5,
                  pl: 0.5,
                  fontWeight: 800,
                  letterSpacing: 0.55,
                  color: "#2855bd",
                  backgroundColor: "#eaf1ff",
                  border: "1px solid #dce7ff",
                }}
              />
              <Typography
                component="h1"
                sx={{
                  maxWidth: 760,
                  fontSize: { xs: "2.7rem", sm: "3.5rem", md: "4.15rem" },
                  lineHeight: 1.06,
                  letterSpacing: "-.055em",
                  fontWeight: 850,
                  color: "#173765",
                }}
              >
                La facturación electrónica que{" "}
                <Box
                  component="span"
                  sx={{
                    background: "linear-gradient(95deg, #2563eb 5%, #0f9f9a 90%)",
                    backgroundClip: "text",
                    WebkitBackgroundClip: "text",
                    WebkitTextFillColor: "transparent",
                  }}
                >
                  se adapta a tu negocio.
                </Box>
              </Typography>
              <Typography
                sx={{
                  mt: 2.5,
                  maxWidth: 660,
                  color: "#5d6d85",
                  fontSize: { xs: "1.05rem", md: "1.17rem" },
                  lineHeight: 1.85,
                }}
              >
                Integra tu ERP, POS o sistema propio mediante una API REST genérica. Centraliza tus
                comprobantes y controla todo el proceso tributario desde una sola plataforma.
              </Typography>
              <Stack direction="row" flexWrap="wrap" useFlexGap gap={1} sx={{ mt: 3.2 }}>
                {[
                  "Independiente del ERP",
                  "API REST genérica",
                  "Control centralizado",
                ].map((label) => (
                  <Chip
                    key={label}
                    icon={<CheckCircleRoundedIcon />}
                    label={label}
                    variant="outlined"
                    sx={{
                      borderColor: "#dce5f1",
                      color: "#405674",
                      backgroundColor: "#fff",
                      fontWeight: 650,
                      "& .MuiChip-icon": { color: "#0f9f8f" },
                    }}
                  />
                ))}
              </Stack>
              <Stack direction="row" spacing={1.2} alignItems="center" sx={{ mt: 4, color: "#70809a" }}>
                <LockRoundedIcon fontSize="small" />
                <Typography variant="body2">Un flujo centralizado, auditable y preparado para crecer.</Typography>
              </Stack>
            </Grid>
            <Grid size={{ xs: 12, md: 5 }}>
              <Paper
                elevation={0}
                sx={{
                  p: { xs: 2.5, md: 3.2 },
                  borderRadius: 5,
                  border: "1px solid #e4eaf4",
                  background: "linear-gradient(145deg, #ffffff, #f5f9ff)",
                  boxShadow: "0 24px 70px rgba(39,74,132,.11)",
                  position: "relative",
                }}
              >
                <Box
                  sx={{
                    position: "absolute",
                    width: 92,
                    height: 92,
                    borderRadius: "50%",
                    right: -20,
                    top: -20,
                    background: "rgba(14,165,164,.12)",
                    filter: "blur(2px)",
                    zIndex: 0,
                  }}
                />
                <Stack direction="row" alignItems="center" justifyContent="space-between" sx={{ position: "relative" }}>
                  <Box>
                    <Typography variant="overline" sx={{ color: "#71819a", fontWeight: 800, letterSpacing: 1 }}>
                      VISIÓN CENTRALIZADA
                    </Typography>
                    <Typography variant="h6" sx={{ color: "#173765", fontWeight: 800 }}>
                      Tu operación, conectada
                    </Typography>
                  </Box>
                  <Box sx={{ width: 44, height: 44, borderRadius: 3, display: "grid", placeItems: "center", color: "#2563eb", bgcolor: "#eaf1ff" }}>
                    <ApiRoundedIcon fontSize="large" />
                  </Box>
                </Stack>
                <Divider sx={{ my: 2.5, borderColor: "#e8edf5" }} />
                <Stack spacing={1.4}>
                  {[
                    { label: "Recepción de datos", detail: "Contrato REST unificado", icon: <CodeRoundedIcon />, color: "#2563eb", bg: "#eaf1ff" },
                    { label: "Validación y procesamiento", detail: "Reglas y flujo controlado", icon: <SecurityRoundedIcon />, color: "#0f9f8f", bg: "#e5f8f4" },
                    { label: "Seguimiento del comprobante", detail: "Estados y evidencias", icon: <TimelineRoundedIcon />, color: "#7c3aed", bg: "#f1eaff" },
                  ].map((step, index) => (
                    <Stack key={step.label} direction="row" spacing={1.5} alignItems="center">
                      <Box sx={{ width: 42, height: 42, borderRadius: 2.5, display: "grid", placeItems: "center", flexShrink: 0, color: step.color, bgcolor: step.bg }}>
                        {step.icon}
                      </Box>
                      <Box sx={{ flexGrow: 1 }}>
                        <Typography sx={{ fontWeight: 750, color: "#263e60" }}>{step.label}</Typography>
                        <Typography variant="body2" sx={{ color: "#74839a" }}>{step.detail}</Typography>
                      </Box>
                      {index === 2 && <VerifiedRoundedIcon sx={{ color: "#0f9f8f" }} />}
                    </Stack>
                  ))}
                </Stack>
                <Box sx={{ mt: 2.5, px: 2, py: 1.6, borderRadius: 3, backgroundColor: "#eaf8f4", display: "flex", gap: 1.2, alignItems: "center" }}>
                  <CheckCircleRoundedIcon sx={{ color: "#0f9f8f" }} />
                  <Typography variant="body2" sx={{ color: "#236a60", fontWeight: 700 }}>
                    Un solo punto de control para tu facturación
                  </Typography>
                </Box>
              </Paper>
            </Grid>
          </Grid>
        </Container>

        <Box component="section" sx={{ py: { xs: 7, md: 9 }, backgroundColor: "#fff", borderTop: "1px solid #edf1f7", borderBottom: "1px solid #edf1f7" }}>
          <Container maxWidth="lg">
            <Stack spacing={1.2} alignItems="center" sx={{ textAlign: "center", mb: 5 }}>
              <Typography variant="overline" sx={{ color: "#2563eb", letterSpacing: 1.4, fontWeight: 850 }}>
                PLATAFORMA DE FACTURACIÓN
              </Typography>
              <Typography component="h2" sx={{ fontSize: { xs: "2rem", md: "2.7rem" }, fontWeight: 850, letterSpacing: "-.04em", color: "#173765" }}>
                Tu sistema se conecta. FactuCore gestiona el resto.
              </Typography>
              <Typography sx={{ maxWidth: 720, color: "#65758c", lineHeight: 1.8 }}>
                Conecta tu sistema actual mediante una API REST y centraliza la gestión electrónica sin depender de integraciones particulares para cada ERP.
              </Typography>
            </Stack>

            <Paper
              variant="outlined"
              sx={{
                maxWidth: 1020,
                mx: "auto",
                p: { xs: 2, md: 4 },
                borderRadius: 4,
                borderColor: "#e3eaf5",
                background: "linear-gradient(120deg, #f8faff, #f4fbfa)",
              }}
            >
              <Grid container spacing={1.5} alignItems="stretch">
                <Grid size={{ xs: 12, md: 4 }}>
                  <Box sx={{ height: "100%", minHeight: 150, p: 2.5, borderRadius: 3, border: "1px solid #dce7fb", backgroundColor: "#fff", display: "flex", flexDirection: "column", justifyContent: "center", alignItems: "center", textAlign: "center" }}>
                    <Stack direction="row" spacing={1} sx={{ mb: 1.3 }}>
                      <Box sx={{ width: 40, height: 40, borderRadius: 2.5, display: "grid", placeItems: "center", color: "#2563eb", bgcolor: "#eaf1ff" }}><CodeRoundedIcon /></Box>
                      <Box sx={{ width: 40, height: 40, borderRadius: 2.5, display: "grid", placeItems: "center", color: "#0f9f8f", bgcolor: "#e5f8f4" }}><IntegrationInstructionsRoundedIcon /></Box>
                    </Stack>
                    <Typography sx={{ color: "#24436e", fontWeight: 800, fontSize: "1.1rem" }}>ERP / POS / Sistema propio</Typography>
                    <Typography variant="body2" sx={{ mt: 0.5, color: "#70809a" }}>El sistema de tu negocio</Typography>
                  </Box>
                </Grid>
                <Grid size={{ xs: 12, md: 1 }} sx={{ display: "flex", justifyContent: "center", alignItems: "center" }}>
                  <ArrowForwardRoundedIcon sx={{ display: { xs: "none", md: "block" }, fontSize: 34, color: "#7b91b3" }} />
                  <Box sx={{ display: { xs: "flex", md: "none" }, py: 0.5 }}><AutorenewRoundedIcon sx={{ fontSize: 28, color: "#7b91b3" }} /></Box>
                </Grid>
                <Grid size={{ xs: 12, md: 3 }}>
                  <Box sx={{ height: "100%", minHeight: 150, p: 2.5, borderRadius: 3, border: "1px solid #bfe9e1", backgroundColor: "#e9faf6", display: "flex", flexDirection: "column", justifyContent: "center", alignItems: "center", textAlign: "center" }}>
                    <ApiRoundedIcon sx={{ fontSize: 37, color: "#0f9f8f", mb: 1 }} />
                    <Typography sx={{ color: "#08786f", fontWeight: 850, fontSize: "1.15rem" }}>API REST</Typography>
                    <Typography variant="body2" sx={{ mt: 0.5, color: "#427b76" }}>Contrato genérico y versionable</Typography>
                  </Box>
                </Grid>
                <Grid size={{ xs: 12, md: 1 }} sx={{ display: "flex", justifyContent: "center", alignItems: "center" }}>
                  <ArrowForwardRoundedIcon sx={{ display: { xs: "none", md: "block" }, fontSize: 34, color: "#7b91b3" }} />
                  <Box sx={{ display: { xs: "flex", md: "none" }, py: 0.5 }}><AutorenewRoundedIcon sx={{ fontSize: 28, color: "#7b91b3" }} /></Box>
                </Grid>
                <Grid size={{ xs: 12, md: 3 }}>
                  <Box sx={{ height: "100%", minHeight: 150, p: 2.5, borderRadius: 3, background: "linear-gradient(145deg, #2563eb, #3157c9)", display: "flex", flexDirection: "column", justifyContent: "center", alignItems: "center", textAlign: "center", boxShadow: "0 10px 24px rgba(37,99,235,.18)" }}>
                    <ReceiptLongRoundedIcon sx={{ fontSize: 37, color: "#fff", mb: 1 }} />
                    <Typography sx={{ color: "#fff", fontWeight: 850, fontSize: "1.15rem" }}>FactuCore</Typography>
                    <Typography variant="body2" sx={{ mt: 0.5, color: "#e3ecff" }}>Motor de facturación electrónica</Typography>
                  </Box>
                </Grid>
              </Grid>
              <Stack direction="row" justifyContent="center" alignItems="center" spacing={1} sx={{ mt: 2.5 }}>
                <CheckCircleRoundedIcon sx={{ color: "#0f9f8f", fontSize: 19 }} />
                <Typography variant="body2" sx={{ textAlign: "center", color: "#526783", fontWeight: 650 }}>
                  Sin adaptadores específicos por ERP: cada cliente envía el contrato de FactuCore.
                </Typography>
              </Stack>
            </Paper>
          </Container>
        </Box>

        <Box component="section" sx={{ py: { xs: 7, md: 9 } }}>
          <Container maxWidth="lg">
            <Grid container spacing={4} alignItems="end" sx={{ mb: 3.5 }}>
              <Grid size={{ xs: 12, md: 8 }}>
                <Typography variant="overline" sx={{ color: "#0f9f8f", fontWeight: 850, letterSpacing: 1.4 }}>
                  CONTROL DE TU OPERACIÓN
                </Typography>
                <Typography component="h2" sx={{ mt: 0.7, fontSize: { xs: "2rem", md: "2.7rem" }, fontWeight: 850, letterSpacing: "-.04em", color: "#173765" }}>
                  Una vista clara de tus comprobantes.
                </Typography>
                <Typography sx={{ mt: 1.2, maxWidth: 720, color: "#65758c", lineHeight: 1.8 }}>
                  Consulta el estado de emisión, autorización e incidencias desde un mismo lugar.
                </Typography>
              </Grid>
              <Grid size={{ xs: 12, md: 4 }}>
                <Chip icon={<InsightsRoundedIcon />} label="Vista demostrativa · Datos ficticios" sx={{ fontWeight: 700, color: "#526783", bgcolor: "#eef3fb" }} />
              </Grid>
            </Grid>

            <Paper variant="outlined" sx={{ p: { xs: 2, md: 3 }, borderRadius: 4, borderColor: "#e3eaf4", backgroundColor: "#fff", boxShadow: "0 14px 40px rgba(25,45,85,.055)" }}>
              <Stack direction={{ xs: "column", sm: "row" }} alignItems={{ xs: "flex-start", sm: "center" }} justifyContent="space-between" spacing={1} sx={{ mb: 2.5 }}>
                <Box>
                  <Typography variant="h6" sx={{ fontWeight: 800, color: "#203c64" }}>Resumen de comprobantes</Typography>
                  <Typography variant="body2" sx={{ color: "#74839a" }}>Actividad reciente de la plataforma</Typography>
                </Box>
                <Chip size="small" icon={<CheckCircleRoundedIcon />} label="Estados de ejemplo" sx={{ color: "#08786f", bgcolor: "#e5f8f4", fontWeight: 700 }} />
              </Stack>
              <Grid container spacing={2}>
                <Grid size={{ xs: 12, sm: 6, md: 3 }}>
                  <MetricCard label="Emitidos" value="1.284" helper="Comprobantes procesados" icon={<ReceiptLongRoundedIcon />} color="#2563eb" background="#eaf1ff" />
                </Grid>
                <Grid size={{ xs: 12, sm: 6, md: 3 }}>
                  <MetricCard label="Autorizados" value="1.231" helper="Autorización confirmada" icon={<VerifiedRoundedIcon />} color="#0f9f8f" background="#e5f8f4" />
                </Grid>
                <Grid size={{ xs: 12, sm: 6, md: 3 }}>
                  <MetricCard label="Pendientes" value="38" helper="En espera de respuesta" icon={<AutorenewRoundedIcon />} color="#b7791f" background="#fff3df" />
                </Grid>
                <Grid size={{ xs: 12, sm: 6, md: 3 }}>
                  <MetricCard label="Incidencias" value="15" helper="Requieren revisión" icon={<SecurityRoundedIcon />} color="#c24141" background="#fff0f0" />
                </Grid>
              </Grid>

              <Box sx={{ mt: 3.2 }}>
                <Typography sx={{ mb: 1.5, color: "#203c64", fontWeight: 800 }}>Comprobantes recientes</Typography>
                <Box sx={{ overflowX: "auto" }}>
                  <Box sx={{ minWidth: 650 }}>
                    <Grid container sx={{ px: 1.5, py: 1.2, bgcolor: "#f5f8fc", borderRadius: 2, mb: 0.5 }}>
                      <Grid size={3}><Typography variant="caption" sx={{ color: "#74839a", fontWeight: 800 }}>COMPROBANTE</Typography></Grid>
                      <Grid size={3}><Typography variant="caption" sx={{ color: "#74839a", fontWeight: 800 }}>CLIENTE</Typography></Grid>
                      <Grid size={2}><Typography variant="caption" sx={{ color: "#74839a", fontWeight: 800 }}>FECHA</Typography></Grid>
                      <Grid size={2}><Typography variant="caption" sx={{ color: "#74839a", fontWeight: 800 }}>TOTAL</Typography></Grid>
                      <Grid size={2}><Typography variant="caption" sx={{ color: "#74839a", fontWeight: 800 }}>ESTADO</Typography></Grid>
                    </Grid>
                    {invoices.map((invoice) => (
                      <Grid container key={invoice.number} alignItems="center" sx={{ px: 1.5, py: 1.7, borderBottom: "1px solid #edf1f7" }}>
                        <Grid size={3}><Typography sx={{ color: "#315ca8", fontWeight: 750, fontSize: ".9rem" }}>{invoice.number}</Typography></Grid>
                        <Grid size={3}><Typography sx={{ color: "#405674", fontSize: ".9rem" }}>{invoice.customer}</Typography></Grid>
                        <Grid size={2}><Typography sx={{ color: "#74839a", fontSize: ".85rem" }}>{invoice.date}</Typography></Grid>
                        <Grid size={2}><Typography sx={{ color: "#263e60", fontWeight: 750, fontSize: ".9rem" }}>{invoice.amount}</Typography></Grid>
                        <Grid size={2}>
                          <Chip size="small" label={invoice.status} sx={{ color: invoice.color, backgroundColor: invoice.background, fontWeight: 750, fontSize: ".72rem" }} />
                        </Grid>
                      </Grid>
                    ))}
                  </Box>
                </Box>
              </Box>
            </Paper>
          </Container>
        </Box>

        <Box component="section" sx={{ py: { xs: 7, md: 9 }, backgroundColor: "#fff", borderTop: "1px solid #edf1f7", borderBottom: "1px solid #edf1f7" }}>
          <Container maxWidth="lg">
            <Stack spacing={1.2} alignItems="center" sx={{ textAlign: "center", mb: 5 }}>
              <Typography variant="overline" sx={{ color: "#7c3aed", letterSpacing: 1.4, fontWeight: 850 }}>
                CAPACIDADES PRINCIPALES
              </Typography>
              <Typography component="h2" sx={{ fontSize: { xs: "2rem", md: "2.7rem" }, fontWeight: 850, letterSpacing: "-.04em", color: "#173765" }}>
                Todo lo que necesitas para facturar.
              </Typography>
              <Typography sx={{ maxWidth: 700, color: "#65758c", lineHeight: 1.8 }}>
                Desde la recepción de datos hasta la autorización y el respaldo documental, con el proceso bajo control.
              </Typography>
            </Stack>
            <Grid container spacing={2.2}>
              {capabilities.map((capability) => (
                <Grid key={capability.title} size={{ xs: 12, sm: 6, md: 4 }}>
                  <Paper
                    variant="outlined"
                    sx={{
                      p: 2.8,
                      height: "100%",
                      minHeight: 205,
                      borderRadius: 3.5,
                      borderColor: "#e6ebf3",
                      transition: "transform 180ms ease, box-shadow 180ms ease",
                      "&:hover": { transform: "translateY(-4px)", boxShadow: "0 14px 28px rgba(25,45,85,.07)" },
                    }}
                  >
                    <Box sx={{ width: 46, height: 46, display: "grid", placeItems: "center", borderRadius: 2.7, color: capability.color, backgroundColor: capability.background, mb: 2 }}>
                      {capability.icon}
                    </Box>
                    <Typography sx={{ color: "#203c64", fontSize: "1.05rem", fontWeight: 800, mb: 1 }}>
                      {capability.title}
                    </Typography>
                    <Typography variant="body2" sx={{ color: "#65758c", lineHeight: 1.8 }}>
                      {capability.description}
                    </Typography>
                  </Paper>
                </Grid>
              ))}
            </Grid>
          </Container>
        </Box>
      </Box>

      <Box component="footer" sx={{ py: 5, backgroundColor: "#f3f7fc" }}>
        <Container maxWidth="lg">
          <Grid container spacing={3} alignItems="center">
            <Grid size={{ xs: 12, md: 5 }}>
              <Stack direction="row" spacing={1.4} alignItems="center">
                <Box sx={{ width: 42, height: 42, borderRadius: 2.5, display: "grid", placeItems: "center", color: "#fff", background: "linear-gradient(135deg, #2563eb, #0ea5a4)" }}>
                  <ReceiptLongRoundedIcon />
                </Box>
                <Box>
                  <Typography sx={{ fontWeight: 850, color: "#173765" }}>FactuCore</Typography>
                  <Typography variant="body2" sx={{ color: "#74839a" }}>Desarrollado por Ing. Danny Lamilla</Typography>
                </Box>
              </Stack>
            </Grid>
            <Grid size={{ xs: 12, md: 4 }}>
              <Typography sx={{ fontWeight: 800, color: "#203c64", mb: 0.5 }}>Contacto y soporte</Typography>
              <Typography variant="body2" sx={{ color: "#65758c" }}>¿Necesitas ayuda con la plataforma?</Typography>
              <Typography component="a" href="mailto:ldanny_19@hotmail.com" sx={{ display: "inline-block", mt: 0.4, color: "#2563eb", fontWeight: 700, textDecoration: "none" }}>
                ldanny_19@hotmail.com
              </Typography>
            </Grid>
            <Grid size={{ xs: 12, md: 3 }}>
              <Button component={RouterLink} to="/login" variant="outlined" endIcon={<EastRoundedIcon />} sx={{ textTransform: "none", fontWeight: 750, borderRadius: 2.5 }}>
                Iniciar sesión
              </Button>
            </Grid>
          </Grid>
          <Divider sx={{ my: 3, borderColor: "#e0e8f2" }} />
          <Typography variant="caption" sx={{ color: "#8190a5" }}>
            © 2026 FactuCore. Todos los derechos reservados.
          </Typography>
        </Container>
      </Box>
    </Box>
  );
}
