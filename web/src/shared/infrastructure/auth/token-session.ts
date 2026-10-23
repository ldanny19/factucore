import { oidcConfig } from "./oidc-config";

export const ACCESS_TOKEN_KEY = "factucore.access_token";
export const REFRESH_TOKEN_KEY = "factucore.refresh_token";
export const SESSION_UPDATED_EVENT = "factucore:session-updated";

interface TokenResponse {
  access_token?: string;
  refresh_token?: string;
  expires_in?: number;
  error?: string;
  error_description?: string;
}

export class TokenRenewalError extends Error {
  constructor(
    message: string,
    readonly invalidGrant: boolean,
  ) {
    super(message);
    this.name = "TokenRenewalError";
  }
}

let renewalInProgress: Promise<void> | null = null;

function isTokenResponse(value: unknown): value is TokenResponse {
  if (typeof value !== "object" || value === null) return false;
  const response = value as TokenResponse;
  return (
    (response.access_token === undefined || typeof response.access_token === "string") &&
    (response.refresh_token === undefined || typeof response.refresh_token === "string") &&
    (response.error === undefined || typeof response.error === "string") &&
    (response.error_description === undefined ||
      typeof response.error_description === "string")
  );
}

function notifySessionUpdated(): void {
  window.dispatchEvent(new Event(SESSION_UPDATED_EVENT));
}

export function storeTokenResponse(response: TokenResponse): boolean {
  if (
    typeof response.access_token !== "string" ||
    !response.access_token ||
    typeof response.refresh_token !== "string" ||
    !response.refresh_token
  ) {
    return false;
  }

  window.sessionStorage.setItem(ACCESS_TOKEN_KEY, response.access_token);
  window.sessionStorage.setItem(REFRESH_TOKEN_KEY, response.refresh_token);
  notifySessionUpdated();
  return true;
}

export function clearTokenSession(): void {
  window.sessionStorage.removeItem(ACCESS_TOKEN_KEY);
  window.sessionStorage.removeItem(REFRESH_TOKEN_KEY);
  notifySessionUpdated();
}

export function hasTokenSession(): boolean {
  return Boolean(
    window.sessionStorage.getItem(ACCESS_TOKEN_KEY) &&
      hasRefreshToken(),
  );
}

export function hasRefreshToken(): boolean {
  return Boolean(window.sessionStorage.getItem(REFRESH_TOKEN_KEY));
}

export function getAccessTokenExpirationMs(): number | null {
  const token = window.sessionStorage.getItem(ACCESS_TOKEN_KEY);
  if (!token) return null;

  try {
    const payload = token.split(".")[1];
    if (!payload) return null;
    const base64 = payload.replace(/-/g, "+").replace(/_/g, "/");
    const decoded = window.atob(base64.padEnd(Math.ceil(base64.length / 4) * 4, "="));
    const claims = JSON.parse(decoded) as { exp?: number };
    return typeof claims.exp === "number" ? claims.exp * 1000 : null;
  } catch {
    return null;
  }
}

export function getAccessTokenRenewalDelayMs(beforeExpiryMs: number): number | null {
  const token = window.sessionStorage.getItem(ACCESS_TOKEN_KEY);
  if (!token) return null;

  try {
    const payload = token.split(".")[1];
    if (!payload) return null;
    const base64 = payload.replace(/-/g, "+").replace(/_/g, "/");
    const decoded = window.atob(base64.padEnd(Math.ceil(base64.length / 4) * 4, "="));
    const claims = JSON.parse(decoded) as { exp?: number; iat?: number };
    if (typeof claims.exp !== "number") return null;

    const expiresAt = claims.exp * 1000;
    const lifetimeMs =
      typeof claims.iat === "number" ? Math.max(0, (claims.exp - claims.iat) * 1000) : 0;
    const renewalWindowMs = Math.min(
      beforeExpiryMs,
      Math.max(15_000, lifetimeMs * 0.2),
    );
    return Math.max(0, expiresAt - Date.now() - renewalWindowMs);
  } catch {
    return null;
  }
}

export function renewAccessToken(): Promise<void> {
  if (renewalInProgress) return renewalInProgress;

  renewalInProgress = (async () => {
    const refreshToken = window.sessionStorage.getItem(REFRESH_TOKEN_KEY);
    if (!refreshToken) {
      throw new TokenRenewalError("No hay un refresh token almacenado.", true);
    }

    const credentials = new URLSearchParams({
      grant_type: "refresh_token",
      client_id: oidcConfig.client_id,
      refresh_token: refreshToken,
    });
    const tokenEndpoint = `${oidcConfig.authority.replace(/\/$/, "")}/protocol/openid-connect/token`;
    const response = await fetch(tokenEndpoint, {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: credentials,
    });

    let body: unknown;
    try {
      body = await response.json();
    } catch {
      throw new TokenRenewalError("Keycloak devolvió una respuesta inválida al renovar.", false);
    }

    if (!response.ok || !isTokenResponse(body) || !body.access_token) {
      const errorCode = isTokenResponse(body) ? body.error : undefined;
      const description = isTokenResponse(body) ? body.error_description : undefined;
      throw new TokenRenewalError(
        description || "Keycloak no pudo renovar la sesión.",
        errorCode === "invalid_grant",
      );
    }

    const nextResponse: TokenResponse = {
      ...body,
      refresh_token: body.refresh_token || refreshToken,
    };
    if (!storeTokenResponse(nextResponse)) {
      throw new TokenRenewalError("Keycloak no devolvió un access token válido.", false);
    }
  })().finally(() => {
    renewalInProgress = null;
  });

  return renewalInProgress;
}
