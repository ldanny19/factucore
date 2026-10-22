import { getRuntimeConfig } from "../../../config/runtime-config";
import { apiRequest } from "../../../shared/api/api-client";
import { normalizarEstadoRegistro } from "../../../shared/api/HttpRepository";
import type { Catalogo, CatalogoItem } from "../domain/Catalogo";
import type { CatalogoRepository } from "../domain/CatalogoRepository";

export function createHttpCatalogoRepository(): CatalogoRepository {
  const config = getRuntimeConfig();
  const catalogosPath = config.API_CATALOGOS_PATH.replace(/^\/+|\/+$/g, "");
  const itemsPath = config.API_CATALOGO_ITEMS_PATH.replace(/^\/+|\/+$/g, "");

  return {
    listar: () =>
      apiRequest<Catalogo[]>({ method: "GET", url: catalogosPath }).then((catalogos) =>
        catalogos.map(normalizarEstadoRegistro),
      ),
    listarItems: (catalogoId) =>
      apiRequest<CatalogoItem[]>({
        method: "GET",
        url: itemsPath,
        params: { catalogoId },
      }).then((items) => items.map(normalizarEstadoRegistro)),
  };
}
