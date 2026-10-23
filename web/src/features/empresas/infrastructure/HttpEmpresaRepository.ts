import { getRuntimeConfig } from "../../../config/runtime-config";
import { HttpRepository } from "../../../shared/api/HttpRepository";
import type { Empresa, EmpresaDatos } from "../domain/Empresa";
import type { EmpresaRepository } from "../domain/EmpresaRepository";

export function createHttpEmpresaRepository(): EmpresaRepository {
  return new HttpRepository<Empresa, EmpresaDatos>(
    getRuntimeConfig().API_EMPRESAS_PATH,
  );
}
