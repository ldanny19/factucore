import { Alert, Box, Button, CircularProgress, Stack, Typography } from "@mui/material";
import type { ReactNode } from "react";
import { useAuth } from "react-oidc-context";
import { Navigate, useLocation } from "react-router-dom";
import { MessageCodes } from "../../shared/messages/MessageCodes";
import { resolveMessage } from "../../shared/messages/MessageResolver";
import { getAccessContext, type AppRole } from "../../shared/security/authorization";

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
  const auth = useAuth();
  const location = useLocation();
  const context = getAccessContext(auth.user?.access_token);

  if (auth.isLoading && !context.token && !auth.isAuthenticated) {
    return <Box sx={{ minHeight: "40vh", display: "grid", placeItems: "center" }}><CircularProgress /></Box>;
  }

  if (!context.isTokenValid) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  }

  if (!context.roles.some((role) => allowedRoles.includes(role))) {
    return <AccessDenied />;
  }

  return <>{children}</>;
}
