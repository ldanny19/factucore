import { createContext, useContext } from "react";
import type { EstablecimientoUseCase } from "../../application/EstablecimientoUseCase";

export const EstablecimientoUseCaseContext =
  createContext<EstablecimientoUseCase | null>(null);

export function useEstablecimientoUseCase(): EstablecimientoUseCase {
  const useCases = useContext(EstablecimientoUseCaseContext);
  if (!useCases) {
    throw new Error("No se configuraron los casos de uso de Establecimientos.");
  }
  return useCases;
}
