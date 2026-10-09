package ec.dalara.factucore.infrastructure.documentoxsd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

class XsdParserAdapterTest {

	private final XsdParserAdapter parser = new XsdParserAdapter();

	@Test
	void debeConstruirArbolYCardinalidadDesdeXsd() {
		String xsd = """
				<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema"
				           targetNamespace="urn:test"
				           xmlns:t="urn:test">
				  <xs:element name="factura" type="t:FacturaType"/>
				  <xs:complexType name="FacturaType">
				    <xs:sequence>
				      <xs:element name="infoTributaria" type="t:InfoType"/>
				      <xs:element name="detalles" minOccurs="0">
				        <xs:complexType>
				          <xs:sequence>
				            <xs:element name="detalle" minOccurs="1" maxOccurs="unbounded">
				              <xs:simpleType>
				                <xs:restriction base="xs:string">
				                  <xs:minLength value="1"/>
				                  <xs:maxLength value="300"/>
				                </xs:restriction>
				              </xs:simpleType>
				            </xs:element>
				          </xs:sequence>
				        </xs:complexType>
				      </xs:element>
				    </xs:sequence>
				  </xs:complexType>
				  <xs:complexType name="InfoType">
				    <xs:sequence>
				      <xs:element name="ruc" type="xs:string" minOccurs="1"/>
				    </xs:sequence>
				  </xs:complexType>
				</xs:schema>
				""";

		var definition = parser.parse(new ByteArrayInputStream(xsd.getBytes(StandardCharsets.UTF_8)), "test.xsd");

		assertEquals("urn:test", definition.namespaceXml());
		assertEquals("factura", definition.elementoRaiz());
		assertEquals(5, definition.elementos().size());

		var factura = definition.elementos().get(0);
		assertEquals("factura", factura.nombre());
		assertEquals(1, factura.minOcurrencias());
		assertEquals(1, factura.maxOcurrencias());

		var detalle = definition.elementos().get(4);
		assertEquals("factura.detalles.detalle", detalle.ruta());
		assertTrue(detalle.esRepetible());
		assertEquals(1, detalle.minOcurrencias());
		assertNull(detalle.maxOcurrencias());
		assertEquals(1, detalle.longitudMinima());
		assertEquals(300, detalle.longitudMaxima());
	}

	@Test
	void noDebePersistirAtributosNiEnumeracionesDeLaRaizFactura() {
		String xsd = """
				<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema">
				  <xs:element name="factura">
				    <xs:complexType>
				      <xs:sequence>
				        <xs:element name="ambiente" type="xs:string"/>
				      </xs:sequence>
				      <xs:attribute name="id">
				        <xs:simpleType>
				          <xs:restriction base="xs:string">
				            <xs:enumeration value="comprobante"/>
				          </xs:restriction>
				        </xs:simpleType>
				      </xs:attribute>
				      <xs:attribute name="version" type="xs:string"/>
				    </xs:complexType>
				  </xs:element>
				</xs:schema>
				""";

		var definition = parser.parse(new ByteArrayInputStream(xsd.getBytes(StandardCharsets.UTF_8)), "test.xsd");

		assertTrue(definition.atributos().isEmpty());
		assertTrue(definition.enumeraciones().isEmpty());
	}

	@Test
	void debeConservarMinOccursDeUnaReferenciaLocal() {
		String xsd = """
				<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema"
				           targetNamespace="urn:test"
				           xmlns:t="urn:test">
				  <xs:element name="opcional" type="xs:string"/>
				  <xs:element name="raiz" type="t:RaizType"/>
				  <xs:complexType name="RaizType">
				    <xs:sequence>
				      <xs:element ref="t:opcional" minOccurs="0"/>
				    </xs:sequence>
				  </xs:complexType>
				</xs:schema>
				""";

		var definition = parser.parse(new ByteArrayInputStream(xsd.getBytes(StandardCharsets.UTF_8)), "test.xsd");
		var opcional = definition.elementos().stream()
				.filter(elemento -> "raiz.opcional".equals(elemento.ruta()))
				.findFirst().orElseThrow();

		assertEquals(0, opcional.minOcurrencias());
		assertTrue(!opcional.esObligatorio());
	}

}
