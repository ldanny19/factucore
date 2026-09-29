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

	@Query("""
				SELECT c
				FROM ConfiguracionEmpresa c
				WHERE c.empresa.id = :empresaId
				  AND c.clave = :clave
				  AND c.estadoRegistro = :estadoRegistro
				  AND c.fechaVigenciaDesde <= :fecha
				  AND c.fechaVigenciaHasta >= :fecha
			""")
	Optional<ConfiguracionEmpresa> findConfiguracionVigenteConFechaFin(@Param("empresaId") Long empresaId,
			@Param("clave") String clave, @Param("estadoRegistro") String estadoRegistro,
			@Param("fecha") LocalDateTime fecha);

	Optional<ConfiguracionEmpresa> findByEmpresaIdAndClaveAndEstadoRegistroAndFechaVigenciaDesdeLessThanEqualAndFechaVigenciaHastaIsNull(
			Long empresaId, String clave, String estadoRegistro, LocalDateTime fecha);
}