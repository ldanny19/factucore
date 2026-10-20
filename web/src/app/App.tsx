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
import BusinessOutlinedIcon from "@mui/icons-material/BusinessOutlined";
import ReceiptLongOutlinedIcon from "@mui/icons-material/ReceiptLongOutlined";
import SecurityOutlinedIcon from "@mui/icons-material/SecurityOutlined";
import { Link as RouterLink } from "react-router-dom";
const items = [
  {
    title: "Empresas",
    description: "Administra la información de las empresas y sus datos generales.",
    icon: <BusinessOutlinedIcon fontSize="large" />,
    status: "Estructura preparada",
  },
  {
    title: "Comprobantes",
    description: "Punto de entrada para la gestión de documentos electrónicos.",
    icon: <ReceiptLongOutlinedIcon fontSize="large" />,
    status: "Próxima etapa",
  },
  {
    title: "Acceso seguro",
    description: "La autenticación se integrará mediante Keycloak y OpenID Connect.",
    icon: <SecurityOutlinedIcon fontSize="large" />,
    status: "Pendiente de validar OIDC",
  },
];
export default function App() {
  return (
    <Box sx={{ minHeight: "100vh", bgcolor: "background.default" }}>
      <AppBar
        position="static"
        color="transparent"
        elevation={0}
        sx={{ borderBottom: 1, borderColor: "divider", bgcolor: "background.paper" }}
      >
        <Toolbar sx={{ maxWidth: 1200, width: "100%", mx: "auto", py: 1 }}>
          <Box
            sx={{
              display: "grid",
              placeItems: "center",
              width: 42,
              height: 42,
              borderRadius: 2,
              bgcolor: "primary.main",
              color: "common.white",
              fontWeight: 800,
              mr: 1.5,
            }}
          >
            F
          </Box>
          <Box>
            <Typography variant="h6" color="primary.main" fontWeight={800}>
              FactuCore
            </Typography>
            <Typography variant="caption" color="text.secondary">
              Facturación electrónica empresarial
            </Typography>
          </Box>
          <Box sx={{ flexGrow: 1 }} />
          <Button component={RouterLink} to="/login" color="inherit">
            Iniciar sesión
          </Button>
          <Button component={RouterLink} to="/empresas" variant="contained" sx={{ ml: 1 }}>
            Empresas
          </Button>
        </Toolbar>
      </AppBar>
      <Container maxWidth="lg" sx={{ py: { xs: 5, md: 9 } }}>
        <Stack spacing={3} sx={{ maxWidth: 780, mb: 6 }}>
          <Chip
            label="PLATAFORMA DE FACTURACIÓN ELECTRÓNICA"
            color="primary"
            variant="outlined"
            sx={{ alignSelf: "flex-start", fontWeight: 700, letterSpacing: 0.6 }}
          />
          <Typography
            component="h1"
            sx={{
              fontSize: { xs: "2.5rem", md: "4rem" },
              lineHeight: 1.08,
              fontWeight: 800,
              letterSpacing: "-0.04em",
              color: "primary.main",
            }}
          >
            Tu operación tributaria, en un solo lugar.
          </Typography>
          <Typography variant="h6" color="text.secondary" sx={{ fontWeight: 400, maxWidth: 690 }}>
            Una base central para administrar empresas y preparar el flujo de comprobantes
            electrónicos de FactuCore.
          </Typography>
          <Stack direction={{ xs: "column", sm: "row" }} spacing={1.5} sx={{ pt: 1 }}>
            <Button
              component={RouterLink}
              to="/login"
              variant="contained"
              size="large"
              endIcon={<SecurityOutlinedIcon />}
            >
              Iniciar sesión
            </Button>
            <Button
              component={RouterLink}
              to="/empresas"
              variant="outlined"
              size="large"
              startIcon={<BusinessOutlinedIcon />}
            >
              Explorar módulo empresas
            </Button>
          </Stack>
        </Stack>
        <Grid container spacing={2.5}>
          {items.map((item) => (
            <Grid key={item.title} size={{ xs: 12, md: 4 }}>
              <Paper
                variant="outlined"
                sx={{
                  p: 3,
                  height: "100%",
                  borderColor: "divider",
                  transition: "transform 160ms ease, box-shadow 160ms ease",
                  "&:hover": { transform: "translateY(-3px)", boxShadow: 3 },
                }}
              >
                <Box sx={{ color: "secondary.main", mb: 2 }}>{item.icon}</Box>
                <Typography variant="h6" fontWeight={700} gutterBottom>
                  {item.title}
                </Typography>
                <Typography color="text.secondary" sx={{ minHeight: 72, mb: 2 }}>
                  {item.description}
                </Typography>
                <Chip label={item.status} size="small" variant="outlined" />
              </Paper>
            </Grid>
          ))}
        </Grid>
        <Typography variant="body2" color="text.secondary" sx={{ mt: 6, textAlign: "center" }}>
          FactuCore · Frontend inicial · Entorno de desarrollo
        </Typography>
      </Container>
    </Box>
  );
}
