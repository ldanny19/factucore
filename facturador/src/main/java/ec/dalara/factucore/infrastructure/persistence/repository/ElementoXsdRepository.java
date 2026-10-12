package ec.dalara.factucore.infrastructure.persistence.repository;

import java.util.List;

import ec.dalara.factucore.infrastructure.persistence.entity.ElementoXsd;

public interface ElementoXsdRepository extends BaseRepository<ElementoXsd, Long> {

	List<ElementoXsd> findByVersionDocumentoXsdId(Long versionDocumentoXsdId);

	List<ElementoXsd> findByElementoPadreId(Long elementoPadreId);
}