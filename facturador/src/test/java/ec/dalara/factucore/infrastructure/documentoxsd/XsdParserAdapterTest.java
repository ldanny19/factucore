package ec.dalara.factucore.infrastructure.documentoxsd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

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
	void debeConservarAtributosDeLaRaizFacturaEnElParseo() {
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

		assertEquals(2, definition.atributos().size());
		assertTrue(definition.atributos().stream()
				.anyMatch(a -> "id".equals(a.nombre()) && "factura".equals(a.rutaElemento())));
		assertTrue(definition.atributos().stream()
				.anyMatch(a -> "version".equals(a.nombre()) && "factura".equals(a.rutaElemento())));
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
		var opcional = definition.elementos().stream().filter(elemento -> "raiz.opcional".equals(elemento.ruta()))
				.findFirst().orElseThrow();

		assertEquals(0, opcional.minOcurrencias());
		assertTrue(!opcional.esObligatorio());
	}

	@Test
	void debeExcluirSignatureXmlDsigDeLosElementosParseados(@TempDir Path directorio) throws Exception {
		Path xsdFirma = directorio.resolve("xmldsig-core-schema.xsd");
		Files.writeString(xsdFirma, """
				<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema"
				           targetNamespace="http://www.w3.org/2000/09/xmldsig#"
				           xmlns:ds="http://www.w3.org/2000/09/xmldsig#"
				           elementFormDefault="qualified">
				  <xs:element name="Signature" type="ds:SignatureType"/>
				  <xs:complexType name="SignatureType">
				    <xs:sequence>
				      <xs:element name="SignedInfo" type="xs:string"/>
				    </xs:sequence>
				  </xs:complexType>
				</xs:schema>
				""");
		String xsdFactura = """
				<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema"
				           xmlns:ds="http://www.w3.org/2000/09/xmldsig#">
				  <xs:import namespace="http://www.w3.org/2000/09/xmldsig#"
				             schemaLocation="xmldsig-core-schema.xsd"/>
				  <xs:element name="factura">
				    <xs:complexType>
				      <xs:sequence>
				        <xs:element name="infoFactura" type="xs:string"/>
				        <xs:element ref="ds:Signature" minOccurs="0"/>
				      </xs:sequence>
				    </xs:complexType>
				  </xs:element>
				</xs:schema>
				""";
		Path archivoFactura = directorio.resolve("factura.xsd");
		Files.writeString(archivoFactura, xsdFactura);

		var definition = parser.parse(new ByteArrayInputStream(Files.readAllBytes(archivoFactura)),
				archivoFactura.toAbsolutePath().normalize().toUri().toString());

		assertTrue(definition.elementos().stream().anyMatch(elemento -> "factura.infoFactura".equals(elemento.ruta())));
		assertFalse(definition.elementos().stream().anyMatch(elemento -> elemento.ruta().contains("Signature")));
	}

}
