import { createContext, useContext } from "react";
import type { SecuencialUseCase } from "../../application/SecuencialUseCase";

export const SecuencialUseCaseContext = createContext<SecuencialUseCase | null>(null);

export function useSecuencialUseCase(): SecuencialUseCase {
  const useCases = useContext(SecuencialUseCaseContext);
  if (!useCases) {
    throw new Error("No se configuraron los casos de uso de Secuenciales.");
  }
  return useCases;
}
