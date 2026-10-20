import { useEffect, useState, type FormEvent } from "react";
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
import DescriptionOutlinedIcon from "@mui/icons-material/DescriptionOutlined";
import LockOutlinedIcon from "@mui/icons-material/LockOutlined";
import VerifiedUserOutlinedIcon from "@mui/icons-material/VerifiedUserOutlined";
import { Link as RouterLink, useNavigate } from "react-router-dom";
import { useAuth } from "react-oidc-context";
import { MessageCodes } from "../../../shared/messages/MessageCodes";
import { resolveMessage } from "../../../shared/messages/MessageResolver";
import { oidcConfig } from "../infrastructure/oidc-config";
import "../../../styles/auth.css";

export default function LoginPage() {
  const [error, setError] = useState<string | null>(null);
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const auth = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (!auth.isLoading && auth.isAuthenticated) {
      navigate("/app", { replace: true });
    }
  }, [auth.isAuthenticated, auth.isLoading, navigate]);

  const clearError = () => setError(null);

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError(null);

    if (!username.trim() || !password) {
      setError(resolveMessage(MessageCodes.LOGIN_REQUIRED_FIELDS));
      return;
    }

    setIsSubmitting(true);
    try {
      const authority = new URL(oidcConfig.authority);
      const tokenPath = `${authority.pathname.replace(/\/$/, "")}/protocol/openid-connect/token`;
      const tokenEndpoint = import.meta.env.DEV ? tokenPath : `${authority.origin}${tokenPath}`;
      const credentials = new URLSearchParams({
        grant_type: "password",
        client_id: oidcConfig.client_id,
        username: username.trim(),
        password,
        scope: "openid profile email",
      });
      const response = await fetch(tokenEndpoint, {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: credentials,
      });
      const responseBody = await response.text();
      let tokenResponse: { access_token?: string };
      try {
        tokenResponse = JSON.parse(responseBody) as typeof tokenResponse;
      } catch {
        tokenResponse = {};
      }

      if (!response.ok || !tokenResponse.access_token) {
        setError(resolveMessage(MessageCodes.LOGIN_AUTHENTICATION_FAILED));
        return;
      }

      sessionStorage.setItem("factucore.access_token", tokenResponse.access_token);
      setPassword("");
      navigate("/app", { replace: true });
    } catch {
      setError(resolveMessage(MessageCodes.LOGIN_SERVICE_UNAVAILABLE));
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <Box className="auth-page auth-login-page">
      <Container maxWidth="lg" className="auth-container">
        <Paper className="auth-login-card" elevation={0}>
          <Box className="auth-login-brand-panel">
            <Stack className="auth-brand-content" spacing={3}>
              <Box className="auth-brand-icon"><DescriptionOutlinedIcon /></Box>
              <Box>
                <Typography className="auth-brand-name" component="p">FactuCore</Typography>
                <Typography className="auth-brand-heading" component="h2">
                  Tu operación tributaria, en un solo lugar.
                </Typography>
                <Typography className="auth-brand-description">
                  Gestiona tu facturación electrónica con una experiencia clara y organizada.
                </Typography>
              </Box>
            </Stack>
            <Box className="auth-brand-footer">
              <VerifiedUserOutlinedIcon />
              <Typography variant="body2">Portal empresarial FactuCore</Typography>
            </Box>
          </Box>

          <Box className="auth-login-form-panel">
            <Box className="auth-login-form">
              <Typography className="auth-eyebrow">PORTAL EMPRESARIAL</Typography>
              <Typography className="auth-login-title" component="h1">Bienvenido</Typography>
              <Typography className="auth-login-description">
                Ingresa tus credenciales para continuar a FactuCore.
              </Typography>

              {error && <Alert severity="error" onClose={clearError} className="auth-error">{error}</Alert>}

              <Box component="form" onSubmit={handleSubmit} noValidate>
                <Stack spacing={2.5}>
                  <TextField
                    label="Usuario"
                    name="username"
                    value={username}
                    onChange={(event) => { setUsername(event.target.value); clearError(); }}
                    fullWidth
                    autoComplete="username"
                    required
                    disabled={isSubmitting}
                  />
                  <TextField
                    label="Contraseña"
                    name="password"
                    type="password"
                    value={password}
                    onChange={(event) => { setPassword(event.target.value); clearError(); }}
                    fullWidth
                    autoComplete="current-password"
                    required
                    disabled={isSubmitting}
                    slotProps={{ input: { startAdornment: <LockOutlinedIcon className="auth-password-icon" /> } }}
                  />
                  <Box className="auth-forgot-link">
                    <Link component={RouterLink} to="/recuperar-contrasena" underline="hover">
                      ¿Olvidaste tu contraseña?
                    </Link>
                  </Box>
                  <Button type="submit" variant="contained" fullWidth size="large" disabled={isSubmitting} className="auth-submit-button">
                    {isSubmitting ? "Validando..." : "Iniciar sesión"}
                  </Button>
                  <Button component={RouterLink} to="/" variant="text" fullWidth>
                    Volver al inicio
                  </Button>
                </Stack>
              </Box>
            </Box>
          </Box>
        </Paper>
      </Container>
    </Box>
  );
}
