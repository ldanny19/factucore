package ec.dalara.factucore.infrastructure.persistence.repository;

import java.util.List;

import ec.dalara.factucore.infrastructure.persistence.entity.MapeoXsd;

public interface MapeoXsdRepository extends BaseRepository<MapeoXsd, Long> {

	List<MapeoXsd> findByVersionDocumentoXsdIdAndEstadoRegistro(Long versionDocumentoXsdId, String estadoRegistro);

	boolean existsByVersionDocumentoXsdIdAndRutaOrigen(Long versionDocumentoXsdId, String rutaOrigen);
}