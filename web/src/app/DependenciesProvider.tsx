import type { ReactNode } from "react";
import { useMemo } from "react";
import { createEmpresaUseCases } from "../features/empresas/application/EmpresaUseCases";
import { EmpresaUseCasesContext } from "../features/empresas/presentation/hooks/EmpresaUseCasesContext";
import { createHttpEmpresaRepository } from "../features/empresas/infrastructure/HttpEmpresaRepository";
import { serviceEstablecimientoUseCase } from "../features/establecimientos/application/EstablecimientoUseCase";
import { createHttpEstablecimientoRepository } from "../features/establecimientos/infrastructure/HttpEstablecimientoRepository";
import { EstablecimientoUseCaseContext } from "../features/establecimientos/presentation/hooks/EstablecimientoUseCaseContext";
import { createPuntoEmisionUseCases } from "../features/puntos-emision/application/PuntoEmisionUseCase";
import { createHttpPuntoEmisionRepository } from "../features/puntos-emision/infrastructure/HttpPuntoEmisionRepository";
import { PuntoEmisionUseCaseContext } from "../features/puntos-emision/presentation/hooks/PuntoEmisionUseCaseContext";

export default function DependenciesProvider({ children }: { children: ReactNode }) {
  const empresaUseCases = useMemo(
    () => createEmpresaUseCases(createHttpEmpresaRepository()),
    [],
  );
  const establecimientoUseCases = useMemo(
    () => serviceEstablecimientoUseCase(createHttpEstablecimientoRepository()),
    [],
  );
  const puntoEmisionUseCases = useMemo(
    () => createPuntoEmisionUseCases(createHttpPuntoEmisionRepository()),
    [],
  );

  return (
    <EmpresaUseCasesContext.Provider value={empresaUseCases}>
      <EstablecimientoUseCaseContext.Provider value={establecimientoUseCases}>
        <PuntoEmisionUseCaseContext.Provider value={puntoEmisionUseCases}>
          {children}
        </PuntoEmisionUseCaseContext.Provider>
      </EstablecimientoUseCaseContext.Provider>
    </EmpresaUseCasesContext.Provider>
  );
}
