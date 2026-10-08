package ec.dalara.factucore.application.validation;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ec.dalara.factucore.application.MessageResolver;
import ec.dalara.factucore.domain.documentoxsd.DocumentDefinitionModel;
import ec.dalara.factucore.domain.documentoxsd.DocumentoXsdModel;
import ec.dalara.factucore.domain.documentoxsd.ElementoXsdModel;
import ec.dalara.factucore.domain.documentoxsd.MapeoXsdModel;
import ec.dalara.factucore.domain.documentoxsd.VersionDocumentoXsdModel;

class DocumentDefinitionDataValidatorTest {

	@Test
	void debeValidarFacturaV100SinAplanarOcurrenciasDeHijos() throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();

		JsonNode json = objectMapper.readTree(recurso("json/factura/factura_V1.0.0.json"));
		JsonNode mappings = objectMapper.readTree(recurso("json/administracion/mapeo_xsd_V1.0.0.json"));

		List<MapeoXsdModel> mapeos = construirMapeos(mappings.path("datos"));
		List<ElementoXsdModel> elementos = construirElementos(mapeos);

		DocumentDefinitionModel definition = new DocumentDefinitionModel(
				new DocumentoXsdModel("FACTURA", "Factura", null, "01", "factura"),
				new VersionDocumentoXsdModel(1L, 1L, "1.0.0", "factura_V1.0.0.xsd", null, "factura",
						null, null, LocalDateTime.now(), null),
				elementos, List.of(), List.of(), mapeos);

		MessageResolver messageResolver = mock(MessageResolver.class);
		when(messageResolver.resolver(anyString(), any(Object[].class))).thenReturn("error");

		DocumentDefinitionDataValidator validator = new DocumentDefinitionDataValidator(messageResolver);
		ComprobanteValidationResult resultado = new ComprobanteValidationResult(messageResolver);

		@SuppressWarnings("unchecked")
		Map<String, Object> datos = objectMapper.convertValue(json.path("datos"), Map.class);

		validator.validar(definition, datos, resultado);

		assertTrue(resultado.esValido(),
				() -> "La factura V1.0.0 no debe generar errores de ocurrencias: " + resultado.getErrores());
	}

	private List<MapeoXsdModel> construirMapeos(JsonNode nodos) {
		List<MapeoXsdModel> resultado = new ArrayList<>();

		for (JsonNode nodo : nodos) {
			Long elementoId = nodo.path("idElementoXsd").isNull() ? null : nodo.path("idElementoXsd").asLong();
			Long atributoId = nodo.path("idAtributoXsd").isNull() ? null : nodo.path("idAtributoXsd").asLong();

			resultado.add(new MapeoXsdModel(nodo.has("id") ? nodo.path("id").asLong() : null, 1L,
					nodo.path("tipoOrigen").asText(), nodo.path("origen").asText(), elementoId, atributoId,
				nodo.path("tipoMapeo").asText()));
		}

		return resultado;
	}

	private List<ElementoXsdModel> construirElementos(List<MapeoXsdModel> mapeos) {
		Map<Long, String> nombres = new HashMap<>();

		for (MapeoXsdModel mapeo : mapeos) {
			if (!mapeo.esElemento()) {
				continue;
			}

			String[] partes = mapeo.getOrigen().split("\\.");
			String nombre = partes[partes.length - 1];

			if (mapeo.getElementoXsdId() == 123L) {
				nombre = "campoAdicional";
			}

			nombres.put(mapeo.getElementoXsdId(), nombre);
		}

		nombres.put(1L, "factura");
		nombres.put(84L, "detAdicional");

		Map<String, Long> idsPorRuta = new HashMap<>();
		for (MapeoXsdModel mapeo : mapeos) {
			if (mapeo.esElemento()) {
				String ruta = mapeo.getOrigen();
				if (mapeo.getElementoXsdId() == 123L) {
					ruta = ruta.substring(0, ruta.lastIndexOf('.'));
				}
				idsPorRuta.put(ruta, mapeo.getElementoXsdId());
			}
		}

		List<ElementoXsdModel> resultado = new ArrayList<>();

		for (Map.Entry<Long, String> entry : nombres.entrySet()) {
			Long id = entry.getKey();
			String nombre = entry.getValue();
			String ruta = rutaElemento(id, mapeos);
			Long padreId = ruta == null || !ruta.contains(".") ? null : idsPorRuta.get(ruta.substring(0, ruta.lastIndexOf('.')));

			if (id == 1L) {
				padreId = null;
			}

			boolean repetible = switch (id.intValue()) {
			case 43, 52, 65, 73, 84, 86, 93, 105, 112, 123 -> true;
		default -> false;
			};

			Integer min = repetible ? 1 : 0;
			Integer max = switch (id.intValue()) {
			case 84 -> 3;
			case 123 -> 15;
			default -> repetible ? null : 1;
			};

			resultado.add(new ElementoXsdModel(id, 1L, padreId, nombre,
					repetible ? "LIST" : "STRING", 1, false, repetible, min, max,
				null, null, null, null, null, null, null, null, null));
		}

		resultado.sort((a, b) -> Long.compare(a.getId(), b.getId()));
		return resultado;
	}

	private String rutaElemento(Long id, List<MapeoXsdModel> mapeos) {
		for (MapeoXsdModel mapeo : mapeos) {
			if (!mapeo.esElemento() || !id.equals(mapeo.getElementoXsdId())) {
				continue;
			}

			if (id == 123L) {
				return mapeo.getOrigen().substring(0, mapeo.getOrigen().lastIndexOf('.'));
			}

			return mapeo.getOrigen();
		}

		if (id == 1L) {
			return "factura";
		}

		if (id == 84L) {
			return "factura.detalles.detalle.detallesAdicionales.detAdicional";
		}

		return null;
	}

	private InputStream recurso(String ruta) {
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream(ruta);

		if (inputStream == null) {
			throw new IllegalStateException("Recurso de prueba no encontrado: " + ruta);
		}

		return inputStream;
	}
}
