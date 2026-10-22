import { createContext, useContext } from "react";
import type { ConfiguracionEmpresaUseCase } from "../../application/ConfiguracionEmpresaUseCase";

export const ConfiguracionEmpresaUseCaseContext =
  createContext<ConfiguracionEmpresaUseCase | null>(null);

export function useConfiguracionEmpresaUseCase(): ConfiguracionEmpresaUseCase {
  const useCases = useContext(ConfiguracionEmpresaUseCaseContext);
  if (!useCases) {
    throw new Error("No se configuraron los casos de uso de Configuración de empresa.");
  }
  return useCases;
}
