package ec.dalara.factucore.application.service;

import ec.dalara.factucore.domain.shared.EstadoRegistro;
import ec.dalara.factucore.infrastructure.persistence.entity.VersionDocumentoXsd;
import ec.dalara.factucore.infrastructure.persistence.repository.BaseRepository;
import ec.dalara.factucore.infrastructure.persistence.repository.VersionDocumentoXsdRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VersionDocumentoXsdService
        extends BaseService<VersionDocumentoXsd> {

    private final VersionDocumentoXsdRepository versionDocumentoXsdRepository;

    @Override
    protected BaseRepository<VersionDocumentoXsd, Long> getRepository() {
        return versionDocumentoXsdRepository;
    }

    public List<VersionDocumentoXsd> listarPorDocumentoXsd(
            Long documentoXsdId
    ) {
        return versionDocumentoXsdRepository
                .findByDocumentoXsdId(documentoXsdId)
                .stream()
                .filter(version ->
                        !EstadoRegistro.ELIMINADO.equals(
                                version.getEstadoRegistro()
                        )
                )
                .toList();
    }

    public Optional<VersionDocumentoXsd> obtenerPorDocumentoXsdYVersion(
            Long documentoXsdId,
            String version
    ) {
        return versionDocumentoXsdRepository
                .findByDocumentoXsdIdAndVersion(
                        documentoXsdId,
                        version
                )
                .filter(versionDocumento ->
                        EstadoRegistro.ACTIVO.equals(
                                versionDocumento.getEstadoRegistro()
                        )
                );
    }

    public boolean existePorDocumentoXsdYVersion(
            Long documentoXsdId,
            String version
    ) {
        return versionDocumentoXsdRepository
                .findByDocumentoXsdIdAndVersion(
                        documentoXsdId,
                        version
                )
                .filter(versionDocumento ->
                        !EstadoRegistro.ELIMINADO.equals(
                                versionDocumento.getEstadoRegistro()
                        )
                )
                .isPresent();
    }
}