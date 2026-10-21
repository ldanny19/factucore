import { useCallback, useEffect, useRef, useState, type ReactNode } from "react";
import {
  Button,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  Stack,
  Typography,
} from "@mui/material";
import { useAuth } from "react-oidc-context";
import { useNavigate } from "react-router-dom";
import { MessageCodes } from "../../shared/messages/MessageCodes";
import { resolveMessage } from "../../shared/messages/MessageResolver";
import { getRuntimeConfig } from "../../config/runtime-config";

const runtimeConfig = getRuntimeConfig();
const INACTIVITY_TIMEOUT_MS = runtimeConfig.SESSION_INACTIVITY_TIMEOUT_MINUTES * 60 * 1000;
const TOKEN_WARNING_BEFORE_EXPIRY_MS = runtimeConfig.SESSION_TOKEN_WARNING_MINUTES * 60 * 1000;
const PROMPT_TIMEOUT_SECONDS = runtimeConfig.SESSION_PROMPT_TIMEOUT_SECONDS;
const ACCESS_TOKEN_KEY = "factucore.access_token";

type SessionLifecycleProps = {
  children: ReactNode;
};

export default function SessionLifecycle({ children }: SessionLifecycleProps) {
  const auth = useAuth();
  const navigate = useNavigate();
  const [dialogOpen, setDialogOpen] = useState(false);
  const [secondsLeft, setSecondsLeft] = useState(PROMPT_TIMEOUT_SECONDS);
  const [isRenewing, setIsRenewing] = useState(false);
  const lastActivityAtRef = useRef(Date.now());
  const dialogOpenRef = useRef(false);
  const logoutStartedRef = useRef(false);
  const warningTimerRef = useRef<number | undefined>(undefined);
  const expiryTimerRef = useRef<number | undefined>(undefined);
  const logoutTimerRef = useRef<number | undefined>(undefined);

  const clearSessionTimers = useCallback(() => {
    window.clearTimeout(warningTimerRef.current);
    window.clearTimeout(expiryTimerRef.current);
    window.clearInterval(logoutTimerRef.current);
  }, []);

  const endSession = useCallback(async () => {
    if (logoutStartedRef.current) return;
    logoutStartedRef.current = true;
    clearSessionTimers();
    dialogOpenRef.current = false;
    setDialogOpen(false);
    window.sessionStorage.removeItem(ACCESS_TOKEN_KEY);

    try {
      if (auth.user || auth.isAuthenticated) {
        await auth.signoutRedirect();
        return;
      }
    } catch {
      // Continue with local logout if the identity provider is unavailable.
    }

    await auth.removeUser();
    navigate("/login", { replace: true });
  }, [auth, clearSessionTimers, navigate]);

  const showWarning = useCallback(() => {
    if (logoutStartedRef.current || dialogOpenRef.current) return;
    dialogOpenRef.current = true;
    setSecondsLeft(PROMPT_TIMEOUT_SECONDS);
    setDialogOpen(true);
  }, []);

  const checkSessionDeadline = useCallback(() => {
    if (!auth.isAuthenticated || !auth.user || logoutStartedRef.current || dialogOpenRef.current) return;

    const now = Date.now();
    const inactivityDeadline = lastActivityAtRef.current + INACTIVITY_TIMEOUT_MS;
    const tokenDeadline =
      typeof auth.user.expires_at === "number" ? auth.user.expires_at * 1000 : now;
    const deadline = Math.min(inactivityDeadline, tokenDeadline);
    const remainingMs = deadline - now;

    window.clearTimeout(warningTimerRef.current);
    window.clearTimeout(expiryTimerRef.current);

    if (remainingMs <= 0) {
      void endSession();
      return;
    }

    const warningLeadMs = Math.min(TOKEN_WARNING_BEFORE_EXPIRY_MS, remainingMs);
    warningTimerRef.current = window.setTimeout(
      () => {
        const currentDeadline = Math.min(
          lastActivityAtRef.current + INACTIVITY_TIMEOUT_MS,
          typeof auth.user?.expires_at === "number" ? auth.user.expires_at * 1000 : 0,
        );
        if (currentDeadline <= Date.now()) {
          void endSession();
          return;
        }
        showWarning();
      },
      Math.max(0, remainingMs - warningLeadMs),
    );
    expiryTimerRef.current = window.setTimeout(() => {
      if (Date.now() >= deadline) void endSession();
      else checkSessionDeadline();
    }, remainingMs);
  }, [auth.isAuthenticated, auth.user, endSession, showWarning]);

  useEffect(() => {
    if (!auth.isAuthenticated || !auth.user) {
      clearSessionTimers();
      dialogOpenRef.current = false;
      setDialogOpen(false);
      return;
    }

    logoutStartedRef.current = false;
    lastActivityAtRef.current = Date.now();
    checkSessionDeadline();

    const handleActivity = () => {
      if (dialogOpenRef.current || logoutStartedRef.current) return;
      lastActivityAtRef.current = Date.now();
      checkSessionDeadline();
    };
    const handleVisibilityOrFocus = () => {
      if (document.visibilityState === "visible") checkSessionDeadline();
    };
    const activityEvents: (keyof WindowEventMap)[] = [
      "pointerdown",
      "keydown",
      "mousemove",
      "scroll",
      "touchstart",
    ];

    activityEvents.forEach((eventName) =>
      window.addEventListener(eventName, handleActivity, { passive: true }),
    );
    window.addEventListener("focus", handleVisibilityOrFocus);
    document.addEventListener("visibilitychange", handleVisibilityOrFocus);

    return () => {
      clearSessionTimers();
      activityEvents.forEach((eventName) =>
        window.removeEventListener(eventName, handleActivity),
      );
      window.removeEventListener("focus", handleVisibilityOrFocus);
      document.removeEventListener("visibilitychange", handleVisibilityOrFocus);
    };
  }, [auth.isAuthenticated, auth.user, checkSessionDeadline, clearSessionTimers]);

  useEffect(() => {
    if (!dialogOpen || isRenewing) {
      window.clearInterval(logoutTimerRef.current);
      return;
    }

    logoutTimerRef.current = window.setInterval(() => {
      setSecondsLeft((current) => {
        if (current <= 1) {
          window.clearInterval(logoutTimerRef.current);
          void endSession();
          return 0;
        }
        return current - 1;
      });
    }, 1000);

    return () => window.clearInterval(logoutTimerRef.current);
  }, [dialogOpen, endSession, isRenewing]);

  const continueSession = async () => {
    if (isRenewing || logoutStartedRef.current) return;
    setIsRenewing(true);
    try {
      const renewedUser = await auth.signinSilent();
      if (
        !renewedUser?.access_token ||
        typeof renewedUser.expires_at !== "number" ||
        renewedUser.expires_at * 1000 <= Date.now()
      ) {
        throw new Error("No se recibió un token renovado válido.");
      }

      window.sessionStorage.removeItem(ACCESS_TOKEN_KEY);
      lastActivityAtRef.current = Date.now();
      dialogOpenRef.current = false;
      setDialogOpen(false);
      setSecondsLeft(PROMPT_TIMEOUT_SECONDS);
    } catch {
      await endSession();
    } finally {
      setIsRenewing(false);
    }
  };

  return (
    <>
      {children}
      <Dialog
        open={dialogOpen}
        onClose={() => undefined}
        disableEscapeKeyDown
        aria-labelledby="session-expiry-title"
        aria-describedby="session-expiry-description"
        fullWidth
        maxWidth="xs"
      >
        <DialogTitle id="session-expiry-title">
          {resolveMessage(MessageCodes.SESSION_EXPIRING_TITLE)}
        </DialogTitle>
        <DialogContent>
          <Stack spacing={2}>
            <DialogContentText id="session-expiry-description">
              {resolveMessage(MessageCodes.SESSION_EXPIRING_DESCRIPTION)}
            </DialogContentText>
            <Typography variant="h4" textAlign="center" aria-live="polite">
              {Math.floor(secondsLeft / 60).toString().padStart(2, "0")}:
              {(secondsLeft % 60).toString().padStart(2, "0")}
            </Typography>
          </Stack>
        </DialogContent>
        <DialogActions sx={{ px: 3, pb: 3 }}>
          <Button onClick={() => void endSession()} color="inherit" disabled={isRenewing}>
            {resolveMessage(MessageCodes.SESSION_END)}
          </Button>
          <Button onClick={() => void continueSession()} variant="contained" disabled={isRenewing}>
            {isRenewing ? (
              <CircularProgress size={20} color="inherit" />
            ) : (
              resolveMessage(MessageCodes.SESSION_CONTINUE)
            )}
          </Button>
        </DialogActions>
      </Dialog>
    </>
  );
}
