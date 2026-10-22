package ec.dalara.factucore.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class FactuCoreMapeoFuenteCatalogoTest {

	@Test
	void listaSoloEntidadesYCamposAprobadosSinValoresDeDatos() {
		var fuentes = FactuCoreMapeoFuenteCatalogo.listar();

		assertEquals(4, fuentes.size());
		assertEquals("Empresa", fuentes.get(0).getNombre());
		assertTrue(fuentes.get(0).getCampos().stream()
				.anyMatch(campo -> "empresa.ruc".equals(campo.getOrigen())));
		assertEquals("Establecimiento", fuentes.get(1).getNombre());
		assertEquals("Punto de emisión", fuentes.get(2).getNombre());
		assertEquals("Ítem de catálogo", fuentes.get(3).getNombre());
		assertTrue(fuentes.get(3).isRequiereCatalogo());
		assertTrue(fuentes.get(3).getCampos().stream()
				.anyMatch(campo -> "catalogo_item.{codigoCatalogo}.codigo".equals(campo.getOrigen())));
		assertFalse(fuentes.stream().anyMatch(fuente -> "certificado_firma".equals(fuente.getCodigo())));
	}
}
