import type { ReactNode } from "react";
import { CssBaseline, ThemeProvider, createTheme } from "@mui/material";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { BrowserRouter } from "react-router-dom";
const queryClient = new QueryClient({
  defaultOptions: { queries: { retry: 1, refetchOnWindowFocus: false } },
});
const theme = createTheme({
  palette: {
    mode: "light",
    primary: { main: "#12304a" },
    secondary: { main: "#1976d2" },
    background: { default: "#f4f6f8", paper: "#fff" },
  },
  shape: { borderRadius: 10 },
  typography: { fontFamily: '"Inter", "Roboto", "Helvetica", "Arial", sans-serif' },
});
export default function AppProviders({ children }: { children: ReactNode }) {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <QueryClientProvider client={queryClient}>
        <BrowserRouter>{children}</BrowserRouter>
      </QueryClientProvider>
    </ThemeProvider>
  );
}
