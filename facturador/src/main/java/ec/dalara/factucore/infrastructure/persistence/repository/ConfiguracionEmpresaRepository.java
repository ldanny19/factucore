package ec.dalara.factucore.infrastructure.persistence.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ec.dalara.factucore.infrastructure.persistence.entity.ConfiguracionEmpresa;

public interface ConfiguracionEmpresaRepository extends BaseRepository<ConfiguracionEmpresa, Long> {

	List<ConfiguracionEmpresa> findByEmpresaId(Long empresaId);

	Optional<ConfiguracionEmpresa> findByEmpresaIdAndClave(Long empresaId, String clave);

	boolean existsByEmpresaIdAndClave(Long empresaId, String clave);

	boolean existsByEmpresaIdAndClaveAndFechaVigenciaDesde(Long empresaId, String clave,
			LocalDateTime fechaVigenciaDesde);

	boolean existsByEmpresaIdAndClaveAndFechaVigenciaDesdeAndIdNot(Long empresaId, String clave,
			LocalDateTime fechaVigenciaDesde, Long id);

\t@Query("""
\t\t\tSELECT c
\t\t\tFROM ConfiguracionEmpresa c
\t\t\tWHERE c.empresa.id = :empresaId
\t\t\t  AND c.clave = :clave
\t\t\t  AND c.estadoRegistro = :estadoRegistro
\t\t\t  AND c.fechaVigenciaDesde <= :fecha
\t\t\t  AND c.fechaVigenciaHasta >= :fecha
\t\t""")
\tOptional<ConfiguracionEmpresa> findConfiguracionVigenteConFechaFin(
\t\t\t@Param("empresaId") Long empresaId,
\t\t\t@Param("clave") String clave,
\t\t\t@Param("estadoRegistro") String estadoRegistro,
\t\t\t@Param("fecha") LocalDateTime fecha);

	Optional<ConfiguracionEmpresa> findByEmpresaIdAndClaveAndEstadoRegistroAndFechaVigenciaDesdeLessThanEqualAndFechaVigenciaHastaIsNull(
			Long empresaId, String clave, String estadoRegistro, LocalDateTime fecha);
}