import type {
  CertificadoFirma,
  CertificadoFirmaDatos,
} from "./CertificadoFirma";

export interface CertificadoFirmaRepository {
  listar(idEmpresa: number): Promise<CertificadoFirma[]>;
  obtener(id: number): Promise<CertificadoFirma>;
  crear(datos: CertificadoFirmaDatos): Promise<CertificadoFirma>;
  actualizar(id: number, datos: CertificadoFirmaDatos): Promise<CertificadoFirma>;
  inactivar(id: number): Promise<void>;
  reactivar(id: number): Promise<void>;
  eliminar(id: number): Promise<void>;
}
