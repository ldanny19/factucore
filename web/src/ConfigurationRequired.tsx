import { Alert, Box, Container, Typography } from "@mui/material";
export default function ConfigurationRequired({ message }: { message: string }) {
  return (
    <Container maxWidth="md">
      <Box sx={{ py: 8 }}>
        <Typography variant="h4" component="h1" gutterBottom>
          FactuCore no está configurado
        </Typography>
        <Alert severity="error">{message}</Alert>
        <Typography color="text.secondary" sx={{ mt: 2 }}>
          Revisa configuraciones/web/config/config.js y vuelve a cargar la página.
        </Typography>
      </Box>
    </Container>
  );
}
