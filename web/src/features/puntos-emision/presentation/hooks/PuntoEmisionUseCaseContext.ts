import { createContext, useContext } from "react";
import type { PuntoEmisionUseCase } from "../../application/PuntoEmisionUseCase";

export const PuntoEmisionUseCaseContext = createContext<PuntoEmisionUseCase | null>(null);

export function usePuntoEmisionUseCase(): PuntoEmisionUseCase {
  const useCases = useContext(PuntoEmisionUseCaseContext);
  if (!useCases) {
    throw new Error("No se configuraron los casos de uso de Puntos de emisión.");
  }
  return useCases;
}
