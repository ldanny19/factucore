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

		DocumentoXsd documento = documentoRepository.findByCodigoForUpdate(request.codigo())
				.orElseGet(() -> documentoRepository.save(DocumentoXsd.builder().codigo(request.codigo())
						.nombre(request.nombre()).descripcion(request.descripcion())
						.tipoDocumento(request.tipoDocumento()).prefijoArchivo(request.prefijoArchivo())
						.estadoRegistro(EstadoRegistro.ACTIVO).usuarioCreacion(request.usuario()).fechaCreacion(ahora)
						.observacion(request.observacion()).build()));

		if (versionRepository.existsByDocumentoXsdIdAndVersion(documento.getId(), request.version())) {
			throw new InfrastructureException("FACTUCORE.VERSION_DOCUMENTO_XSD.DUPLICADA", request.version());
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
					.elementoPadre(padre).nombre(source.nombre())
					.tipoDato(tipoDatoRuntime(source, definition)).orden(source.orden())
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


	private String tipoDatoRuntime(XsdElementSource source, XsdDefinitionSource definition) {
		if (source.esRepetible()) {
			return "LIST";
		}

		if (tieneEstructuraObjeto(source, definition)) {
			return "MAP";
		}

		return tipoDatoEscalar(source.tipoDato());
	}

	private String tipoDatoEscalar(String tipoDatoXsd) {
		String tipo = tipoDatoXsd == null ? "" : tipoDatoXsd.trim().toLowerCase();

		int separador = tipo.lastIndexOf(':');
		if (separador >= 0) {
			tipo = tipo.substring(separador + 1);
		}

		return switch (tipo) {
		case "boolean" -> "BOOLEAN";
		case "decimal" -> "DECIMAL";
		case "double" -> "DOUBLE";
		case "float" -> "FLOAT";
		case "integer" -> "INTEGER";
		case "int", "unsignedint", "nonnegativeinteger", "positiveinteger", "negativeinteger",
				"nonpositiveinteger" -> "INTEGER";
		case "long", "unsignedlong" -> "LONG";
		case "short", "unsignedshort" -> "SHORT";
		case "byte", "unsignedbyte" -> "BYTE";
		case "datetime" -> "DATETIME";
		case "date" -> "DATE";
		default -> "STRING";
		};
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
				if (esSeccionFactuCore(attribute.rutaElemento(), definition)
						|| esAtributoTecnico(attribute, definition)) {
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
			schema.put("additionalProperties", false);
			Set<String> rootRequired = new HashSet<>();

			for (XsdElementSource source : elementosEsquema(definition)) {
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
		boolean tieneEstructuraObjeto = tieneEstructuraObjetoEsquema(source, definition);
		ObjectNode elementoSchema = esquemaElemento(source, tieneEstructuraObjeto, definition);

		properties.set(source.nombre(), elementoSchema);

		if (source.esObligatorio()) {
			required.add(source.nombre());
		}
	}

	private ObjectNode esquemaElemento(XsdElementSource source, boolean tieneEstructuraObjeto,
			XsdDefinitionSource definition) {
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
		String tipo = tipoJson(source.tipoDato());
		if ("string".equals(tipo)) {
			if (source.longitudMinima() != null) {
				target.put("minLength", source.longitudMinima());
			}
			if (source.longitudMaxima() != null) {
				target.put("maxLength", source.longitudMaxima());
			}
			if (source.patron() != null && !source.patron().isBlank()) {
				target.put("pattern", source.patron());
			}
		} else if ("number".equals(tipo) || "integer".equals(tipo)) {
			if (source.valorMinimo() != null) {
				target.put("minimum", source.valorMinimo());
			}
			if (source.valorMaximo() != null) {
				target.put("maximum", source.valorMaximo());
			}
			if (source.digitosTotales() != null && source.digitosTotales() > 0) {
				int decimales = source.decimales() == null ? 0 : source.decimales();
				int digitosEnteros = Math.max(0, source.digitosTotales() - decimales);
				java.math.BigDecimal limite = java.math.BigDecimal.TEN.pow(digitosEnteros);
				target.put("exclusiveMinimum", limite.negate());
				target.put("exclusiveMaximum", limite);
			}
			if (source.decimales() != null && source.decimales() >= 0 && "number".equals(tipo)) {
				target.put("multipleOf", java.math.BigDecimal.ONE.movePointLeft(source.decimales()));
			}
		}

		List<XsdEnumerationSource> enumeraciones = definition.enumeraciones().stream()
				.filter(e -> source.ruta().equals(e.rutaElemento())).toList();
		if (!enumeraciones.isEmpty()) {
			ArrayNode enumeration = target.putArray("enum");
			for (XsdEnumerationSource valor : enumeraciones) {
				if ("integer".equals(tipo)) {
					try {
						enumeration.add(new java.math.BigInteger(valor.valor()));
					} catch (NumberFormatException exception) {
						enumeration.add(valor.valor());
					}
				} else if ("number".equals(tipo)) {
					try {
						enumeration.add(new java.math.BigDecimal(valor.valor()));
					} catch (NumberFormatException exception) {
						enumeration.add(valor.valor());
					}
				} else {
					enumeration.add(valor.valor());
				}
			}
		}
	}
	private void construirTipo(ObjectNode target, XsdElementSource source, boolean tieneEstructuraObjeto,
			XsdDefinitionSource definition) {
		if (!tieneEstructuraObjeto) {
			String tipo = tipoJson(source.tipoDato());
			target.put("type", tipo);
			String tipoXsd = source.tipoDato() == null ? "" : source.tipoDato().trim().toLowerCase();
			int separador = tipoXsd.lastIndexOf(':');
			if (separador >= 0) {
				tipoXsd = tipoXsd.substring(separador + 1);
			}
			if ("date".equals(tipoXsd)) {
				target.put("format", "date");
			} else if ("datetime".equals(tipoXsd)) {
				target.put("format", "date-time");
			} else if ("time".equals(tipoXsd)) {
				target.put("format", "time");
			}
			return;
		}

		target.put("type", "object");
		target.put("additionalProperties", false);
		ObjectNode properties = target.putObject("properties");
		if (!tieneHijosEsquema(source.ruta(), definition)) {
			ObjectNode valorSchema = objectMapper.createObjectNode();
			valorSchema.put("type", tipoJson(source.tipoDato()));
			properties.set("valor", valorSchema);
		}
		Set<String> required = new HashSet<>();

		for (XsdElementSource child : elementosEsquema(definition)) {
			if (!source.ruta().equals(rutaPadre(child.ruta()))) {
				continue;
			}
			agregarEsquemaElemento(properties, required, child, definition);
		}

		for (XsdAttributeSource attribute : definition.atributos()) {
			if (esAtributoExcluidoEsquema(attribute, definition)
					|| !source.ruta().equals(attribute.rutaElemento())) {
				continue;
			}
			ObjectNode attributeSchema = objectMapper.createObjectNode();
			attributeSchema.put("type", tipoJson(attribute.tipoDato()));
			if ("string".equals(tipoJson(attribute.tipoDato())) && attribute.patron() != null
					&& !attribute.patron().isBlank()) {
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

	private List<XsdElementSource> elementosEsquema(XsdDefinitionSource definition) {
		return definition.elementos().stream().filter(source -> !contieneTagExcluido(source.ruta())).toList();
	}

	private boolean tieneEstructuraObjetoEsquema(XsdElementSource source, XsdDefinitionSource definition) {
		return tieneHijosEsquema(source.ruta(), definition)
				|| definition.atributos().stream().anyMatch(attribute -> !esAtributoExcluidoEsquema(attribute, definition)
						&& source.ruta().equals(attribute.rutaElemento()));
	}

	private boolean tieneHijosEsquema(String ruta, XsdDefinitionSource definition) {
		String prefijo = ruta + ".";
		return elementosEsquema(definition).stream().anyMatch(elemento -> elemento.ruta().startsWith(prefijo));
	}

	private boolean esAtributoExcluidoEsquema(XsdAttributeSource attribute, XsdDefinitionSource definition) {
		return contieneTagExcluido(attribute.rutaElemento()) || esAtributoTecnico(attribute, definition);
	}

	private boolean contieneTagExcluido(String ruta) {
		for (String segmento : ruta.split("\\.")) {
			if ("infoTributaria".equalsIgnoreCase(segmento) || "signature".equalsIgnoreCase(segmento)) {
				return true;
			}
		}
		return false;
	}

	private String tipoJson(String tipoDato) {
		String tipo = tipoDato == null ? "" : tipoDato.trim().toLowerCase();
		int separador = tipo.lastIndexOf(':');
		if (separador >= 0) {
			tipo = tipo.substring(separador + 1);
		}

		return switch (tipo) {
		case "boolean" -> "boolean";
		case "decimal", "double", "float" -> "number";
		case "integer", "int", "long", "short", "byte", "unsignedint", "unsignedlong", "unsignedshort",
				"unsignedbyte", "nonnegativeinteger", "positiveinteger", "negativeinteger", "nonpositiveinteger" ->
				"integer";
		default -> "string";
		};
	}
	private boolean tieneEstructuraObjeto(XsdElementSource source, XsdDefinitionSource definition) {
		return tieneHijos(source.ruta(), definition)
				|| definition.atributos().stream().anyMatch(a -> !esSeccionFactuCore(a.rutaElemento(), definition)
						&& !esAtributoTecnico(a, definition) && source.ruta().equals(a.rutaElemento()));
	}

	private boolean tieneHijos(String ruta, XsdDefinitionSource definition) {
		String prefijo = ruta + ".";
		return elementosEntrada(definition).stream().anyMatch(e -> e.ruta().startsWith(prefijo));
	}

	private List<XsdElementSource> elementosEntrada(XsdDefinitionSource definition) {
		return definition.elementos().stream().filter(source -> !esSeccionFactuCore(source.ruta(), definition))
				.filter(source -> !esElementoTecnico(source, definition)).toList();
	}

	private boolean esSeccionFactuCore(String ruta, XsdDefinitionSource definition) {
		String raiz = definition.elementoRaiz();
		return "factura".equals(raiz)
				&& (ruta.equals(raiz + ".infoTributaria") || ruta.startsWith(raiz + ".infoTributaria."));
	}

	private boolean esElementoTecnico(XsdElementSource source, XsdDefinitionSource definition) {
		return "factura".equals(definition.elementoRaiz())
				&& source.ruta().equals(definition.elementoRaiz() + ".Signature");
	}

	private boolean esAtributoTecnico(XsdAttributeSource attribute, XsdDefinitionSource definition) {
		if (!"factura".equals(definition.elementoRaiz())
				|| !definition.elementoRaiz().equals(attribute.rutaElemento())) {
			return false;
		}
		return "id".equals(attribute.nombre()) || "version".equals(attribute.nombre());
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
