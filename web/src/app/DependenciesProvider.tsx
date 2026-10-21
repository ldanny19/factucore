import type { ReactNode } from "react";
import { useMemo } from "react";
import { createEmpresaUseCases } from "../features/empresas/application/EmpresaUseCases";
import { EmpresaUseCasesContext } from "../features/empresas/presentation/hooks/EmpresaUseCasesContext";
import { HttpEmpresaRepository } from "../features/empresas/infrastructure/HttpEmpresaRepository";

export default function DependenciesProvider({ children }: { children: ReactNode }) {
  const empresaUseCases = useMemo(
    () => createEmpresaUseCases(new HttpEmpresaRepository()),
    [],
  );

  return (
    <EmpresaUseCasesContext.Provider value={empresaUseCases}>
      {children}
    </EmpresaUseCasesContext.Provider>
  );
}
