import { useEffect, useState } from "react";
import {
  Alert,
  Box,
  Button,
  Container,
  Paper,
  Stack,
  Typography,
} from "@mui/material";
import DescriptionOutlinedIcon from "@mui/icons-material/DescriptionOutlined";
import VerifiedUserOutlinedIcon from "@mui/icons-material/VerifiedUserOutlined";
import ArrowForwardRoundedIcon from "@mui/icons-material/ArrowForwardRounded";
import { Link as RouterLink, useNavigate } from "react-router-dom";
import { useAuth } from "react-oidc-context";
import { MessageCodes } from "../../../shared/messages/MessageCodes";
import { resolveMessage } from "../../../shared/messages/MessageResolver";
import "../../../styles/auth.css";

export default function LoginPage() {
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const auth = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (!auth.isLoading && auth.isAuthenticated) {
      navigate("/app", { replace: true });
    }
  }, [auth.isAuthenticated, auth.isLoading, navigate]);

  const handleLogin = async () => {
    setError(null);
    setIsSubmitting(true);
    try {
      await auth.signinRedirect();
    } catch {
      setError(resolveMessage(MessageCodes.LOGIN_SERVICE_UNAVAILABLE));
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
                Inicia sesión con tu cuenta corporativa para continuar a FactuCore.
              </Typography>

              {error && <Alert severity="error" onClose={() => setError(null)} className="auth-error">{error}</Alert>}
              {auth.error && (
                <Alert severity="error" className="auth-error">
                  {resolveMessage(MessageCodes.LOGIN_AUTHENTICATION_FAILED)}
                </Alert>
              )}

              <Stack spacing={2.5}>
                <Button
                  onClick={() => void handleLogin()}
                  variant="contained"
                  fullWidth
                  size="large"
                  disabled={isSubmitting || auth.isLoading}
                  endIcon={<ArrowForwardRoundedIcon />}
                  className="auth-submit-button"
                >
                  {isSubmitting ? "Redirigiendo..." : "Iniciar sesión"}
                </Button>
                <Button component={RouterLink} to="/" variant="text" fullWidth>
                  Volver al inicio
                </Button>
              </Stack>
            </Box>
          </Box>
        </Paper>
      </Container>
    </Box>
  );
}
