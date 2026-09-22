package ec.dalara.factucore.infrastructure.persistence.repository;

import ec.dalara.factucore.infrastructure.persistence.entity.MapeoXsd;

import java.util.List;

public interface MapeoXsdRepository
        extends BaseRepository<MapeoXsd, Long> {

    List<MapeoXsd> findByVersionDocumentoXsdIdAndEstadoRegistro(
            Long versionDocumentoXsdId,
            String estadoRegistro
    );
}