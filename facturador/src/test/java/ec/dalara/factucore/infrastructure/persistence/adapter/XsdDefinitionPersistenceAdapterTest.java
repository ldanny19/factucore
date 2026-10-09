package ec.dalara.factucore.infrastructure.persistence.adapter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;

import ec.dalara.factucore.domain.documentoxsd.importacion.XsdAttributeSource;
import ec.dalara.factucore.domain.documentoxsd.importacion.XsdDefinitionSource;
import ec.dalara.factucore.domain.documentoxsd.importacion.XsdElementSource;
import ec.dalara.factucore.domain.documentoxsd.importacion.XsdEnumerationSource;

class XsdDefinitionPersistenceAdapterTest {

	@Test
	void debeExcluirInfoTributariaYDatosTecnicosDePlantillaFactura() throws Exception {
		var definition = new XsdDefinitionSource("urn:test", "factura",
				List.of(new XsdElementSource("factura", "factura", "FacturaType", 1, 1, 1, null, null, null, null, null,
						null, null),
						new XsdElementSource("factura.infoTributaria", "infoTributaria", "InfoType", 1, 1, 1, null,
								null, null, null, null, null, null),
						new XsdElementSource("factura.infoTributaria.ruc", "ruc", "string", 1, 1, 1, null, null, null,
								null, null, null, null),
						new XsdElementSource("factura.infoFactura", "infoFactura", "InfoFacturaType", 2, 1, 1, null,
								null, null, null, null, null, null),
						new XsdElementSource("factura.infoFactura.fechaEmision", "fechaEmision", "string", 1, 1, 1,
								null, null, null, null, null, null, null),
						new XsdElementSource("factura.Signature", "Signature", "SignatureType", 3, 0, 1, null, null,
								null, null, null, null, null)),
				List.of(new XsdAttributeSource("factura", "id", "string", true, null, null),
						new XsdAttributeSource("factura", "version", "string", true, null, null)),
				List.<XsdEnumerationSource>of());

		var adapter = new XsdDefinitionPersistenceAdapter(null, null, null, null, null, new ObjectMapper());
		Method method = XsdDefinitionPersistenceAdapter.class.getDeclaredMethod("generarPlantillaJson",
				XsdDefinitionSource.class);
		method.setAccessible(true);

		String plantilla = (String) method.invoke(adapter, definition);
		JsonNode json = new ObjectMapper().readTree(plantilla);

		assertTrue(json.path("factura").has("infoFactura"));
		assertTrue(json.path("factura").path("infoFactura").has("fechaEmision"));
		assertFalse(json.path("factura").has("infoTributaria"));
		assertFalse(json.path("factura").has("Signature"));
		assertFalse(json.path("factura").has("id"));
		assertFalse(json.path("factura").has("version"));
	}


	@Test
	void debeGenerarObligatoriedadCardinalidadYExcluirTagsDelEsquema() throws Exception {
		var definition = new XsdDefinitionSource("urn:test", "factura",
				List.of(new XsdElementSource("factura", "factura", "FacturaType", 1, 1, 1, null, null, null, null,
						null, null, null),
						new XsdElementSource("factura.infoTributaria", "infoTributaria", "InfoType", 1, 1, 1, null,
							null, null, null, null, null, null),
						new XsdElementSource("factura.infoTributaria.ruc", "ruc", "string", 1, 1, 1, null, null,
							null, null, null, null, null),
						new XsdElementSource("factura.infoFactura", "infoFactura", "InfoType", 1, 1, 1, null, null,
							null, null, null, null, null),
						new XsdElementSource("factura.infoFactura.fechaEmision", "fechaEmision", "string", 1, 1, 1,
							null, null, null, null, null, null, null),
						new XsdElementSource("factura.infoFactura.observacion", "observacion", "string", 2, 0, 1,
							null, null, null, null, null, null, null),
						new XsdElementSource("factura.detalle", "detalle", "string", 2, 1, null, null, null, null, null,
							null, null, null),
						new XsdElementSource("factura.signature", "signature", "SignatureType", 3, 0, 1, null, null,
							null, null, null, null, null)),
				List.of(new XsdAttributeSource("factura.infoFactura", "codigo", "string", true, null, null),
						new XsdAttributeSource("factura.signature", "id", "string", true, null, null)),
				List.<XsdEnumerationSource>of());

		var adapter = new XsdDefinitionPersistenceAdapter(null, null, null, null, null, new ObjectMapper());
		Method method = XsdDefinitionPersistenceAdapter.class.getDeclaredMethod("generarEsquemaJson",
				XsdDefinitionSource.class);
		method.setAccessible(true);

		JsonNode schema = new ObjectMapper().readTree((String) method.invoke(adapter, definition));
		JsonNode factura = schema.path("properties").path("factura");
		JsonNode infoFactura = factura.path("properties").path("infoFactura");

		assertTrue(schema.path("required").toString().contains("factura"));
		assertTrue(factura.path("required").toString().contains("infoFactura"));
		assertTrue(factura.path("required").toString().contains("detalle"));
		assertTrue(infoFactura.path("required").toString().contains("fechaEmision"));
		assertFalse(infoFactura.path("required").toString().contains("observacion"));
		assertTrue(factura.path("properties").path("detalle").path("type").asText().equals("array"));
		assertTrue(factura.path("properties").path("detalle").path("minItems").asInt() == 1);
		assertTrue(infoFactura.path("required").toString().contains("codigo"));
		assertFalse(factura.path("properties").has("infoTributaria"));
		assertFalse(factura.path("properties").has("signature"));
	}


	@Test
	void esquemaGeneradoDebeAplicarRestriccionesConUnValidadorJsonSchemaReal() throws Exception {
		var definition = new XsdDefinitionSource("urn:test", "documento",
				List.of(new XsdElementSource("documento", "documento", "DocumentoType", 1, 1, 1, null, null,
						null, null, null, null, null),
						new XsdElementSource("documento.codigo", "codigo", "xs:string", 1, 1, 1, 2, 5, null,
								null, null, null, "^[A-Z]+$"),
						new XsdElementSource("documento.importe", "importe", "xs:decimal", 2, 0, 1, null, null,
								6, 2, null, null, null),
						new XsdElementSource("documento.estado", "estado", "xs:string", 3, 1, 1, null, null,
								null, null, null, null, null)),
				List.of(), List.of(new XsdEnumerationSource("documento.estado", "ACTIVO", "Activo", 1),
						new XsdEnumerationSource("documento.estado", "INACTIVO", "Inactivo", 2)));
		var adapter = new XsdDefinitionPersistenceAdapter(null, null, null, null, null, new ObjectMapper());
		Method method = XsdDefinitionPersistenceAdapter.class.getDeclaredMethod("generarEsquemaJson",
				XsdDefinitionSource.class);
		method.setAccessible(true);
		String schemaJson = (String) method.invoke(adapter, definition);
		JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
		var schema = factory.getSchema(new ObjectMapper().readTree(schemaJson));

		ObjectMapper mapper = new ObjectMapper();
		com.fasterxml.jackson.databind.node.ObjectNode documentoValido = mapper.createObjectNode();
		documentoValido.put("codigo", "ABCD");
		documentoValido.put("importe", new java.math.BigDecimal("12.34"));
		documentoValido.put("estado", "ACTIVO");
		com.fasterxml.jackson.databind.node.ObjectNode valido = mapper.createObjectNode();
		valido.set("documento", documentoValido);
		assertTrue(schema.validate(valido).isEmpty());

		com.fasterxml.jackson.databind.node.ObjectNode documentoInvalido = mapper.createObjectNode();
		documentoInvalido.put("codigo", "a");
		documentoInvalido.put("importe", new java.math.BigDecimal("12.345"));
		documentoInvalido.put("estado", "OTRO");
		com.fasterxml.jackson.databind.node.ObjectNode invalido = mapper.createObjectNode();
		invalido.set("documento", documentoInvalido);
		assertFalse(schema.validate(invalido).isEmpty());
	}

	@Test
	void debeExcluirDatosTecnicosDeEsquemaFactura() throws Exception {
		var definition = new XsdDefinitionSource("urn:test", "factura",
				List.of(new XsdElementSource("factura", "factura", "FacturaType", 1, 1, 1, null, null, null, null, null,
						null, null),
						new XsdElementSource("factura.infoFactura", "infoFactura", "InfoFacturaType", 1, 1, 1, null,
								null, null, null, null, null, null),
						new XsdElementSource("factura.infoFactura.fechaEmision", "fechaEmision", "string", 1, 1, 1,
								null, null, null, null, null, null, null),
						new XsdElementSource("factura.Signature", "Signature", "SignatureType", 2, 0, 1, null, null,
								null, null, null, null, null)),
				List.of(new XsdAttributeSource("factura", "id", "string", true, null, null),
						new XsdAttributeSource("factura", "version", "string", true, null, null)),
				List.<XsdEnumerationSource>of());

		var adapter = new XsdDefinitionPersistenceAdapter(null, null, null, null, null, new ObjectMapper());
		Method method = XsdDefinitionPersistenceAdapter.class.getDeclaredMethod("generarEsquemaJson",
				XsdDefinitionSource.class);
		method.setAccessible(true);

		String esquema = (String) method.invoke(adapter, definition);
		JsonNode json = new ObjectMapper().readTree(esquema);
		JsonNode factura = json.path("properties").path("factura");

		assertTrue(factura.path("properties").has("infoFactura"));
		assertFalse(factura.path("properties").has("Signature"));
		assertFalse(factura.path("properties").has("id"));
		assertFalse(factura.path("properties").has("version"));
	}
}
