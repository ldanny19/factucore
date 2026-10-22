import type {
  ConfiguracionEmpresa,
  ConfiguracionEmpresaDatos,
} from "./ConfiguracionEmpresa";

export interface ConfiguracionEmpresaRepository {
  listar(idEmpresa: number): Promise<ConfiguracionEmpresa[]>;
  obtener(id: number): Promise<ConfiguracionEmpresa>;
  crear(datos: ConfiguracionEmpresaDatos): Promise<ConfiguracionEmpresa>;
  actualizar(id: number, datos: ConfiguracionEmpresaDatos): Promise<ConfiguracionEmpresa>;
  inactivar(id: number): Promise<void>;
  reactivar(id: number): Promise<void>;
  eliminar(id: number): Promise<void>;
}
