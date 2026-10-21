import axios from "axios";
import type { AxiosInstance, AxiosRequestConfig } from "axios";
import { getRuntimeConfig } from "../../config/runtime-config";
import { userManager } from "../infrastructure/auth/user-manager";
import type { AdministracionResponse } from "./administracion-contracts";

function createClient(): AxiosInstance {
  const config = getRuntimeConfig();
  const client = axios.create({
    baseURL: config.API_BASE_URL.replace(/\/$/, ""),
  });

  client.interceptors.request.use(async (request) => {
    const user = await userManager.getUser();
    if (user?.access_token && user.expires_at && user.expires_at * 1000 > Date.now()) {
      request.headers.set("Authorization", `Bearer ${user.access_token}`);
    }
    return request;
  });

  return client;
}

const client = createClient();

export async function apiRequest<T>(
  config: AxiosRequestConfig,
): Promise<T> {
  const response = await client.request<AdministracionResponse<T>>(config);
  const envelope = response.data;

  if (!envelope.exitoso) {
    throw new Error(envelope.mensaje || envelope.codigo || "La operación no fue exitosa.");
  }

  return envelope.datos;
}
