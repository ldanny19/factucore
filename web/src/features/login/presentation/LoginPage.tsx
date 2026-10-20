import { Alert, Button, Container, Link, Paper, Stack, TextField, Typography } from "@mui/material";
import { Link as RouterLink } from "react-router-dom";
import { useState } from "react";
export default function LoginPage() {
  const [error, setError] = useState<string | null>(null);
  return (
    <Container maxWidth="sm" sx={{ py: { xs: 5, md: 9 } }}>
      <Paper variant="outlined" sx={{ p: { xs: 3, md: 5 } }}>
        <Stack spacing={2.5}>
          <Typography variant="h4" component="h1" fontWeight={800} color="primary.main">
            Iniciar sesión
          </Typography>
          <Typography color="text.secondary">
            Ingresas tus credenciales para ingresar a FactuCore.
          </Typography>
          {error && <Alert severity="error">{error}</Alert>}
          <TextField label="Usuario" name="username" fullWidth autoComplete="username" required />

          <TextField
            label="Contraseña"
            name="password"
            type="password"
            fullWidth
            required
            autoComplete="current-password"
          />
          <Link component={RouterLink} to="/recuperar-contrasena" underline="hover">
            ¿Olvidaste tu contraseña?
          </Link>
          <Stack direction="row" spacing={2} justifyContent="center">
            <Button component={RouterLink} to="/" variant="contained">
              Iniciar
            </Button>
            <Button component={RouterLink} to="/" variant="contained">
              Volver al inicio
            </Button>
          </Stack>
        </Stack>
      </Paper>
    </Container>
  );
}
