import { getRuntimeConfig } from "../../../config/runtime-config";
import { apiRequest } from "../../../shared/api/api-client";
import { normalizarEstadoRegistro } from "../../../shared/api/HttpRepository";
import type { AdministracionGlobalRequest } from "../../../shared/api/administracion-contracts";
import type {
  Catalogo,
  CatalogoItem,
} from "../domain/Catalogo";
import type { CatalogoRepository } from "../domain/CatalogoRepository";

function crearSolicitud<T>(datos: T): AdministracionGlobalRequest<T> {
  const now = new Date();
  const pad = (value: number, length = 2) => String(value).padStart(length, "0");
  const fechaInicio =
    `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())} ` +
    `${pad(now.getHours())}:${pad(now.getMinutes())}:${pad(now.getSeconds())}.` +
    pad(now.getMilliseconds(), 3);

  return {
    idTransaccion: crypto.randomUUID(),
    fechaInicio,
    canal: "WEB",
    datos,
  };
}

export function createHttpCatalogoRepository(): CatalogoRepository {
  const config = getRuntimeConfig();
  const catalogosPath = config.API_CATALOGOS_PATH.replace(/^\/+|\/+$/g, "");
  const itemsPath = config.API_CATALOGO_ITEMS_PATH.replace(/^\/+|\/+$/g, "");

  return {
    listar: () =>
      apiRequest<Catalogo[]>({ method: "GET", url: catalogosPath }).then((catalogos) =>
        catalogos.map(normalizarEstadoRegistro),
      ),
    obtener: (id) =>
        apiRequest<Catalogo>({ method: "GET", url: `${catalogosPath}/${id}` }).then(
          normalizarEstadoRegistro,
        ),
    crear: (datos) =>
        apiRequest<Catalogo>({
          method: "POST",
          url: catalogosPath,
          data: crearSolicitud(datos),
        }).then(normalizarEstadoRegistro),
    actualizar: (id, datos) =>
        apiRequest<Catalogo>({
          method: "PUT",
          url: `${catalogosPath}/${id}`,
          data: crearSolicitud(datos),
        }).then(normalizarEstadoRegistro),
    inactivar: (id) =>
        apiRequest<void>({ method: "PATCH", url: `${catalogosPath}/${id}/inactivar` }),
    reactivar: (id) =>
        apiRequest<void>({ method: "PATCH", url: `${catalogosPath}/${id}/reactivar` }),
    eliminar: (id) =>
        apiRequest<void>({ method: "DELETE", url: `${catalogosPath}/${id}` }),
    listarItems: (catalogoId) =>
      apiRequest<CatalogoItem[]>({
        method: "GET",
        url: itemsPath,
        params: { catalogoId },
      }).then((items) => items.map(normalizarEstadoRegistro)),
    obtenerItem: (id) =>
      apiRequest<CatalogoItem>({ method: "GET", url: `${itemsPath}/${id}` }).then(
        normalizarEstadoRegistro,
      ),
    crearItem: (datos) =>
      apiRequest<CatalogoItem>({
        method: "POST",
        url: itemsPath,
        data: crearSolicitud(datos),
      }).then(normalizarEstadoRegistro),
    actualizarItem: (id, datos) =>
      apiRequest<CatalogoItem>({
        method: "PUT",
        url: `${itemsPath}/${id}`,
        data: crearSolicitud(datos),
      }).then(normalizarEstadoRegistro),
    inactivarItem: (id) =>
      apiRequest<void>({ method: "PATCH", url: `${itemsPath}/${id}/inactivar` }),
    reactivarItem: (id) =>
      apiRequest<void>({ method: "PATCH", url: `${itemsPath}/${id}/reactivar` }),
    eliminarItem: (id) =>
      apiRequest<void>({ method: "DELETE", url: `${itemsPath}/${id}` }),
  };
}
