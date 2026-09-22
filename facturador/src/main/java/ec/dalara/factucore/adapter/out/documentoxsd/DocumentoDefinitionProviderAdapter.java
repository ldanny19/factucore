package ec.dalara.factucore.adapter.out.documentoxsd;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Component;

import ec.dalara.factucore.application.port.out.DocumentoDefinitionProvider;
import ec.dalara.factucore.application.service.DocumentoXsdService;
import ec.dalara.factucore.application.service.VersionDocumentoXsdService;
import ec.dalara.factucore.domain.documentoxsd.DocumentDefinitionModel;
import ec.dalara.factucore.domain.documentoxsd.VersionDocumentoXsdModel;
import ec.dalara.factucore.infrastructure.mapper.entity.AtributoXsdEntityMapper;
import ec.dalara.factucore.infrastructure.mapper.entity.DocumentoXsdEntityMapper;
import ec.dalara.factucore.infrastructure.mapper.entity.ElementoXsdEntityMapper;
import ec.dalara.factucore.infrastructure.mapper.entity.EnumeracionXsdEntityMapper;
import ec.dalara.factucore.infrastructure.mapper.entity.MapeoXsdEntityMapper;
import ec.dalara.factucore.infrastructure.mapper.entity.VersionDocumentoXsdEntityMapper;
import ec.dalara.factucore.infrastructure.persistence.repository.AtributoXsdRepository;
import ec.dalara.factucore.infrastructure.persistence.repository.ElementoXsdRepository;
import ec.dalara.factucore.infrastructure.persistence.repository.EnumeracionXsdRepository;
import ec.dalara.factucore.infrastructure.persistence.repository.MapeoXsdRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DocumentoDefinitionProviderAdapter implements DocumentoDefinitionProvider {

    private final DocumentoXsdService documentoXsdService;
    private final VersionDocumentoXsdService versionService;
    private final DocumentoXsdEntityMapper documentoMapper;
    private final VersionDocumentoXsdEntityMapper versionMapper;
    private final ElementoXsdRepository elementoRepository;
    private final AtributoXsdRepository atributoRepository;
    private final EnumeracionXsdRepository enumeracionRepository;
    private final MapeoXsdRepository mapeoRepository;
    private final ElementoXsdEntityMapper elementoMapper;
    private final AtributoXsdEntityMapper atributoMapper;
    private final EnumeracionXsdEntityMapper enumeracionMapper;
    private final MapeoXsdEntityMapper mapeoMapper;

    @Override
    public Optional<VersionDocumentoXsdModel> obtenerVersionVigente(String codigoDocumento,
            LocalDateTime fechaEmision) {
        return documentoXsdService.obtenerPorCodigo(codigoDocumento)
                .flatMap(documento -> versionService.obtenerVigente(documento.getId(), fechaEmision))
                .map(versionMapper::toModel);
    }

    @Override
    public Optional<DocumentDefinitionModel> obtenerDefinicionVigente(String codigoDocumento,
            LocalDateTime fechaEmision) {
        return documentoXsdService.obtenerPorCodigo(codigoDocumento)
                .flatMap(documento -> versionService.obtenerVigente(documento.getId(), fechaEmision)
                        .map(version -> construir(documento, version)));
    }

    private DocumentDefinitionModel construir(ec.dalara.factucore.infrastructure.persistence.entity.DocumentoXsd documento,
            ec.dalara.factucore.infrastructure.persistence.entity.VersionDocumentoXsd version) {
        var elementos = elementoRepository.findByVersionDocumentoXsdId(version.getId()).stream()
                .map(elementoMapper::toModel).toList();

        var atributos = elementos.stream()
                .flatMap(elemento -> atributoRepository.findByElementoXsdId(elemento.getId()).stream())
                .map(atributoMapper::toModel).toList();

        var enumeraciones = elementos.stream()
                .flatMap(elemento -> enumeracionRepository.findByElementoXsdId(elemento.getId()).stream())
                .map(enumeracionMapper::toModel).toList();

        var mapeos = mapeoRepository.findByVersionDocumentoXsdIdAndEstadoRegistro(
                version.getId(), ec.dalara.factucore.domain.shared.EstadoRegistro.ACTIVO)
                .stream().map(mapeoMapper::toModel).toList();

        return new DocumentDefinitionModel(
                documentoMapper.toModel(documento),
                versionMapper.toModel(version),
                elementos, atributos, enumeraciones, mapeos);
    }
}
