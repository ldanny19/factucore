import { useState, type FormEvent } from "react";
import { Alert, Box, Button, Container, Paper, Stack, TextField, Typography } from "@mui/material";
import ArrowBackRoundedIcon from "@mui/icons-material/ArrowBackRounded";
import EmailOutlinedIcon from "@mui/icons-material/EmailOutlined";
import LockResetRoundedIcon from "@mui/icons-material/LockResetRounded";
import { Link as RouterLink } from "react-router-dom";
import { oidcConfig } from "../../../shared/infrastructure/auth/oidc-config";
import "../../../styles/auth.css";

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
    <Box className="auth-page">
      <Container maxWidth="sm">
        <Paper className="auth-recovery-card" elevation={0}>
          <Stack spacing={2.5} alignItems="flex-start">
            <Box className="auth-recovery-icon">
              <LockResetRoundedIcon />
            </Box>
            <Box>
              <Typography variant="overline" color="primary.main" fontWeight={800}>
                PORTAL EMPRESARIAL
              </Typography>
              <Typography className="auth-recovery-title" component="h1" variant="h4" fontWeight={800}>
                Recuperar contraseña
              </Typography>
              <Typography color="text.secondary" className="auth-recovery-description">
                Ingresa el correo electrónico asociado a tu cuenta para continuar con la recuperación.
              </Typography>
            </Box>
            {error && <Alert severity="error" className="auth-error">{error}</Alert>}
            <Box component="form" onSubmit={handleSubmit} className="auth-recovery-form">
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
                      startAdornment: <EmailOutlinedIcon className="auth-password-icon" />,
                    },
                  }}
                />
                <Button
                  type="submit"
                  variant="contained"
                  sx={{ alignSelf: "flex-start" }}
                >
                  Continuar
                </Button>
                <Button
                  component={RouterLink}
                  to="/login"
                  variant="text"
                  startIcon={<ArrowBackRoundedIcon />}
                  sx={{ alignSelf: "flex-start" }}
                >
                  Volver al inicio de sesión
                </Button>
              </Stack>
            </Box>
          </Stack>
        </Paper>
      </Container>
    </Box>
  );
}
