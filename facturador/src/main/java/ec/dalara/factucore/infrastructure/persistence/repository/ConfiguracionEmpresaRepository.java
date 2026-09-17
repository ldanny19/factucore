package ec.dalara.factucore.infrastructure.persistence.repository;

import ec.dalara.factucore.infrastructure.persistence.entity.ConfiguracionEmpresa;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ConfiguracionEmpresaRepository
        extends BaseRepository<ConfiguracionEmpresa, Long> {

    List<ConfiguracionEmpresa> findByEmpresaId(Long empresaId);

    Optional<ConfiguracionEmpresa> findByEmpresaIdAndClave(
            Long empresaId,
            String clave
    );

    boolean existsByEmpresaIdAndClave(
            Long empresaId,
            String clave
    );

    Optional<ConfiguracionEmpresa> findByEmpresaIdAndClaveAndEstadoRegistroAndFechaVigenciaDesdeLessThanEqualAndFechaVigenciaHastaGreaterThanEqual(
            Long empresaId,
            String clave,
            String estadoRegistro,
            LocalDateTime fecha
    );

    Optional<ConfiguracionEmpresa> findByEmpresaIdAndClaveAndEstadoRegistroAndFechaVigenciaDesdeLessThanEqualAndFechaVigenciaHastaIsNull(
            Long empresaId,
            String clave,
            String estadoRegistro,
            LocalDateTime fecha
    );
}