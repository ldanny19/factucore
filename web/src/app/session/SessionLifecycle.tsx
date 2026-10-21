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

type SessionLifecycleProps = {
  children: ReactNode;
};

export default function SessionLifecycle({ children }: SessionLifecycleProps) {
  const auth = useAuth();
  const navigate = useNavigate();
  const [dialogOpen, setDialogOpen] = useState(false);
  const [secondsLeft, setSecondsLeft] = useState(PROMPT_TIMEOUT_SECONDS);
  const [isRenewing, setIsRenewing] = useState(false);
  const idleTimerRef = useRef<number | undefined>(undefined);
  const tokenWarningTimerRef = useRef<number | undefined>(undefined);
  const expiryTimerRef = useRef<number | undefined>(undefined);
  const logoutTimerRef = useRef<number | undefined>(undefined);
  const logoutStartedRef = useRef(false);

  const endSession = useCallback(async () => {
    if (logoutStartedRef.current) return;
    logoutStartedRef.current = true;
    setDialogOpen(false);
    window.sessionStorage.removeItem("factucore.access_token");

    try {
      if (auth.user || auth.isAuthenticated) {
        await auth.signoutRedirect();
        return;
      }
    } catch {
      // If the identity provider is unavailable, still close the local session.
    }

    await auth.removeUser();
    navigate("/login", { replace: true });
  }, [auth, navigate]);

  const showWarning = useCallback(() => {
    if (logoutStartedRef.current) return;
    setSecondsLeft(PROMPT_TIMEOUT_SECONDS);
    setDialogOpen(true);
  }, []);

  useEffect(() => {
    if (!auth.isAuthenticated || !auth.user) {
      window.clearTimeout(idleTimerRef.current);
      window.clearTimeout(tokenWarningTimerRef.current);
      window.clearTimeout(expiryTimerRef.current);
      return;
    }

    logoutStartedRef.current = false;
    const expiresAt = auth.user.expires_at;
    if (typeof expiresAt === "number") {
      const warningDelay = expiresAt * 1000 - Date.now() - TOKEN_WARNING_BEFORE_EXPIRY_MS;
      if (warningDelay <= 0) {
        if (expiresAt * 1000 <= Date.now()) {
          void endSession();
        } else {
          showWarning();
        }
      } else {
        tokenWarningTimerRef.current = window.setTimeout(showWarning, warningDelay);
      }

      expiryTimerRef.current = window.setTimeout(
        () => {
          if (Date.now() >= expiresAt * 1000) void endSession();
        },
        Math.max(0, expiresAt * 1000 - Date.now()),
      );
    }

    const resetIdleTimer = () => {
      if (dialogOpen || logoutStartedRef.current) return;
      window.clearTimeout(idleTimerRef.current);
      idleTimerRef.current = window.setTimeout(showWarning, INACTIVITY_TIMEOUT_MS);
    };

    const activityEvents: (keyof WindowEventMap)[] = [
      "pointerdown",
      "keydown",
      "mousemove",
      "scroll",
      "touchstart",
    ];
    activityEvents.forEach((eventName) =>
      window.addEventListener(eventName, resetIdleTimer, { passive: true }),
    );
    resetIdleTimer();

    return () => {
      window.clearTimeout(idleTimerRef.current);
      window.clearTimeout(tokenWarningTimerRef.current);
      window.clearTimeout(expiryTimerRef.current);
      activityEvents.forEach((eventName) =>
        window.removeEventListener(eventName, resetIdleTimer),
      );
    };
  }, [auth.isAuthenticated, auth.user, dialogOpen, endSession, showWarning]);

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
    setIsRenewing(true);
    try {
      const renewedUser = await auth.signinSilent();
      if (
        !renewedUser?.access_token ||
        typeof renewedUser.expires_at !== "number" ||
        renewedUser.expires_at * 1000 <= Date.now()
      ) {
        throw new Error("Session renewal did not return a valid token.");
      }

      window.sessionStorage.removeItem("factucore.access_token");
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
