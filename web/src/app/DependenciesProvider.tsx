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
import { createSecuencialUseCases } from "../features/secuenciales/application/SecuencialUseCase";
import { createHttpSecuencialRepository } from "../features/secuenciales/infrastructure/HttpSecuencialRepository";
import { SecuencialUseCaseContext } from "../features/secuenciales/presentation/hooks/SecuencialUseCaseContext";
import { createConfiguracionEmpresaUseCases } from "../features/configuracion-empresa/application/ConfiguracionEmpresaUseCase";
import { createHttpConfiguracionEmpresaRepository } from "../features/configuracion-empresa/infrastructure/HttpConfiguracionEmpresaRepository";
import { ConfiguracionEmpresaUseCaseContext } from "../features/configuracion-empresa/presentation/hooks/ConfiguracionEmpresaUseCaseContext";
import { createCertificadoFirmaUseCases } from "../features/certificados-firma/application/CertificadoFirmaUseCase";
import { createHttpCertificadoFirmaRepository } from "../features/certificados-firma/infrastructure/HttpCertificadoFirmaRepository";
import { CertificadoFirmaUseCaseContext } from "../features/certificados-firma/presentation/hooks/CertificadoFirmaUseCaseContext";
import { createTarifaTributariaUseCases } from "../features/tarifas-tributarias/application/TarifaTributariaUseCase";
import { createHttpTarifaTributariaRepository } from "../features/tarifas-tributarias/infrastructure/HttpTarifaTributariaRepository";
import { TarifaTributariaUseCaseContext } from "../features/tarifas-tributarias/presentation/hooks/TarifaTributariaUseCaseContext";
import { createCatalogoUseCases } from "../features/catalogos/application/CatalogoUseCase";
import { createHttpCatalogoRepository } from "../features/catalogos/infrastructure/HttpCatalogoRepository";
import { CatalogoUseCaseContext } from "../features/catalogos/presentation/hooks/CatalogoUseCaseContext";

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
  const secuencialUseCases = useMemo(
    () => createSecuencialUseCases(createHttpSecuencialRepository()),
    [],
  );
  const configuracionEmpresaUseCases = useMemo(
    () => createConfiguracionEmpresaUseCases(createHttpConfiguracionEmpresaRepository()),
    [],
  );
  const certificadoFirmaUseCases = useMemo(
    () => createCertificadoFirmaUseCases(createHttpCertificadoFirmaRepository()),
    [],
  );
  const tarifaTributariaUseCases = useMemo(
    () => createTarifaTributariaUseCases(createHttpTarifaTributariaRepository()),
    [],
  );
  const catalogoUseCases = useMemo(
    () => createCatalogoUseCases(createHttpCatalogoRepository()),
    [],
  );

  return (
    <EmpresaUseCasesContext.Provider value={empresaUseCases}>
      <EstablecimientoUseCaseContext.Provider value={establecimientoUseCases}>
        <PuntoEmisionUseCaseContext.Provider value={puntoEmisionUseCases}>
          <SecuencialUseCaseContext.Provider value={secuencialUseCases}>
            <ConfiguracionEmpresaUseCaseContext.Provider value={configuracionEmpresaUseCases}>
              <CertificadoFirmaUseCaseContext.Provider value={certificadoFirmaUseCases}>
                <CatalogoUseCaseContext.Provider value={catalogoUseCases}>
                  <TarifaTributariaUseCaseContext.Provider value={tarifaTributariaUseCases}>
                    {children}
                  </TarifaTributariaUseCaseContext.Provider>
                </CatalogoUseCaseContext.Provider>
              </CertificadoFirmaUseCaseContext.Provider>
            </ConfiguracionEmpresaUseCaseContext.Provider>
          </SecuencialUseCaseContext.Provider>
        </PuntoEmisionUseCaseContext.Provider>
      </EstablecimientoUseCaseContext.Provider>
    </EmpresaUseCasesContext.Provider>
  );
}
