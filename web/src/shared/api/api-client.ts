import axios from "axios";
import type { AxiosInstance, AxiosRequestConfig } from "axios";
import { getRuntimeConfig } from "../../config/runtime-config";
import { MessageCodes } from "../messages/MessageCodes";
import { resolveMessage } from "../messages/MessageResolver";
import { userManager } from "../infrastructure/auth/user-manager";
import type { AdministracionResponse } from "./administracion-contracts";

const SESSION_TOKEN_KEY = "factucore.access_token";

function getStoredAccessToken(): string | null {
  if (typeof window === "undefined") return null;
  return window.sessionStorage.getItem(SESSION_TOKEN_KEY);
}

function isTokenExpired(token: string): boolean {
  try {
    const payload = token.split(".")[1];
    if (!payload) return true;

    const base64 = payload.replace(/-/g, "+").replace(/_/g, "/");
    const decoded = window.atob(base64.padEnd(Math.ceil(base64.length / 4) * 4, "="));
    const claims = JSON.parse(decoded) as { exp?: number };
    return typeof claims.exp !== "number" || claims.exp * 1000 <= Date.now();
  } catch {
    return true;
  }
}

function createClient(): AxiosInstance {
  const config = getRuntimeConfig();
  const baseURL = [
    config.API_BASE_URL.replace(/\/$/, ""),
    config.API_PATH_PREFIX.replace(/^\/+|\/+$/g, ""),
    config.API_VERSION.replace(/^\/+|\/+$/g, ""),
  ]
    .filter(Boolean)
    .join("/");
  const client = axios.create({ baseURL: `${baseURL}/` });

  client.interceptors.request.use(async (request) => {
    const user = await userManager.getUser();
    const oidcToken =
      user?.access_token && user.expires_at && user.expires_at * 1000 > Date.now()
        ? user.access_token
        : null;
    const storedToken = getStoredAccessToken();
    const accessToken = oidcToken ?? (storedToken && !isTokenExpired(storedToken) ? storedToken : null);

    if (accessToken) {
      request.headers.set("Authorization", `Bearer ${accessToken}`);
    } else {
      request.headers.delete("Authorization");
    }

    return request;
  });

  return client;
}

const client = createClient();

export async function apiRequest<T>(config: AxiosRequestConfig): Promise<T> {
  const response = await client.request<AdministracionResponse<T>>(config);
  const envelope = response.data;
  if (!envelope.exitoso) {
    throw new Error(
      envelope.mensaje || envelope.codigo || resolveMessage(MessageCodes.API_OPERATION_FAILED),
    );
  }
  return envelope.datos;
}
