package ec.dalara.factucore.infrastructure.persistence;

import ec.dalara.factucore.application.port.out.DocumentoDefinitionProvider;
import ec.dalara.factucore.domain.documentoxsd.VersionDocumentoXsdModel;
import ec.dalara.factucore.domain.shared.EstadoRegistro;
import ec.dalara.factucore.infrastructure.persistence.entity.DocumentoXsd;
import ec.dalara.factucore.infrastructure.persistence.entity.VersionDocumentoXsd;
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

    @Override
    public Optional<VersionDocumentoXsdModel> obtenerVersionVigente(
            String codigoDocumento,
            LocalDateTime fechaEmision
    ) {
        if (codigoDocumento == null || codigoDocumento.isBlank()) {
            return Optional.empty();
        }

        if (fechaEmision == null) {
            return Optional.empty();
        }

        Optional<DocumentoXsd> documentoOptional =
                documentoXsdRepository
                        .findByCodigo(codigoDocumento)
                        .filter(documento ->
                                EstadoRegistro.ACTIVO.equals(
                                        documento.getEstadoRegistro()
                                )
                        );

        if (documentoOptional.isEmpty()) {
            return Optional.empty();
        }

        DocumentoXsd documento = documentoOptional.get();

        Optional<VersionDocumentoXsd> versionOptional =
                versionDocumentoXsdRepository
                        .findByDocumentoXsdIdAndEstadoRegistroAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(
                                documento.getId(),
                                EstadoRegistro.ACTIVO,
                                fechaEmision,
                                fechaEmision
                        );

        if (versionOptional.isEmpty()) {
            versionOptional =
                    versionDocumentoXsdRepository
                            .findByDocumentoXsdIdAndEstadoRegistroAndFechaInicioLessThanEqualAndFechaFinIsNull(
                                    documento.getId(),
                                    EstadoRegistro.ACTIVO,
                                    fechaEmision
                            );
        }

        if (versionOptional.isEmpty()) {
            return Optional.empty();
        }

        VersionDocumentoXsd version = versionOptional.get();

        return Optional.of(
                new VersionDocumentoXsdModel(
                        version.getId(),
                        documento.getId(),
                        version.getVersion(),
                        version.getVersion(),
                        version.getFechaInicio(),
                        version.getFechaFin()
                )
        );
    }
}