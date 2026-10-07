package ec.dalara.factucore.infrastructure.persistence.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ec.dalara.factucore.infrastructure.persistence.entity.VersionDocumentoXsd;

public interface VersionDocumentoXsdRepository extends BaseRepository<VersionDocumentoXsd, Long> {

	List<VersionDocumentoXsd> findByDocumentoXsdId(Long documentoXsdId);

	Optional<VersionDocumentoXsd> findByDocumentoXsdIdAndVersion(Long documentoXsdId, String version);

	Optional<VersionDocumentoXsd> findByDocumentoXsdIdAndVersionAndEstadoRegistro(Long documentoXsdId,
			String version, String estadoRegistro);

	boolean existsByDocumentoXsdIdAndVersion(Long documentoXsdId, String version);

	boolean existsByDocumentoXsdIdAndVersionAndIdNot(Long documentoXsdId, String version, Long id);

	@Query("""
			select case when count(v) > 0 then true else false end
			from VersionDocumentoXsd v
			where v.documentoXsd.id = :documentoXsdId
			and v.estadoRegistro = :estadoRegistro
			and v.fechaInicio <= coalesce(:fechaFin, v.fechaInicio)
			and (v.fechaFin is null or v.fechaFin >= :fechaInicio)
			""")
	boolean existsByDocumentoXsdIdAndEstadoRegistroAndRangoFechas(
			@Param("documentoXsdId") Long documentoXsdId, @Param("estadoRegistro") String estadoRegistro,
			@Param("fechaInicio") LocalDateTime fechaInicio, @Param("fechaFin") LocalDateTime fechaFin);

	Optional<VersionDocumentoXsd> findByDocumentoXsdIdAndEstadoRegistroAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(
			Long documentoXsdId, String estadoRegistro, LocalDateTime fechaInicio, LocalDateTime fechaFin);

	Optional<VersionDocumentoXsd> findByDocumentoXsdIdAndEstadoRegistroAndFechaInicioLessThanEqualAndFechaFinIsNull(
			Long documentoXsdId, String estadoRegistro, LocalDateTime fechaInicio);
} 