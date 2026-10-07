package ec.dalara.factucore.infrastructure.persistence.adapter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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
