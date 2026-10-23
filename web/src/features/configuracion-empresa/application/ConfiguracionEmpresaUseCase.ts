import type {
  ConfiguracionEmpresa,
  ConfiguracionEmpresaDatos,
} from "../domain/ConfiguracionEmpresa";
import type { ConfiguracionEmpresaRepository } from "../domain/ConfiguracionEmpresaRepository";

export interface ConfiguracionEmpresaUseCase {
  listar(idEmpresa: number): Promise<ConfiguracionEmpresa[]>;
  obtener(id: number): Promise<ConfiguracionEmpresa>;
  crear(datos: ConfiguracionEmpresaDatos): Promise<ConfiguracionEmpresa>;
  actualizar(id: number, datos: ConfiguracionEmpresaDatos): Promise<ConfiguracionEmpresa>;
  inactivar(id: number): Promise<void>;
  reactivar(id: number): Promise<void>;
  eliminar(id: number): Promise<void>;
}

export function createConfiguracionEmpresaUseCases(
  repository: ConfiguracionEmpresaRepository,
): ConfiguracionEmpresaUseCase {
  return {
    listar: (idEmpresa) => repository.listar(idEmpresa),
    obtener: (id) => repository.obtener(id),
    crear: (datos) => repository.crear(datos),
    actualizar: (id, datos) => repository.actualizar(id, datos),
    inactivar: (id) => repository.inactivar(id),
    reactivar: (id) => repository.reactivar(id),
    eliminar: (id) => repository.eliminar(id),
  };
}
