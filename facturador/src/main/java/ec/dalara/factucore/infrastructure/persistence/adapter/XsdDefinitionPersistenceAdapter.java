package ec.dalara.factucore.infrastructure.persistence.adapter;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ec.dalara.factucore.application.port.out.XsdDefinitionPersistencePort;
import ec.dalara.factucore.domain.documentoxsd.importacion.XsdAttributeSource;
import ec.dalara.factucore.domain.documentoxsd.importacion.XsdDefinitionSource;
import ec.dalara.factucore.domain.documentoxsd.importacion.XsdElementSource;
import ec.dalara.factucore.domain.documentoxsd.importacion.XsdEnumerationSource;
import ec.dalara.factucore.domain.documentoxsd.importacion.XsdImportRequest;
import ec.dalara.factucore.domain.documentoxsd.importacion.XsdImportResult;
import ec.dalara.factucore.infrastructure.InfrastructureException;
import ec.dalara.factucore.infrastructure.persistence.entity.AtributoXsd;
import ec.dalara.factucore.infrastructure.persistence.entity.DocumentoXsd;
import ec.dalara.factucore.infrastructure.persistence.entity.ElementoXsd;
import ec.dalara.factucore.infrastructure.persistence.entity.EnumeracionXsd;
import ec.dalara.factucore.infrastructure.persistence.entity.VersionDocumentoXsd;
import ec.dalara.factucore.infrastructure.persistence.repository.AtributoXsdRepository;
import ec.dalara.factucore.infrastructure.persistence.repository.DocumentoXsdRepository;
import ec.dalara.factucore.infrastructure.persistence.repository.ElementoXsdRepository;
import ec.dalara.factucore.infrastructure.persistence.repository.EnumeracionXsdRepository;
import ec.dalara.factucore.infrastructure.persistence.repository.VersionDocumentoXsdRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class XsdDefinitionPersistenceAdapter implements XsdDefinitionPersistencePort {

    private static final String ACTIVO = "ACTIVO";

    private final DocumentoXsdRepository documentoRepository;
    private final VersionDocumentoXsdRepository versionRepository;
    private final ElementoXsdRepository elementoRepository;
    private final AtributoXsdRepository atributoRepository;
    private final EnumeracionXsdRepository enumeracionRepository;

    @Override
    @Transactional
    public XsdImportResult persist(XsdImportRequest request, XsdDefinitionSource definition) {
        if (documentoRepository.existsByCodigo(request.codigo())) {
            throw new InfrastructureException(
                    "FACTUCORE.XSD.IMPORTACION.DOCUMENTO.EXISTENTE", request.codigo());
        }

        LocalDateTime ahora = LocalDateTime.now();

        DocumentoXsd documento = documentoRepository.save(DocumentoXsd.builder()
                .codigo(request.codigo())
                .nombre(request.nombre())
                .descripcion(request.descripcion())
                .tipoDocumento(request.tipoDocumento())
                .estadoRegistro(ACTIVO)
                .usuarioCreacion(request.usuario())
                .fechaCreacion(ahora)
                .observacion(request.observacion())
                .build());

        VersionDocumentoXsd version = versionRepository.save(VersionDocumentoXsd.builder()
                .documentoXsd(documento)
                .version(request.version())
                .nombreArchivo(request.nombreArchivo())
                .namespaceXml(definition.namespaceXml())
                .elementoRaiz(definition.elementoRaiz())
                .fechaInicio(request.fechaInicio())
                .fechaFin(request.fechaFin())
                .estadoRegistro(ACTIVO)
                .usuarioCreacion(request.usuario())
                .fechaCreacion(ahora)
                .observacion(request.observacion())
                .build());

        Map<String, ElementoXsd> elementosPorRuta = new HashMap<>();
        int elementos = 0;

        for (XsdElementSource source : definition.elementos()) {
            ElementoXsd padre = padreDe(source.ruta(), elementosPorRuta);

            ElementoXsd elemento = elementoRepository.save(ElementoXsd.builder()
                    .versionDocumentoXsd(version)
                    .elementoPadre(padre)
                    .nombre(source.nombre())
                    .tipoDato(source.tipoDato())
                    .orden(source.orden())
                    .obligatorio(source.esObligatorio())
                    .repetible(source.esRepetible())
                    .minOcurrencias(source.minOcurrencias())
                    .maxOcurrencias(source.maxOcurrencias())
                    .longitudMinima(source.longitudMinima())
                    .longitudMaxima(source.longitudMaxima())
                    .digitosTotales(source.digitosTotales())
                    .decimales(source.decimales())
                    .valorMinimo(source.valorMinimo())
                    .valorMaximo(source.valorMaximo())
                    .patron(source.patron())
                    .fechaInicio(request.fechaInicio())
                    .fechaFin(request.fechaFin())
                    .estadoRegistro(ACTIVO)
                    .usuarioCreacion(request.usuario())
                    .fechaCreacion(ahora)
                    .observacion(request.observacion())
                    .build());

            elementosPorRuta.put(source.ruta(), elemento);
            elementos++;
        }

        int atributos = 0;
        for (XsdAttributeSource source : definition.atributos()) {
            ElementoXsd elemento = elementoPorRuta(source.rutaElemento(), elementosPorRuta);

            atributoRepository.save(AtributoXsd.builder()
                    .elementoXsd(elemento)
                    .nombre(source.nombre())
                    .tipoDato(source.tipoDato())
                    .obligatorio(source.obligatorio())
                    .valorPredeterminado(source.valorPredeterminado())
                    .patron(source.patron())
                    .estadoRegistro(ACTIVO)
                    .usuarioCreacion(request.usuario())
                    .fechaCreacion(ahora)
                    .observacion(request.observacion())
                    .build());

            atributos++;
        }

        int enumeraciones = 0;
        for (XsdEnumerationSource source : definition.enumeraciones()) {
            ElementoXsd elemento = elementoPorRuta(source.rutaElemento(), elementosPorRuta);

            enumeracionRepository.save(EnumeracionXsd.builder()
                    .elementoXsd(elemento)
                    .valor(source.valor())
                    .descripcion(source.descripcion())
                    .orden(source.orden())
                    .estadoRegistro(ACTIVO)
                    .usuarioCreacion(request.usuario())
                    .fechaCreacion(ahora)
                    .observacion(request.observacion())
                    .build());

            enumeraciones++;
        }

        return new XsdImportResult(
                documento.getId(), version.getId(), elementos, atributos, enumeraciones);
    }

    private ElementoXsd padreDe(String ruta, Map<String, ElementoXsd> elementosPorRuta) {
        int separador = ruta.lastIndexOf('.');
        if (separador < 0) {
            return null;
        }
        return elementoPorRuta(ruta.substring(0, separador), elementosPorRuta);
    }

    private ElementoXsd elementoPorRuta(String ruta, Map<String, ElementoXsd> elementosPorRuta) {
        ElementoXsd elemento = elementosPorRuta.get(ruta);
        if (elemento == null) {
            throw new InfrastructureException(
                    "FACTUCORE.XSD.IMPORTACION.RUTA_ELEMENTO.NO_ENCONTRADA", ruta);
        }
        return elemento;
    }
}
