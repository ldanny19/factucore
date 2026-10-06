package ec.dalara.factucore.application.contract.request;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class ComprobanteGeneracionRequestValidationTest {

	private static ValidatorFactory validatorFactory;
	private static Validator validator;

	@BeforeAll
	static void setUpValidator() {
		validatorFactory = Validation.buildDefaultValidatorFactory();
		validator = validatorFactory.getValidator();
	}

	@AfterAll
	static void closeValidator() {
		validatorFactory.close();
	}

	@Test
	void requestValido_noDebeTenerViolaciones() {
		var request = ComprobanteGeneracionRequest.builder().idTransaccion("TX-001")
				.fechaInicio(OffsetDateTime.now()).usuario("usuario").canal("API").idEmpresa(1L)
				.idEstablecimiento(1L).idPuntoEmision(1L).idTipoDocumento(1L).build();

		assertTrue(validator.validate(request).isEmpty());
	}

	@Test
	void requestConDatosJson_aceptaEstructuraJerarquica() throws Exception {
		var datos = new ObjectMapper().readTree("""
				{
				  "factura": {
				    "infoFactura": {
				      "razonSocialComprador": "CLIENTE DEMO"
				    },
				    "detalles": {
				      "detalle": [
				        {
				          "codigoPrincipal": "PROD001",
				          "cantidad": 2
				        }
				      ]
				    }
				  }
				}
				""");

		var request = ComprobanteGeneracionRequest.builder().idTransaccion("TX-001")
				.fechaInicio(OffsetDateTime.now()).usuario("usuario").canal("API").idEmpresa(1L)
				.idEstablecimiento(1L).idPuntoEmision(1L).idTipoDocumento(1L).datos(datos).build();

		assertTrue(validator.validate(request).isEmpty());
		assertNotNull(request.getDatos().get("factura"));
		assertTrue(request.getDatos().get("factura").get("detalles").get("detalle").isArray());
	}

	@Test
	void requestSinCamposObligatorios_debeDetectarViolaciones() {
		var request = ComprobanteGeneracionRequest.builder().build();

		assertTrue(validator.validate(request).size() >= 8);
	}

	@Test
	void idTransaccionExcedeLongitud_debeDetectarViolacion() {
		var request = ComprobanteGeneracionRequest.builder().idTransaccion("A".repeat(101))
				.fechaInicio(OffsetDateTime.now()).usuario("usuario").canal("API").idEmpresa(1L)
				.idEstablecimiento(1L).idPuntoEmision(1L).idTipoDocumento(1L).build();

		assertTrue(validator.validate(request).stream()
				.anyMatch(v -> "idTransaccion".equals(v.getPropertyPath().toString())));
	}
}
