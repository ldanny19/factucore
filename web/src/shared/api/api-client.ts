import axios from "axios";
import type { AxiosError, AxiosInstance, AxiosRequestConfig } from "axios";
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
    const accessToken = storedToken
      ? isTokenExpired(storedToken) ? null : storedToken
      : oidcToken;

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

function isApiResponse<T>(value: unknown): value is AdministracionResponse<T> {
  if (typeof value !== "object" || value === null) return false;

  const response = value as Partial<AdministracionResponse<T>>;
  return typeof response.exitoso === "boolean";
}

function getHttpErrorMessage(error: AxiosError<unknown>): string {
  const responseBody = error.response?.data;

  if (isApiResponse<unknown>(responseBody) && responseBody.mensaje?.trim()) {
    return responseBody.mensaje;
  }

  const status = error.response?.status;
  switch (status) {
    case 400:
      return resolveMessage(MessageCodes.API_HTTP_BAD_REQUEST);
    case 401:
      return resolveMessage(MessageCodes.API_HTTP_UNAUTHORIZED);
    case 403:
      return resolveMessage(MessageCodes.API_HTTP_FORBIDDEN);
    case 404:
      return resolveMessage(MessageCodes.API_HTTP_NOT_FOUND);
    case 500:
      return resolveMessage(MessageCodes.API_HTTP_INTERNAL_SERVER_ERROR);
    default:
      return status
        ? resolveMessage(MessageCodes.API_HTTP_UNEXPECTED_ERROR, { status })
        : resolveMessage(MessageCodes.API_CONNECTION_ERROR);
  }
}

export async function apiRequest<T>(config: AxiosRequestConfig): Promise<T> {
  try {
    const response = await client.request<unknown>(config);
    const envelope = response.data;

    if (!isApiResponse<T>(envelope)) {
      throw new Error(resolveMessage(MessageCodes.API_INVALID_RESPONSE));
    }

    if (!envelope.exitoso) {
      throw new Error(
        envelope.mensaje?.trim() ||
          envelope.codigo ||
          resolveMessage(MessageCodes.API_OPERATION_FAILED),
      );
    }

    return envelope.datos;
  } catch (error: unknown) {
    if (axios.isAxiosError(error)) {
      throw new Error(getHttpErrorMessage(error));
    }

    throw error;
  }
}

export async function apiRequestRaw<T>(config: AxiosRequestConfig): Promise<T> {
  try {
    const response = await client.request<T>(config);
    return response.data;
  } catch (error: unknown) {
    if (axios.isAxiosError(error)) {
      throw new Error(getHttpErrorMessage(error));
    }
    throw error;
  }
}
