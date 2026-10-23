import { useCallback, useEffect, useRef, useState, type ReactNode } from "react";
import {
  Button,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  Alert,
  Stack,
  Typography,
} from "@mui/material";
import { useNavigate } from "react-router-dom";
import { MessageCodes } from "../../shared/messages/MessageCodes";
import { resolveMessage } from "../../shared/messages/MessageResolver";
import { getRuntimeConfig } from "../../config/runtime-config";
import {
  clearTokenSession,
  getAccessTokenRenewalDelayMs,
  hasTokenSession,
  renewAccessToken,
  SESSION_UPDATED_EVENT,
  TokenRenewalError,
} from "../../shared/infrastructure/auth/token-session";

const runtimeConfig = getRuntimeConfig();
const INACTIVITY_TIMEOUT_MS = runtimeConfig.SESSION_INACTIVITY_TIMEOUT_MINUTES * 60 * 1000;
const INACTIVITY_WARNING_BEFORE_END_MS =
  runtimeConfig.SESSION_TOKEN_WARNING_MINUTES * 60 * 1000;
const PROMPT_TIMEOUT_SECONDS = runtimeConfig.SESSION_PROMPT_TIMEOUT_SECONDS;
const TOKEN_RENEWAL_RETRY_MS = 30 * 1000;

type SessionLifecycleProps = {
  children: ReactNode;
};

export default function SessionLifecycle({ children }: SessionLifecycleProps) {
  const navigate = useNavigate();
  const [sessionActive, setSessionActive] = useState(hasTokenSession);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [secondsLeft, setSecondsLeft] = useState(PROMPT_TIMEOUT_SECONDS);
  const [isRenewing, setIsRenewing] = useState(false);
  const [renewalError, setRenewalError] = useState(false);
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

  const endSession = useCallback(() => {
    if (logoutStartedRef.current) return;
    logoutStartedRef.current = true;
    clearSessionTimers();
    dialogOpenRef.current = false;
    setDialogOpen(false);
    clearTokenSession();
    setSessionActive(false);
    navigate("/login", { replace: true });
  }, [clearSessionTimers, navigate]);

  const showWarning = useCallback((remainingSeconds: number) => {
    if (logoutStartedRef.current || dialogOpenRef.current) return;
    dialogOpenRef.current = true;
    setSecondsLeft(remainingSeconds);
    setRenewalError(false);
    setDialogOpen(true);
  }, []);

  const checkSessionDeadline = useCallback(() => {
    if (!sessionActive || logoutStartedRef.current) return;

    const now = Date.now();
    const inactivityDeadline = lastActivityAtRef.current + INACTIVITY_TIMEOUT_MS;

    window.clearTimeout(warningTimerRef.current);
    window.clearTimeout(expiryTimerRef.current);

    if (now >= inactivityDeadline) {
      void endSession();
      return;
    }

    const warningDeadline = inactivityDeadline - INACTIVITY_WARNING_BEFORE_END_MS;
    if (now >= warningDeadline && !dialogOpenRef.current) {
      showWarning(Math.max(1, Math.ceil((inactivityDeadline - now) / 1000)));
    } else if (!dialogOpenRef.current) {
      warningTimerRef.current = window.setTimeout(checkSessionDeadline, warningDeadline - now);
    }

    expiryTimerRef.current = window.setTimeout(() => {
      if (Date.now() >= lastActivityAtRef.current + INACTIVITY_TIMEOUT_MS) void endSession();
      else checkSessionDeadline();
    }, inactivityDeadline - now);
  }, [sessionActive, endSession, showWarning]);

  useEffect(() => {
    const synchronizeSession = () => setSessionActive(hasTokenSession());
    window.addEventListener(SESSION_UPDATED_EVENT, synchronizeSession);
    return () => window.removeEventListener(SESSION_UPDATED_EVENT, synchronizeSession);
  }, []);

  useEffect(() => {
    if (!sessionActive) {
      clearSessionTimers();
      dialogOpenRef.current = false;
      setDialogOpen(false);
      return;
    }

    logoutStartedRef.current = false;
    lastActivityAtRef.current = Date.now();
    checkSessionDeadline();

    const handleActivity = (event: Event) => {
      if (logoutStartedRef.current) return;
      const target = event.target;
      const interactionInWarningDialog =
        dialogOpenRef.current &&
        target instanceof Element &&
        target.closest('[role="dialog"]') !== null;

      lastActivityAtRef.current = Date.now();
      if (dialogOpenRef.current && !interactionInWarningDialog) {
        dialogOpenRef.current = false;
        setDialogOpen(false);
        setSecondsLeft(PROMPT_TIMEOUT_SECONDS);
        setRenewalError(false);
      }
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
  }, [sessionActive, checkSessionDeadline, clearSessionTimers]);

  useEffect(() => {
    if (!sessionActive) return;

    let cancelado = false;
    let timer: number | undefined;
    const programarRenovacion = () => {
      window.clearTimeout(timer);
      if (cancelado || !hasTokenSession()) return;
      const espera = getAccessTokenRenewalDelayMs(
        runtimeConfig.SESSION_TOKEN_WARNING_MINUTES * 60 * 1000,
      ) ?? 0;
      timer = window.setTimeout(() => void renovar(), espera);
    };
    const renovar = async () => {
      try {
        await renewAccessToken();
        if (!cancelado) {
          setRenewalError(false);
          programarRenovacion();
        }
      } catch (error: unknown) {
        if (cancelado) return;
        if (error instanceof TokenRenewalError && error.invalidGrant) {
          endSession();
          return;
        }
        setRenewalError(true);
        timer = window.setTimeout(() => void renovar(), TOKEN_RENEWAL_RETRY_MS);
      }
    };
    const renovarSiEsNecesario = () => {
      const espera = getAccessTokenRenewalDelayMs(
        runtimeConfig.SESSION_TOKEN_WARNING_MINUTES * 60 * 1000,
      );
      if (espera === null || espera <= 0) {
        void renovar();
      }
    };

    programarRenovacion();
    window.addEventListener("focus", renovarSiEsNecesario);
    document.addEventListener("visibilitychange", renovarSiEsNecesario);
    return () => {
      cancelado = true;
      window.clearTimeout(timer);
      window.removeEventListener("focus", renovarSiEsNecesario);
      document.removeEventListener("visibilitychange", renovarSiEsNecesario);
    };
  }, [endSession, sessionActive]);

  useEffect(() => {
    if (!dialogOpen || isRenewing) {
      window.clearInterval(logoutTimerRef.current);
      return;
    }

    logoutTimerRef.current = window.setInterval(() => {
      const deadline = lastActivityAtRef.current + INACTIVITY_TIMEOUT_MS;
      setSecondsLeft(Math.max(0, Math.ceil((deadline - Date.now()) / 1000)));
    }, 1000);

    return () => window.clearInterval(logoutTimerRef.current);
  }, [dialogOpen, isRenewing]);

  const continueSession = async () => {
    if (isRenewing || logoutStartedRef.current) return;
    setIsRenewing(true);
    setRenewalError(false);
    try {
      await renewAccessToken();
      lastActivityAtRef.current = Date.now();
      dialogOpenRef.current = false;
      setDialogOpen(false);
      setSecondsLeft(PROMPT_TIMEOUT_SECONDS);
      setRenewalError(false);
    } catch (error: unknown) {
      if (error instanceof TokenRenewalError && error.invalidGrant) {
        endSession();
        return;
      }
      lastActivityAtRef.current = Date.now();
      setRenewalError(true);
      checkSessionDeadline();
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
            {renewalError && (
              <Alert severity="error">
                {resolveMessage(MessageCodes.SESSION_RENEWAL_FAILED)}
              </Alert>
            )}
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
