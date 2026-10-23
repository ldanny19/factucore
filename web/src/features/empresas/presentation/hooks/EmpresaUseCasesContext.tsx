import { createContext, useContext } from "react";
import type { EmpresaUseCases } from "../../application/EmpresaUseCases";

export const EmpresaUseCasesContext = createContext<EmpresaUseCases | null>(null);

export function useEmpresaUseCases(): EmpresaUseCases {
  const useCases = useContext(EmpresaUseCasesContext);
  if (!useCases) {
    throw new Error("No se configuraron los casos de uso de Empresas.");
  }
  return useCases;
}
