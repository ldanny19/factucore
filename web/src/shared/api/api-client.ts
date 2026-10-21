import axios from "axios";
import type { AxiosInstance, AxiosRequestConfig } from "axios";
import { getRuntimeConfig } from "../../config/runtime-config";
import { MessageCodes } from "../messages/MessageCodes";
import { resolveMessage } from "../messages/MessageResolver";
import { userManager } from "../infrastructure/auth/user-manager";
import type { AdministracionResponse } from "./administracion-contracts";

function createClient(): AxiosInstance {
  const config = getRuntimeConfig();
  const baseURL = [config.API_BASE_URL.replace(/\/$/, ""), config.API_PATH_PREFIX.replace(/^\/+|\/+$/g, ""), config.API_VERSION.replace(/^\/+|\/+$/g, "")].filter(Boolean).join("/");
  const client = axios.create({ baseURL: `${baseURL}/` });
  client.interceptors.request.use(async (request) => {
    const user = await userManager.getUser();
    if (user?.access_token && user.expires_at && user.expires_at * 1000 > Date.now()) request.headers.set("Authorization", `Bearer ${user.access_token}`);
    return request;
  });
  return client;
}
const client = createClient();
export async function apiRequest<T>(config: AxiosRequestConfig): Promise<T> {
  const response = await client.request<AdministracionResponse<T>>(config);
  const envelope = response.data;
  if (!envelope.exitoso) throw new Error(envelope.mensaje || envelope.codigo || resolveMessage(MessageCodes.API_OPERATION_FAILED));
  return envelope.datos;
}
