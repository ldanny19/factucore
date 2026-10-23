import { createContext, useContext } from "react";
import type { TarifaTributariaUseCase } from "../../application/TarifaTributariaUseCase";

export const TarifaTributariaUseCaseContext =
  createContext<TarifaTributariaUseCase | null>(null);

export function useTarifaTributariaUseCase(): TarifaTributariaUseCase {
  const useCases = useContext(TarifaTributariaUseCaseContext);
  if (!useCases) {
    throw new Error("No se configuraron los casos de uso de Tarifas tributarias.");
  }
  return useCases;
}
