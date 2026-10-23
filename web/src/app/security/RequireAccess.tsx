import { Alert, Box, Button, CircularProgress, Stack, Typography } from "@mui/material";
import { useEffect, useState, type ReactNode } from "react";
import { Navigate, useLocation } from "react-router-dom";
import { MessageCodes } from "../../shared/messages/MessageCodes";
import { resolveMessage } from "../../shared/messages/MessageResolver";
import { getAccessContext, type AppRole } from "../../shared/security/authorization";
import {
  clearTokenSession,
  hasRefreshToken,
  renewAccessToken,
  SESSION_UPDATED_EVENT,
  TokenRenewalError,
} from "../../shared/infrastructure/auth/token-session";

interface RequireAccessProps {
  children: ReactNode;
  allowedRoles: readonly AppRole[];
}

function AccessDenied() {
  return (
    <Box sx={{ maxWidth: 640, mx: "auto", py: { xs: 5, md: 9 } }}>
      <Stack spacing={2} alignItems="flex-start">
        <Alert severity="warning" sx={{ width: "100%" }}>
          <Typography component="div" fontWeight={750}>
            {resolveMessage(MessageCodes.ACCESS_DENIED_TITLE)}
          </Typography>
          {resolveMessage(MessageCodes.ACCESS_DENIED_DESCRIPTION)}
        </Alert>
        <Button href="/" variant="contained">{resolveMessage(MessageCodes.ACCESS_DENIED_HOME)}</Button>
      </Stack>
    </Box>
  );
}

export default function RequireAccess({ children, allowedRoles }: RequireAccessProps) {
  const location = useLocation();
  const context = getAccessContext();
  const [tokenRevision, setTokenRevision] = useState(0);
  const [refreshing, setRefreshing] = useState(false);
  const [refreshFailed, setRefreshFailed] = useState(false);
  const refreshTokenAvailable = hasRefreshToken();

  useEffect(() => {
    const handleSessionUpdate = () => setTokenRevision((revision) => revision + 1);
    window.addEventListener(SESSION_UPDATED_EVENT, handleSessionUpdate);
    return () => window.removeEventListener(SESSION_UPDATED_EVENT, handleSessionUpdate);
  }, []);

  useEffect(() => {
    if (context.isTokenValid || !refreshTokenAvailable) return;

    let active = true;
    setRefreshing(true);
    setRefreshFailed(false);
    renewAccessToken()
      .then(() => {
        if (active) {
          setTokenRevision((revision) => revision + 1);
          setRefreshing(false);
        }
      })
      .catch((error: unknown) => {
        if (!active) return;
        if (error instanceof TokenRenewalError && error.invalidGrant) {
          clearTokenSession();
        }
        setRefreshFailed(true);
        setRefreshing(false);
      });

    return () => {
      active = false;
    };
  }, [context.isTokenValid, refreshTokenAvailable, tokenRevision]);

  if (!context.isTokenValid) {
    if (refreshTokenAvailable) {
      if (refreshing || !refreshFailed) {
        return <Box sx={{ minHeight: "40vh", display: "grid", placeItems: "center" }}><CircularProgress /></Box>;
      }
      return (
        <Box sx={{ maxWidth: 640, mx: "auto", py: { xs: 5, md: 9 } }}>
          <Stack spacing={2}>
            <Alert severity="error">{resolveMessage(MessageCodes.SESSION_RENEWAL_FAILED)}</Alert>
            <Button variant="contained" onClick={() => setTokenRevision((revision) => revision + 1)}>
              {resolveMessage(MessageCodes.SESSION_RETRY)}
            </Button>
          </Stack>
        </Box>
      );
    }
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  }

  if (!context.roles.some((role) => allowedRoles.includes(role))) {
    return <AccessDenied />;
  }

  return <>{children}</>;
}
