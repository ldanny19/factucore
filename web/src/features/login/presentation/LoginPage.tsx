import "../../../styles/auth.css";
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
import { MessageCodes } from "../../../shared/messages/MessageCodes";
import { resolveMessage } from "../../../shared/messages/MessageResolver";
import { useAuth } from "react-oidc-context";
import { oidcConfig } from "../infrastructure/oidc-config";

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
      // En desarrollo, Vite proxifica /auth para evitar que el navegador bloquee el token por CORS.
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
      let tokenResponse: { access_token?: string; error?: string; error_description?: string };
      try {
        tokenResponse = JSON.parse(responseBody) as typeof tokenResponse;
      } catch {
        tokenResponse = { error: "invalid_response" };
      }

      if (!response.ok || !tokenResponse.access_token) {
        setError(resolveMessage(MessageCodes.LOGIN_AUTHENTICATION_FAILED));
        return;
      }

      // Sesión temporal para la prueba del portal; no se almacena la contraseña.
      sessionStorage.setItem("factucore.access_token", tokenResponse.access_token);
      setPassword("");
      navigate("/app", { replace: true });
    } catch {
      setError(resolveMessage(MessageCodes.LOGIN_SERVICE_UNAVAILABLE));
    } finally {
      setIsSubmitting(false);
    }
  };

  const clearError = () => setError(null);

  return (
    <Box
      sx={{
        minHeight: "100vh",
        display: "grid",
        placeItems: "center",
        py: { xs: 3, md: 6 },
        background:
          "radial-gradient(circle at 12% 12%, rgba(25, 118, 210, 0.08), transparent 28%), #f3f6fa",
      }}
    >
      <Container maxWidth="lg">
        <Paper
          className="auth-login-card"
          elevation={0}
          sx={{
            width: "100%",
            maxWidth: 1040,
            mx: "auto",
            display: "grid",
            gridTemplateColumns: { xs: "1fr", md: "0.9fr 1.1fr" },
            overflow: "hidden",
            border: "1px solid #e1e7ee",
            borderRadius: { xs: 3, md: 5 },
            boxShadow: "0 24px 70px rgba(18, 48, 74, 0.12)",
          }}
        >
          <Box
            sx={{
              position: "relative",
              isolation: "isolate",
              overflow: "hidden",
              display: "flex",
              flexDirection: "column",
              justifyContent: "space-between",
              gap: 5,
              p: { xs: 3.5, sm: 5, md: 6 },
              minHeight: { xs: 220, md: 590 },
              color: "#ffffff",
              background: "linear-gradient(145deg, #0a1f33 0%, #12304a 58%, #1b5278 100%)",
              "&::before": {
                content: '""',
                position: "absolute",
                zIndex: -1,
                width: 290,
                height: 290,
                right: -110,
                top: -100,
                border: "1px solid rgba(255,255,255,0.14)",
                borderRadius: "50%",
                boxShadow: "0 0 0 35px rgba(255,255,255,0.035), 0 0 0 75px rgba(255,255,255,0.025)",
              },
              "&::after": {
                content: '""',
                position: "absolute",
                zIndex: -1,
                width: 190,
                height: 190,
                left: -95,
                bottom: -115,
                borderRadius: "50%",
                backgroundColor: "rgba(25, 118, 210, 0.18)",
              },
            }}
          >
            <Stack spacing={2.5}>
              <Box
                sx={{
                  width: 54,
                  height: 54,
                  display: "grid",
                  placeItems: "center",
                  borderRadius: 3,
                  color: "#ffffff",
                  backgroundColor: "rgba(255,255,255,0.12)",
                  border: "1px solid rgba(255,255,255,0.18)",
                }}
              >
                <DescriptionOutlinedIcon sx={{ fontSize: 30 }} />
              </Box>
              <Box>
                <Typography variant="h5" component="p" fontWeight={800} letterSpacing="-0.04em">
                  FactuCore
                </Typography>
                <Typography
                  sx={{
                    mt: 2.5,
                    maxWidth: 340,
                    fontSize: { xs: "1.65rem", md: "2.15rem" },
                    lineHeight: 1.2,
                    fontWeight: 750,
                    letterSpacing: "-0.04em",
                  }}
                >
                  Tu operación tributaria, en un solo lugar.
                </Typography>
                <Typography
                  sx={{
                    mt: 2,
                    maxWidth: 340,
                    color: "rgba(255,255,255,0.72)",
                    lineHeight: 1.8,
                  }}
                >
                  Gestiona tu facturación electrónica con una experiencia clara y organizada.
                </Typography>
              </Box>
            </Stack>

            <Stack direction="row" spacing={1.5} alignItems="center">
              <VerifiedUserOutlinedIcon sx={{ color: "#8fc8ff", fontSize: 22 }} />
              <Typography variant="body2" sx={{ color: "rgba(255,255,255,0.78)" }}>
                Portal empresarial FactuCore
              </Typography>
            </Stack>
          </Box>

          <Box
            sx={{
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              p: { xs: 3, sm: 5, md: 7 },
              backgroundColor: "#ffffff",
            }}
          >
            <Box sx={{ width: "100%", maxWidth: 390 }}>
              <Typography
                variant="overline"
                sx={{ color: "secondary.main", fontWeight: 800, letterSpacing: "0.12em" }}
              >
                PORTAL EMPRESARIAL
              </Typography>
              <Typography
                variant="h4"
                component="h1"
                sx={{ mt: 1, fontSize: { xs: "1.8rem", sm: "2.1rem" }, fontWeight: 800 }}
              >
                Bienvenido
              </Typography>
              <Typography color="text.secondary" sx={{ mt: 1, mb: 3.5, lineHeight: 1.7 }}>
                Ingresa tus credenciales para continuar a FactuCore.
              </Typography>

              {error && (
                <Alert severity="error" onClose={clearError} sx={{ mb: 2.5, borderRadius: 2 }}>
                  {error}
                </Alert>
              )}

              <Box component="form" onSubmit={handleSubmit} noValidate>
                <Stack spacing={2.5}>
                  <TextField
                    label="Usuario"
                    name="username"
                    value={username}
                    onChange={(event) => {
                      setUsername(event.target.value);
                      clearError();
                    }}
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
                    onChange={(event) => {
                      setPassword(event.target.value);
                      clearError();
                    }}
                    fullWidth
                    autoComplete="current-password"
                    required
                    disabled={isSubmitting}
                    slotProps={{
                      input: {
                        startAdornment: (
                          <LockOutlinedIcon
                            sx={{ mr: 1.25, color: "text.secondary", fontSize: 20 }}
                          />
                        ),
                      },
                    }}
                  />

                  <Box sx={{ display: "flex", justifyContent: "flex-end", mt: -0.5 }}>
                    <Link
                      component={RouterLink}
                      to="/recuperar-contrasena"
                      underline="hover"
                      sx={{ fontSize: "0.9rem", fontWeight: 600 }}
                    >
                      ¿Olvidaste tu contraseña?
                    </Link>
                  </Box>

                  <Button
                    type="submit"
                    variant="contained"
                    fullWidth
                    size="large"
                    disabled={isSubmitting}
                    sx={{ mt: 0.5, py: 1.4 }}
                  >
                    {isSubmitting ? "Validando..." : "Iniciar sesión"}
                  </Button>

                  <Button component={RouterLink} to="/" variant="text" color="primary" fullWidth>
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
