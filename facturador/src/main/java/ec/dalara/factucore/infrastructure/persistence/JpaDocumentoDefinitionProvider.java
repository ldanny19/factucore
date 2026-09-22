package ec.dalara.factucore.infrastructure.persistence;

import ec.dalara.factucore.application.port.out.DocumentoDefinitionProvider;
import ec.dalara.factucore.domain.documentoxsd.AtributoXsdModel;
import ec.dalara.factucore.domain.documentoxsd.DocumentDefinitionModel;
import ec.dalara.factucore.domain.documentoxsd.DocumentoXsdModel;
import ec.dalara.factucore.domain.documentoxsd.ElementoXsdModel;
import ec.dalara.factucore.domain.documentoxsd.EnumeracionXsdModel;
import ec.dalara.factucore.domain.documentoxsd.VersionDocumentoXsdModel;
import ec.dalara.factucore.domain.documentoxsd.MapeoXsdModel;
import ec.dalara.factucore.domain.shared.EstadoRegistro;
import ec.dalara.factucore.infrastructure.persistence.entity.AtributoXsd;
import ec.dalara.factucore.infrastructure.persistence.entity.DocumentoXsd;
import ec.dalara.factucore.infrastructure.persistence.entity.ElementoXsd;
import ec.dalara.factucore.infrastructure.persistence.entity.EnumeracionXsd;
import ec.dalara.factucore.infrastructure.persistence.entity.VersionDocumentoXsd;
import ec.dalara.factucore.infrastructure.persistence.entity.MapeoXsd;
import ec.dalara.factucore.infrastructure.persistence.repository.AtributoXsdRepository;
import ec.dalara.factucore.infrastructure.persistence.repository.DocumentoXsdRepository;
import ec.dalara.factucore.infrastructure.persistence.repository.ElementoXsdRepository;
import ec.dalara.factucore.infrastructure.persistence.repository.EnumeracionXsdRepository;
import ec.dalara.factucore.infrastructure.persistence.repository.VersionDocumentoXsdRepository;
import ec.dalara.factucore.infrastructure.persistence.repository.MapeoXsdRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JpaDocumentoDefinitionProvider
        implements DocumentoDefinitionProvider {

    private final DocumentoXsdRepository documentoXsdRepository;
    private final VersionDocumentoXsdRepository versionDocumentoXsdRepository;
    private final ElementoXsdRepository elementoXsdRepository;
    private final AtributoXsdRepository atributoXsdRepository;
    private final EnumeracionXsdRepository enumeracionXsdRepository;
    private final MapeoXsdRepository mapeoXsdRepository;

    @Override
    public Optional<VersionDocumentoXsdModel> obtenerVersionVigente(
            String codigoDocumento,
            LocalDateTime fechaEmision
    ) {
        return obtenerEntidadVersionVigente(
                codigoDocumento,
                fechaEmision
        ).map(this::crearVersionModel);
    }

    @Override
    public Optional<DocumentDefinitionModel> obtenerDefinicionVigente(
            String codigoDocumento,
            LocalDateTime fechaEmision
    ) {
        Optional<VersionDocumentoXsd> versionOptional =
                obtenerEntidadVersionVigente(
                        codigoDocumento,
                        fechaEmision
                );

        if (versionOptional.isEmpty()) {
            return Optional.empty();
        }

        VersionDocumentoXsd version =
                versionOptional.get();

        DocumentoXsd documento =
                version.getDocumentoXsd();

        List<ElementoXsd> elementos =
                elementoXsdRepository
                        .findByVersionDocumentoXsdId(version.getId())
                        .stream()
                        .filter(elemento ->
                                EstadoRegistro.ACTIVO.equals(
                                        elemento.getEstadoRegistro()
                                )
                        )
                        .toList();

        List<ElementoXsdModel> elementoModels =
                elementos.stream()
                        .map(this::crearElementoModel)
                        .toList();

        List<AtributoXsdModel> atributoModels =
                elementos.stream()
                        .flatMap(elemento ->
                                atributoXsdRepository
                                        .findByElementoXsdId(
                                                elemento.getId()
                                        )
                                        .stream()
                        )
                        .filter(atributo ->
                                EstadoRegistro.ACTIVO.equals(
                                        atributo.getEstadoRegistro()
                                )
                        )
                        .map(this::crearAtributoModel)
                        .toList();

        List<EnumeracionXsdModel> enumeracionModels =
                elementos.stream()
                        .flatMap(elemento ->
                                enumeracionXsdRepository
                                        .findByElementoXsdId(
                                                elemento.getId()
                                        )
                                        .stream()
                        )
                        .filter(enumeracion ->
                                EstadoRegistro.ACTIVO.equals(
                                        enumeracion.getEstadoRegistro()
                                )
                        )
                        .map(this::crearEnumeracionModel)
                        .toList();

        List<MapeoXsdModel> mapeoModels =
                mapeoXsdRepository
                        .findByVersionDocumentoXsdIdAndEstadoRegistro(
                                version.getId(),
                                EstadoRegistro.ACTIVO
                        )
                        .stream()
                        .map(this::crearMapeoModel)
                        .toList();

        DocumentoXsdModel documentoModel =
                new DocumentoXsdModel(
                        documento.getCodigo(),
                        documento.getNombre(),
                        documento.getDescripcion(),
                        documento.getTipoDocumento()
                );

        VersionDocumentoXsdModel versionModel =
                crearVersionModel(version);

        return Optional.of(
                new DocumentDefinitionModel(
                        documentoModel,
                        versionModel,
                        elementoModels,
                        atributoModels,
                        enumeracionModels,
                        mapeoModels
                )
        );
    }

    private Optional<VersionDocumentoXsd> obtenerEntidadVersionVigente(
            String codigoDocumento,
            LocalDateTime fechaEmision
    ) {
        if (codigoDocumento == null
                || codigoDocumento.isBlank()
                || fechaEmision == null) {
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

        DocumentoXsd documento =
                documentoOptional.get();

        Optional<VersionDocumentoXsd> version =
                versionDocumentoXsdRepository
                        .findByDocumentoXsdIdAndEstadoRegistroAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(
                                documento.getId(),
                                EstadoRegistro.ACTIVO,
                                fechaEmision,
                                fechaEmision
                        );

        if (version.isPresent()) {
            return version;
        }

        return versionDocumentoXsdRepository
                .findByDocumentoXsdIdAndEstadoRegistroAndFechaInicioLessThanEqualAndFechaFinIsNull(
                        documento.getId(),
                        EstadoRegistro.ACTIVO,
                        fechaEmision
                );
    }

    private VersionDocumentoXsdModel crearVersionModel(
            VersionDocumentoXsd version
    ) {
        return new VersionDocumentoXsdModel(
                version.getId(),
                version.getDocumentoXsd().getId(),
                version.getVersion(),
                version.getNamespaceXml(),
                version.getElementoRaiz(),
                version.getFechaInicio(),
                version.getFechaFin()
        );
    }

    private ElementoXsdModel crearElementoModel(
            ElementoXsd elemento
    ) {
        return new ElementoXsdModel(
                elemento.getId(),
                elemento.getVersionDocumentoXsd().getId(),
                elemento.getElementoPadre() == null
                        ? null
                        : elemento.getElementoPadre().getId(),
                elemento.getNombre(),
                elemento.getTipoDato(),
                elemento.getOrden(),
                elemento.getObligatorio(),
                elemento.getRepetible(),
                elemento.getMinOcurrencias(),
                elemento.getMaxOcurrencias(),
                elemento.getLongitudMinima(),
                elemento.getLongitudMaxima(),
                elemento.getDigitosTotales(),
                elemento.getDecimales(),
                elemento.getValorMinimo(),
                elemento.getValorMaximo(),
                elemento.getPatron()
        );
    }

    private AtributoXsdModel crearAtributoModel(
            AtributoXsd atributo
    ) {
        return new AtributoXsdModel(
                atributo.getId(),
                atributo.getElementoXsd().getId(),
                atributo.getNombre(),
                atributo.getTipoDato(),
                atributo.getObligatorio(),
                atributo.getValorPredeterminado(),
                atributo.getPatron()
        );
    }

    private EnumeracionXsdModel crearEnumeracionModel(
            EnumeracionXsd enumeracion
    ) {
        return new EnumeracionXsdModel(
                enumeracion.getElementoXsd().getId(),
                enumeracion.getValor(),
                enumeracion.getDescripcion(),
                enumeracion.getOrden()
        );
    }

    private MapeoXsdModel crearMapeoModel(
            MapeoXsd mapeo
    ) {
        return new MapeoXsdModel(
                mapeo.getId(),
                mapeo.getVersionDocumentoXsd().getId(),
                mapeo.getRutaOrigen(),
                mapeo.getElementoXsd() == null
                        ? null
                        : mapeo.getElementoXsd().getId(),
                mapeo.getAtributoXsd() == null
                        ? null
                        : mapeo.getAtributoXsd().getId(),
                mapeo.getTipoMapeo()
        );
    }
}