import {
  AppBar,
  Box,
  Container,
  Paper,
  Toolbar,
  Typography,
} from "@mui/material";

export default function App() {
  return (
    <Box sx={{ minHeight: "100vh", bgcolor: "background.default" }}>
      <AppBar position="static" elevation={0}>
        <Toolbar>
          <Typography variant="h6" component="div" sx={{ fontWeight: 700 }}>
            FactuCore
          </Typography>
          <Box sx={{ flexGrow: 1 }} />
          <Typography variant="body2">Facturación electrónica</Typography>
        </Toolbar>
      </AppBar>

      <Container maxWidth="lg" sx={{ py: 5 }}>
        <Paper variant="outlined" sx={{ p: { xs: 3, md: 4 } }}>
          <Typography variant="h4" component="h1" gutterBottom>
            Frontend inicial
          </Typography>
          <Typography color="text.secondary">
            La base de React y TypeScript está preparada. Los módulos de
            administración, navegación y autenticación se incorporarán en las
            siguientes tareas.
          </Typography>
        </Paper>
      </Container>
    </Box>
  );
}
