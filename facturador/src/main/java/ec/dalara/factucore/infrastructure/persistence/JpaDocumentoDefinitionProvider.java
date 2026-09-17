package ec.dalara.factucore.infrastructure.persistence;

import ec.dalara.factucore.application.port.out.DocumentoDefinitionProvider;
import ec.dalara.factucore.domain.documentoxsd.VersionDocumentoXsdModel;
import ec.dalara.factucore.domain.shared.EstadoRegistro;
import ec.dalara.factucore.infrastructure.mapper.VersionDocumentoXsdMapper;
import ec.dalara.factucore.infrastructure.persistence.entity.DocumentoXsd;
import ec.dalara.factucore.infrastructure.persistence.repository.DocumentoXsdRepository;
import ec.dalara.factucore.infrastructure.persistence.repository.VersionDocumentoXsdRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JpaDocumentoDefinitionProvider
        implements DocumentoDefinitionProvider {

    private final DocumentoXsdRepository documentoXsdRepository;
    private final VersionDocumentoXsdRepository versionDocumentoXsdRepository;
    private final VersionDocumentoXsdMapper versionDocumentoXsdMapper;

    @Override
    public Optional<VersionDocumentoXsdModel> obtenerVersionVigente(
            String codigoDocumento,
            LocalDateTime fechaEmision
    ) {
        Optional<DocumentoXsd> documento = documentoXsdRepository
                .findByCodigo(codigoDocumento)
                .filter(documentoXsd ->
                        EstadoRegistro.ACTIVO.equals(
                                documentoXsd.getEstadoRegistro()
                        )
                );

        if (documento.isEmpty()) {
            return Optional.empty();
        }

        Long documentoXsdId = documento.get().getId();

        Optional<ec.dalara.factucore.infrastructure.persistence.entity.VersionDocumentoXsd> version =
                versionDocumentoXsdRepository
                        .findByDocumentoXsdIdAndEstadoRegistroAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(
                                documentoXsdId,
                                EstadoRegistro.ACTIVO,
                                fechaEmision,
                                fechaEmision
                        );

        if (version.isEmpty()) {
            version = versionDocumentoXsdRepository
                    .findByDocumentoXsdIdAndEstadoRegistroAndFechaInicioLessThanEqualAndFechaFinIsNull(
                            documentoXsdId,
                            EstadoRegistro.ACTIVO,
                            fechaEmision
                    );
        }

        return version.map(versionDocumentoXsdMapper::toModel);
    }
}