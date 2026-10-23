import { createContext, useContext } from "react";
import type { CatalogoUseCase } from "../../application/CatalogoUseCase";

export const CatalogoUseCaseContext = createContext<CatalogoUseCase | null>(null);

export function useCatalogoUseCase(): CatalogoUseCase {
  const useCases = useContext(CatalogoUseCaseContext);
  if (!useCases) {
    throw new Error("No se configuraron los casos de uso de Catálogos.");
  }
  return useCases;
}
