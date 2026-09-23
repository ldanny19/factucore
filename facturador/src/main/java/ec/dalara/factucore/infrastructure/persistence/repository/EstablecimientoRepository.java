package ec.dalara.factucore.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import ec.dalara.factucore.infrastructure.persistence.entity.Establecimiento;

public interface EstablecimientoRepository extends BaseRepository<Establecimiento, Long> {

    List<Establecimiento> findByEmpresaId(Long empresaId);

    Optional<Establecimiento> findByEmpresaIdAndCodigo(Long empresaId, String codigo);

    boolean existsByEmpresaIdAndCodigo(Long empresaId, String codigo);

    boolean existsByEmpresaIdAndCodigoAndIdNot(Long empresaId, String codigo, Long id);
}
