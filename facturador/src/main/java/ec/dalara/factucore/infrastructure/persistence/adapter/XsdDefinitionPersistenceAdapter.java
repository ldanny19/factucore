package ec.dalara.factucore.infrastructure.persistence.adapter;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ec.dalara.factucore.application.port.out.XsdDefinitionPersistencePort;
import ec.dalara.factucore.domain.documentoxsd.importacion.XsdAttributeSource;
import ec.dalara.factucore.domain.documentoxsd.importacion.XsdDefinitionSource;
import ec.dalara.factucore.domain.documentoxsd.importacion.XsdElementSource;
import ec.dalara.factucore.domain.documentoxsd.importacion.XsdEnumerationSource;
import ec.dalara.factucore.domain.documentoxsd.importacion.XsdImportRequest;
import ec.dalara.factucore.domain.documentoxsd.importacion.XsdImportResult;
import ec.dalara.factucore.domain.shared.EstadoRegistro;
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

	private final DocumentoXsdRepository documentoRepository;
	private final VersionDocumentoXsdRepository versionRepository;
	private final ElementoXsdRepository elementoRepository;
	private final AtributoXsdRepository atributoRepository;
	private final EnumeracionXsdRepository enumeracionRepository;
	private final ObjectMapper objectMapper;

	@Override
	@Transactional
	public XsdImportResult persist(XsdImportRequest request, XsdDefinitionSource definition) {
		LocalDateTime ahora = LocalDateTime.now();

		DocumentoXsd documento = documentoRepository.findByCodigo(request.codigo()).orElseGet(() -> documentoRepository
				.save(DocumentoXsd.builder().codigo(request.codigo()).nombre(request.nombre()).descripcion(request.descripcion())
						.tipoDocumento(request.tipoDocumento()).prefijoArchivo(request.prefijoArchivo())
						.estadoRegistro(EstadoRegistro.ACTIVO).usuarioCreacion(request.usuario()).fechaCreacion(ahora)
						.observacion(request.observacion()).build()));

		if (versionRepository.existsByDocumentoXsdIdAndVersion(documento.getId(), request.version())) {
			throw new InfrastructureException("FACTUCORE.VERSION_DOCUMENTO_XSD.DUPLICADA", request.version());
		}

		if (versionRepository.existsByDocumentoXsdIdAndEstadoRegistroAndRangoFechas(documento.getId(),
				EstadoRegistro.ACTIVO, request.fechaInicio(), request.fechaFin())) {
			throw new InfrastructureException("FACTUCORE.VERSION_DOCUMENTO_XSD.RANGO_FECHAS.INVALIDO",
					request.version(), request.fechaInicio(), request.fechaFin());
		}

		VersionDocumentoXsd version = VersionDocumentoXsd.builder().documentoXsd(documento).version(request.version())
				.nombreArchivo(request.nombreArchivo()).namespaceXml(definition.namespaceXml())
				.elementoRaiz(definition.elementoRaiz()).plantillaJson(generarPlantillaJson(definition))
				.esquemaJson(generarEsquemaJson(definition)).fechaInicio(request.fechaInicio())
				.fechaFin(request.fechaFin()).estadoRegistro(EstadoRegistro.ACTIVO).usuarioCreacion(request.usuario())
				.fechaCreacion(ahora).observacion(request.observacion()).build();

		version = versionRepository.save(version);

		Map<String, ElementoXsd> elementosPorRuta = new HashMap<>();
		int elementos = 0;

		for (XsdElementSource source : definition.elementos()) {
			ElementoXsd padre = padreDe(source.ruta(), elementosPorRuta);

			ElementoXsd elemento = elementoRepository.save(ElementoXsd.builder().versionDocumentoXsd(version)
					.elementoPadre(padre).nombre(source.nombre()).tipoDato(source.tipoDato()).orden(source.orden())
					.obligatorio(source.esObligatorio()).repetible(source.esRepetible())
					.minOcurrencias(source.minOcurrencias()).maxOcurrencias(source.maxOcurrencias())
					.longitudMinima(source.longitudMinima()).longitudMaxima(source.longitudMaxima())
					.digitosTotales(source.digitosTotales()).decimales(source.decimales())
					.valorMinimo(source.valorMinimo()).valorMaximo(source.valorMaximo()).patron(source.patron())
					.fechaInicio(request.fechaInicio()).fechaFin(request.fechaFin())
					.estadoRegistro(EstadoRegistro.ACTIVO).usuarioCreacion(request.usuario()).fechaCreacion(ahora)
					.observacion(request.observacion()).build());

			elementosPorRuta.put(source.ruta(), elemento);
			elementos++;
		}

		int atributos = 0;
		for (XsdAttributeSource source : definition.atributos()) {
			ElementoXsd elemento = elementoPorRuta(source.rutaElemento(), elementosPorRuta);

			atributoRepository.save(AtributoXsd.builder().elementoXsd(elemento).nombre(source.nombre())
					.tipoDato(source.tipoDato()).obligatorio(source.obligatorio())
					.valorPredeterminado(source.valorPredeterminado()).patron(source.patron())
					.estadoRegistro(EstadoRegistro.ACTIVO).usuarioCreacion(request.usuario()).fechaCreacion(ahora)
					.observacion(request.observacion()).build());

			atributos++;
		}

		int enumeraciones = 0;
		for (XsdEnumerationSource source : definition.enumeraciones()) {
			ElementoXsd elemento = elementoPorRuta(source.rutaElemento(), elementosPorRuta);

			enumeracionRepository.save(EnumeracionXsd.builder().elementoXsd(elemento).valor(source.valor())
					.descripcion(source.descripcion()).orden(source.orden()).estadoRegistro(EstadoRegistro.ACTIVO)
					.usuarioCreacion(request.usuario()).fechaCreacion(ahora).observacion(request.observacion())
					.build());

			enumeraciones++;
		}

		return new XsdImportResult(documento.getId(), version.getId(), elementos, atributos, enumeraciones);
	}

	private String generarPlantillaJson(XsdDefinitionSource definition) {
		try {
			ObjectNode root = objectMapper.createObjectNode();
			Map<String, JsonNode> nodos = new HashMap<>();

			for (XsdElementSource source : elementosEntrada(definition)) {
				JsonNode valor = valorPlantilla(source, definition);
				String rutaPadre = rutaPadre(source.ruta());

				if (rutaPadre == null) {
					root.set(source.nombre(), valor);
					nodos.put(source.ruta(), valor);
					continue;
				}

				JsonNode padre = nodos.get(rutaPadre);
				if (padre == null) {
					throw new InfrastructureException("FACTUCORE.XSD.IMPORTACION.RUTA_ELEMENTO.NO_ENCONTRADA",
							rutaPadre);
				}

				ObjectNode objetoPadre = objetoContenedor(padre);
				objetoPadre.set(source.nombre(), valor);
				nodos.put(source.ruta(), valor);
			}

			for (XsdAttributeSource attribute : definition.atributos()) {
				if (esSeccionFactuCore(attribute.rutaElemento(), definition)) {
					continue;
				}
				JsonNode elemento = nodos.get(attribute.rutaElemento());
				if (elemento == null) {
					throw new InfrastructureException("FACTUCORE.XSD.IMPORTACION.RUTA_ELEMENTO.NO_ENCONTRADA",
							attribute.rutaElemento());
				}
				ObjectNode objeto = objetoContenedor(elemento);
				objeto.set(attribute.nombre(), valorEjemplo(attribute.tipoDato()));
			}
			return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
		} catch (InfrastructureException exception) {
			throw exception;
		} catch (Exception exception) {
			throw new InfrastructureException("FACTUCORE.XSD.JSON.PLANTILLA.ERROR", exception);
		}
	}

	private JsonNode valorPlantilla(XsdElementSource source, XsdDefinitionSource definition) {
		boolean tieneEstructuraObjeto = tieneEstructuraObjeto(source, definition);
		JsonNode valor;

		if (tieneEstructuraObjeto) {
			ObjectNode objeto = objectMapper.createObjectNode();
			if (!tieneHijos(source.ruta(), definition)) {
				objeto.set("valor", valorEjemplo(source.tipoDato()));
			}
			valor = objeto;
		} else {
			valor = valorEjemplo(source.tipoDato());
		}

		if (source.esRepetible()) {
			ArrayNode array = objectMapper.createArrayNode();
			array.add(valor);
			return array;
		}

		return valor;
	}

	private JsonNode valorEjemplo(String tipoDato) {
		String tipo = tipoDato == null ? "" : tipoDato.toLowerCase();

		if (tipo.contains("boolean")) {
			return objectMapper.getNodeFactory().booleanNode(false);
		}
		if (tipo.contains("decimal") || tipo.contains("double") || tipo.contains("float") || tipo.contains("integer")
				|| tipo.contains("int") || tipo.contains("long") || tipo.contains("short") || tipo.contains("byte")) {
			return objectMapper.getNodeFactory().numberNode(0);
		}

		return objectMapper.getNodeFactory().textNode("");
	}

	private String generarEsquemaJson(XsdDefinitionSource definition) {
		try {
			ObjectNode schema = objectMapper.createObjectNode();
			schema.put("$schema", "https://json-schema.org/draft/2020-12/schema");
			schema.put("title", definition.elementoRaiz());
			schema.put("type", "object");

			ObjectNode rootProperties = schema.putObject("properties");
			Set<String> rootRequired = new HashSet<>();

			for (XsdElementSource source : elementosEntrada(definition)) {
				if (rutaPadre(source.ruta()) != null) {
					continue;
				}
				agregarEsquemaElemento(rootProperties, rootRequired, source, definition);
			}

			if (!rootRequired.isEmpty()) {
				ArrayNode required = schema.putArray("required");
				rootRequired.forEach(required::add);
			}

			return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema);
		} catch (Exception exception) {
			throw new InfrastructureException("FACTUCORE.XSD.JSON.ESQUEMA.ERROR", exception);
		}
	}

	private void agregarEsquemaElemento(ObjectNode properties, Set<String> required, XsdElementSource source,
			XsdDefinitionSource definition) {
		boolean tieneEstructuraObjeto = tieneEstructuraObjeto(source, definition);
		ObjectNode elementoSchema = esquemaElemento(source, tieneEstructuraObjeto, definition);

		properties.set(source.nombre(), elementoSchema);

		if (source.esObligatorio()) {
			required.add(source.nombre());
		}
	}

	private ObjectNode esquemaElemento(XsdElementSource source, boolean tieneEstructuraObjeto, XsdDefinitionSource definition) {
		ObjectNode base = objectMapper.createObjectNode();

		if (source.esRepetible()) {
			base.put("type", "array");
			if (source.minOcurrencias() != null) {
				base.put("minItems", source.minOcurrencias());
			}
			if (source.maxOcurrencias() != null) {
				base.put("maxItems", source.maxOcurrencias());
			}
			ObjectNode items = base.putObject("items");
			construirTipo(items, source, tieneEstructuraObjeto, definition);
			aplicarRestricciones(items, source, definition);
		} else {
			construirTipo(base, source, tieneEstructuraObjeto, definition);
			aplicarRestricciones(base, source, definition);
		}

		return base;
	}

	private void aplicarRestricciones(ObjectNode target, XsdElementSource source, XsdDefinitionSource definition) {
		if (source.longitudMinima() != null) {
			target.put("minLength", source.longitudMinima());
		}
		if (source.longitudMaxima() != null) {
			target.put("maxLength", source.longitudMaxima());
		}
		if (source.digitosTotales() != null) {
			target.put("totalDigits", source.digitosTotales());
		}
		if (source.decimales() != null) {
			target.put("fractionDigits", source.decimales());
		}
		if (source.valorMinimo() != null) {
			target.put("minimum", source.valorMinimo());
		}
		if (source.valorMaximo() != null) {
			target.put("maximum", source.valorMaximo());
		}
		if (source.patron() != null) {
			target.put("pattern", source.patron());
		}

		List<XsdEnumerationSource> enumeraciones = definition.enumeraciones().stream()
				.filter(e -> source.ruta().equals(e.rutaElemento())).toList();
		if (!enumeraciones.isEmpty()) {
			ArrayNode enumeration = target.putArray("enum");
			enumeraciones.forEach(e -> enumeration.add(e.valor()));
		}
	}

	private void construirTipo(ObjectNode target, XsdElementSource source, boolean tieneEstructuraObjeto,
			XsdDefinitionSource definition) {
		if (!tieneEstructuraObjeto) {
			target.put("type", tipoJson(source.tipoDato()));
			return;
		}

		target.put("type", "object");
		ObjectNode properties = target.putObject("properties");
		if (!tieneHijos(source.ruta(), definition)) {
			ObjectNode valorSchema = objectMapper.createObjectNode();
			valorSchema.put("type", tipoJson(source.tipoDato()));
			properties.set("valor", valorSchema);
		}
		Set<String> required = new HashSet<>();

		for (XsdElementSource child : elementosEntrada(definition)) {
			if (!source.ruta().equals(rutaPadre(child.ruta()))) {
				continue;
			}
			agregarEsquemaElemento(properties, required, child, definition);
		}

		for (XsdAttributeSource attribute : definition.atributos()) {
			if (esSeccionFactuCore(attribute.rutaElemento(), definition)
					|| !source.ruta().equals(attribute.rutaElemento())) {
				continue;
			}
			ObjectNode attributeSchema = objectMapper.createObjectNode();
			attributeSchema.put("type", tipoJson(attribute.tipoDato()));
			if (attribute.patron() != null) {
				attributeSchema.put("pattern", attribute.patron());
			}
			properties.set(attribute.nombre(), attributeSchema);
			if (attribute.obligatorio()) {
				required.add(attribute.nombre());
			}
		}

		if (!required.isEmpty()) {
			ArrayNode requiredNode = target.putArray("required");
			required.forEach(requiredNode::add);
		}
	}

	private String tipoJson(String tipoDato) {
		String tipo = tipoDato == null ? "" : tipoDato.toLowerCase();

		if (tipo.contains("boolean")) {
			return "boolean";
		}
		if (tipo.contains("decimal") || tipo.contains("double") || tipo.contains("float")) {
			return "number";
		}
		if (tipo.contains("integer") || tipo.contains("int") || tipo.contains("long") || tipo.contains("short")
				|| tipo.contains("byte")) {
			return "integer";
		}
		return "string";
	}

	private boolean tieneEstructuraObjeto(XsdElementSource source, XsdDefinitionSource definition) {
		return tieneHijos(source.ruta(), definition)
				|| definition.atributos().stream()
						.anyMatch(a -> !esSeccionFactuCore(a.rutaElemento(), definition)
								&& source.ruta().equals(a.rutaElemento()));
	}

	private boolean tieneHijos(String ruta, XsdDefinitionSource definition) {
		String prefijo = ruta + ".";
		return elementosEntrada(definition).stream().anyMatch(e -> e.ruta().startsWith(prefijo));
	}

	private List<XsdElementSource> elementosEntrada(XsdDefinitionSource definition) {
		return definition.elementos().stream()
				.filter(source -> !esSeccionFactuCore(source.ruta(), definition))
				.toList();
	}

	private boolean esSeccionFactuCore(String ruta, XsdDefinitionSource definition) {
		String raiz = definition.elementoRaiz();
		return "factura".equals(raiz)
				&& (ruta.equals(raiz + ".infoTributaria")
						|| ruta.startsWith(raiz + ".infoTributaria."));
	}

	private ObjectNode objetoContenedor(JsonNode nodo) {
		if (nodo.isArray()) {
			if (nodo.isEmpty()) {
				((ArrayNode) nodo).add(objectMapper.createObjectNode());
			}
			return (ObjectNode) nodo.get(0);
		}
		return (ObjectNode) nodo;
	}

	private String rutaPadre(String ruta) {
		int separador = ruta.lastIndexOf('.');
		return separador < 0 ? null : ruta.substring(0, separador);
	}

	private ElementoXsd padreDe(String ruta, Map<String, ElementoXsd> elementosPorRuta) {
		String padre = rutaPadre(ruta);
		return padre == null ? null : elementoPorRuta(padre, elementosPorRuta);
	}

	private ElementoXsd elementoPorRuta(String ruta, Map<String, ElementoXsd> elementosPorRuta) {
		ElementoXsd elemento = elementosPorRuta.get(ruta);
		if (elemento == null) {
			throw new InfrastructureException("FACTUCORE.XSD.IMPORTACION.RUTA_ELEMENTO.NO_ENCONTRADA", ruta);
		}
		return elemento;
	}
}
