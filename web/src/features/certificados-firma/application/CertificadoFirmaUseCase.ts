import type {
  CertificadoFirma,
  CertificadoFirmaDatos,
} from "../domain/CertificadoFirma";
import type { CertificadoFirmaRepository } from "../domain/CertificadoFirmaRepository";

export interface CertificadoFirmaUseCase {
  listar(idEmpresa: number): Promise<CertificadoFirma[]>;
  obtener(id: number): Promise<CertificadoFirma>;
  crear(datos: CertificadoFirmaDatos): Promise<CertificadoFirma>;
  actualizar(id: number, datos: CertificadoFirmaDatos): Promise<CertificadoFirma>;
  inactivar(id: number): Promise<void>;
  reactivar(id: number): Promise<void>;
  eliminar(id: number): Promise<void>;
}

export function createCertificadoFirmaUseCases(
  repository: CertificadoFirmaRepository,
): CertificadoFirmaUseCase {
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
