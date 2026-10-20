import { useState, type FormEvent } from "react";
import {
  Alert,
  Box,
  Button,
  Container,
  Link,
  Paper,
  Stack,
  TextField,
  Typography,
} from "@mui/material";
import ArrowBackRoundedIcon from "@mui/icons-material/ArrowBackRounded";
import EmailOutlinedIcon from "@mui/icons-material/EmailOutlined";
import LockResetRoundedIcon from "@mui/icons-material/LockResetRounded";
import { Link as RouterLink } from "react-router-dom";
import { oidcConfig } from "../../login/infrastructure/oidc-config";

export default function RecuperarContrasenaPage() {
  const [email, setEmail] = useState("");
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError(null);
    const normalizedEmail = email.trim();

    if (!normalizedEmail || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(normalizedEmail)) {
      setError("Ingresa un correo electrónico válido.");
      return;
    }

    // La recuperación y el envío del correo los administra Keycloak.
    // login_hint permite precargar el correo en su pantalla de autenticación.
    const authority = new URL(oidcConfig.authority);
    const authorizationUrl = new URL(
      `${authority.pathname.replace(/\/$/, "")}/protocol/openid-connect/auth`,
      authority.origin,
    );
    authorizationUrl.searchParams.set("client_id", oidcConfig.client_id);
    authorizationUrl.searchParams.set("redirect_uri", oidcConfig.redirect_uri);
    authorizationUrl.searchParams.set("response_type", "code");
    authorizationUrl.searchParams.set("scope", "openid profile email");
    authorizationUrl.searchParams.set("login_hint", normalizedEmail);
    window.location.assign(authorizationUrl.toString());
  };

  return (
    <Box
      sx={{
        minHeight: "100vh",
        display: "grid",
        placeItems: "center",
        py: 3,
        px: 2,
        background:
          "radial-gradient(circle at 12% 12%, rgba(25, 118, 210, 0.08), transparent 28%), #f3f6fa",
      }}
    >
      <Container maxWidth="sm">
        <Paper
          elevation={0}
          sx={{
            p: { xs: 3, sm: 5 },
            border: "1px solid #e1e7ee",
            borderRadius: 4,
            boxShadow: "0 24px 70px rgba(18, 48, 74, 0.10)",
          }}
        >
          <Stack spacing={2.5} alignItems="flex-start">
            <Box
              sx={{
                width: 54,
                height: 54,
                display: "grid",
                placeItems: "center",
                borderRadius: 3,
                color: "primary.main",
                bgcolor: "action.hover",
              }}
            >
              <LockResetRoundedIcon sx={{ fontSize: 30 }} />
            </Box>
            <Box>
              <Typography variant="overline" color="primary.main" fontWeight={800}>
                PORTAL EMPRESARIAL
              </Typography>
              <Typography component="h1" variant="h4" fontWeight={800} sx={{ mt: 0.5 }}>
                Recuperar contraseña
              </Typography>
              <Typography color="text.secondary" sx={{ mt: 1.5, lineHeight: 1.8 }}>
                Ingresa el correo electrónico asociado a tu cuenta. Continuarás con el servicio seguro
                de Keycloak para solicitar el enlace de restablecimiento.
              </Typography>
            </Box>

            {error && <Alert severity="error" sx={{ width: "100%" }}>{error}</Alert>}

            <Box component="form" onSubmit={handleSubmit} sx={{ width: "100%" }}>
              <Stack spacing={2.5}>
                <TextField
                  label="Correo electrónico"
                  name="email"
                  type="email"
                  value={email}
                  onChange={(event) => {
                    setEmail(event.target.value);
                    setError(null);
                  }}
                  autoComplete="email"
                  required
                  fullWidth
                  autoFocus
                  slotProps={{
                    input: {
                      startAdornment: <EmailOutlinedIcon sx={{ mr: 1.25, color: "text.secondary" }} />,
                    },
                  }}
                />
                <Alert severity="info">
                  En la pantalla de Keycloak, selecciona «¿Olvidaste tu contraseña?» si no aparece
                  automáticamente. Keycloak verificará la cuenta y gestionará el envío del correo.
                </Alert>
                <Button type="submit" variant="contained" size="large" fullWidth>
                  Continuar
                </Button>
                <Button
                  component={RouterLink}
                  to="/login"
                  variant="text"
                  fullWidth
                  startIcon={<ArrowBackRoundedIcon />}
                >
                  Volver al inicio de sesión
                </Button>
              </Stack>
            </Box>
            <Typography variant="caption" color="text.secondary">
              Por seguridad, FactuCore no solicita ni almacena tu contraseña actual.
            </Typography>
            <Link component={RouterLink} to="/login" underline="hover" sx={{ fontSize: ".9rem" }}>
              Volver a iniciar sesión
            </Link>
          </Stack>
        </Paper>
      </Container>
    </Box>
  );
}
