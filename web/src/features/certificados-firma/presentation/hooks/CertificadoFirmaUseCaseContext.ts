import { createContext, useContext } from "react";
import type { CertificadoFirmaUseCase } from "../../application/CertificadoFirmaUseCase";

export const CertificadoFirmaUseCaseContext =
  createContext<CertificadoFirmaUseCase | null>(null);

export function useCertificadoFirmaUseCase(): CertificadoFirmaUseCase {
  const useCases = useContext(CertificadoFirmaUseCaseContext);
  if (!useCases) {
    throw new Error("No se configuraron los casos de uso de Certificados de firma.");
  }
  return useCases;
}
