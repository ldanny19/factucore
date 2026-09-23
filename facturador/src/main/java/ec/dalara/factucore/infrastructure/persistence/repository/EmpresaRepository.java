package ec.dalara.factucore.infrastructure.persistence.repository;

import java.util.Optional;

import ec.dalara.factucore.infrastructure.persistence.entity.Empresa;

public interface EmpresaRepository extends BaseRepository<Empresa, Long> {

    Optional<Empresa> findByRuc(String ruc);

    boolean existsByRuc(String ruc);

    boolean existsByRucAndIdNot(String ruc, Long id);
}
