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
	void debeExcluirInfoTributariaDePlantillaFactura() throws Exception {
		var definition = new XsdDefinitionSource(
				"urn:test",
				"factura",
				List.of(
						new XsdElementSource("factura", "factura", "FacturaType", 1, 1, 1, null, null, null, null, null, null, null),
						new XsdElementSource("factura.infoTributaria", "infoTributaria", "InfoType", 1, 1, 1, null, null, null, null, null, null, null),
						new XsdElementSource("factura.infoTributaria.ruc", "ruc", "string", 1, 1, 1, null, null, null, null, null, null, null),
						new XsdElementSource("factura.infoFactura", "infoFactura", "InfoFacturaType", 2, 1, 1, null, null, null, null, null, null, null),
						new XsdElementSource("factura.infoFactura.fechaEmision", "fechaEmision", "string", 1, 1, 1, null, null, null, null, null, null, null)),
				List.<XsdAttributeSource>of(),
				List.<XsdEnumerationSource>of());

		var adapter = new XsdDefinitionPersistenceAdapter(null, null, null, null, null, new ObjectMapper());
		Method method = XsdDefinitionPersistenceAdapter.class.getDeclaredMethod("generarPlantillaJson", XsdDefinitionSource.class);
		method.setAccessible(true);

		String plantilla = (String) method.invoke(adapter, definition);
		JsonNode json = new ObjectMapper().readTree(plantilla);

		assertTrue(json.path("factura").has("infoFactura"));
		assertTrue(json.path("factura").path("infoFactura").has("fechaEmision"));
		assertFalse(json.path("factura").has("infoTributaria"));
	}
}
