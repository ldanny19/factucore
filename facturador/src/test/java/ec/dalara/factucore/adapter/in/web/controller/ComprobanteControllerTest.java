package ec.dalara.factucore.adapter.in.web.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import ec.dalara.factucore.application.contract.request.ComprobanteGeneracionRequest;
import ec.dalara.factucore.application.contract.response.ComprobanteGeneracionResponse;
import ec.dalara.factucore.application.port.in.ComprobanteWorkflowPort;

@ExtendWith(MockitoExtension.class)
class ComprobanteControllerTest {

	@Mock
	private ComprobanteWorkflowPort workflowPort;

	private ComprobanteController controller;

	@BeforeEach
	void setUp() {
		controller = new ComprobanteController(workflowPort);
	}

	@Test
	void generar_debeDelegarAlWorkflowYRetornarRespuesta() {
		var request = ComprobanteGeneracionRequest.builder().idTransaccion("TX-001").fechaInicio(OffsetDateTime.now())
				.usuario("usuario").canal("API").idEmpresa(1L).codigoEstablecimiento("001").puntoEmision("001")
				.tipoDocumento("01").build();

		var expected = ComprobanteGeneracionResponse.builder().idTransaccion("TX-001").exitoso(true)
				.estado("AUTORIZADO").claveAcceso("1234567890").build();

		when(workflowPort.procesar(request)).thenReturn(expected);

		ResponseEntity<ComprobanteGeneracionResponse> response = controller.generar(request);

		assertEquals(200, response.getStatusCode().value());
		assertEquals(expected, response.getBody());
		verify(workflowPort).procesar(request);
	}

	@Test
	void reprocesar_debeDelegarAlWorkflowYRetornarEstado() {
		Long comprobanteId = 10L;

		ResponseEntity<ComprobanteGeneracionResponse> response = controller.reprocesar(comprobanteId);

		assertEquals(200, response.getStatusCode().value());
		assertTrue(response.getBody().getExitoso());
		assertEquals("REPROCESADO", response.getBody().getEstado());
		verify(workflowPort).reprocesar(comprobanteId);
	}
}
