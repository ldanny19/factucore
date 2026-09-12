package ec.dalara.factucore.infrastructure.persistence.repository;

import ec.dalara.factucore.infrastructure.persistence.entity.ConfiguracionEmpresa;
import org.springframework.data.jpa.repository.JpaRepository;

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
}