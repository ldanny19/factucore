package ec.dalara.factucore.infrastructure.persistence.repository;

import ec.dalara.factucore.infrastructure.persistence.entity.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmpresaRepository extends BaseRepository<Empresa, Long> {

    Optional<Empresa> findByRuc(String ruc);

    boolean existsByRuc(String ruc);
}