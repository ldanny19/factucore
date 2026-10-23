import type { ReactNode } from "react";
import { CssBaseline, ThemeProvider, createTheme } from "@mui/material";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { BrowserRouter } from "react-router-dom";
import { AuthProvider } from "react-oidc-context";
import { userManager } from "../shared/infrastructure/auth/user-manager";
import SessionLifecycle from "./session/SessionLifecycle";
import DependenciesProvider from "./DependenciesProvider";

const queryClient = new QueryClient({
  defaultOptions: { queries: { retry: 1, refetchOnWindowFocus: false } },
});

const theme = createTheme({
  palette: {
    mode: "light",
    primary: {
      main: "#12304a",
      light: "#244d6c",
      dark: "#0a1f33",
      contrastText: "#ffffff",
    },
    secondary: {
      main: "#1976d2",
      light: "#64a8f0",
      dark: "#115293",
      contrastText: "#ffffff",
    },
    background: { default: "#f3f6fa", paper: "#ffffff" },
    text: { primary: "#172b3a", secondary: "#5d6b78" },
    divider: "#e1e7ee",
  },
  shape: { borderRadius: 12 },
  typography: {
    fontSize: 13,
    fontFamily: '"Inter", "Roboto", "Helvetica", "Arial", sans-serif',
    button: { fontWeight: 600 },
    h4: { letterSpacing: "-0.025em" },
  },
  components: {
    MuiButton: {
      styleOverrides: {
        root: {
          borderRadius: 10,
          textTransform: "none",
          fontWeight: 600,
          minHeight: 36,
          padding: "6px 14px",
          boxShadow: "none",
        },
        containedPrimary: {
          "&:hover": {
            backgroundColor: "#0a2438",
            boxShadow: "none",
          },
        },
        outlinedPrimary: {
          borderColor: "#cbd6e0",
          "&:hover": {
            borderColor: "#12304a",
            backgroundColor: "rgba(18, 48, 74, 0.04)",
          },
        },
      },
    },
    MuiTextField: {
      defaultProps: {
        size: "small",
      },
    },
    MuiOutlinedInput: {
      styleOverrides: {
        root: {
          borderRadius: 10,
          backgroundColor: "#ffffff",
          "& .MuiOutlinedInput-notchedOutline": {
            borderColor: "#d7e0e8",
          },
          "&:hover .MuiOutlinedInput-notchedOutline": {
            borderColor: "#8ca0b2",
          },
          "&.Mui-focused .MuiOutlinedInput-notchedOutline": {
            borderWidth: 1.5,
          },
        },
      },
    },
  },
});

export default function AppProviders({
  children,
}: {
  children: ReactNode;
}) {
  return (
    <AuthProvider userManager={userManager}>
      <ThemeProvider theme={theme}>
        <CssBaseline />
        <QueryClientProvider client={queryClient}>
          <DependenciesProvider>
            <BrowserRouter>
              <SessionLifecycle>{children}</SessionLifecycle>
            </BrowserRouter>
          </DependenciesProvider>
        </QueryClientProvider>
      </ThemeProvider>
    </AuthProvider>
  );
}
