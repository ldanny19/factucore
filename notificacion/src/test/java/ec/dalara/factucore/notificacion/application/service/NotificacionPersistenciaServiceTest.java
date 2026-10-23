package ec.dalara.factucore.notificacion.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.ObjectMapper;

import ec.dalara.factucore.messaging.api.EventoMensaje;
import ec.dalara.factucore.notificacion.infrastructure.persistence.NotificacionEnvio;
import ec.dalara.factucore.notificacion.infrastructure.persistence.NotificacionEnvio.EstadoEnvio;
import ec.dalara.factucore.notificacion.infrastructure.persistence.NotificacionEnvioRepository;

@ExtendWith(MockitoExtension.class)
class NotificacionPersistenciaServiceTest {

	@Mock
	private NotificacionEnvioRepository repository;

	private ObjectMapper objectMapper;
	private NotificacionPersistenciaService service;

	@BeforeEach
	void setUp() {
		objectMapper = new ObjectMapper().findAndRegisterModules();
		service = new NotificacionPersistenciaService(repository, objectMapper);
	}

	@Test
	void solicitarReenvioCreaRegistroNuevoRelacionadoConElAnterior() throws Exception {
		NotificacionEnvio original = envioOriginal(EstadoEnvio.ENVIADA);
		when(repository.findByIdSolicitudReenvio("request-1")).thenReturn(Optional.empty());
		when(repository.findTopByIdTransaccionOrderByFechaRecepcionDesc("tx-1"))
				.thenReturn(Optional.of(original));
		when(repository.saveAndFlush(any(NotificacionEnvio.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

		NotificacionEnvio reenvio = service.solicitarReenvio("tx-1", "request-1", "admin");
		EventoMensaje eventoOriginal = objectMapper.readValue(original.getEventoJson(), EventoMensaje.class);
		EventoMensaje eventoReenvio = objectMapper.readValue(reenvio.getEventoJson(), EventoMensaje.class);

		assertEquals(EstadoEnvio.PENDIENTE, reenvio.getEstadoEnvio());
		assertEquals(original.getId(), reenvio.getIdEnvioOrigen());
		assertEquals("request-1", reenvio.getIdSolicitudReenvio());
		assertEquals("admin", reenvio.getUsuarioSolicitud());
		assertEquals("destino@example.com", reenvio.getDestinatario());
		assertNotEquals(eventoOriginal.id(), eventoReenvio.id());
		assertEquals(eventoOriginal.correlationId(), eventoReenvio.correlationId());
		assertEquals(eventoOriginal.payload(), eventoReenvio.payload());
	}

	@Test
	void solicitarReenvioDevuelveLaMismaSolicitudIdempotente() {
		NotificacionEnvio existente = envioOriginal(EstadoEnvio.PENDIENTE);
		existente.setIdSolicitudReenvio("request-1");
		when(repository.findByIdSolicitudReenvio("request-1")).thenReturn(Optional.of(existente));

		assertSame(existente, service.solicitarReenvio("tx-1", "request-1", "admin"));
		verify(repository, never()).findTopByIdTransaccionOrderByFechaRecepcionDesc("tx-1");
	}

	@Test
	void solicitarReenvioRechazaUnEnvioTodaviaPendiente() {
		when(repository.findByIdSolicitudReenvio("request-1")).thenReturn(Optional.empty());
		when(repository.findTopByIdTransaccionOrderByFechaRecepcionDesc("tx-1"))
				.thenReturn(Optional.of(envioOriginal(EstadoEnvio.PENDIENTE)));

		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> service.solicitarReenvio("tx-1", "request-1", "admin"));

		assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
		verify(repository, never()).saveAndFlush(any(NotificacionEnvio.class));
	}

	private NotificacionEnvio envioOriginal(EstadoEnvio estado) {
		EventoMensaje evento = EventoMensaje.crear("COMPROBANTE_AUTORIZADO", "1", "tx-1",
				objectMapper.createObjectNode().put("idTransaccion", "tx-1"));
		NotificacionEnvio envio = new NotificacionEnvio();
		envio.setId(10L);
		envio.setIdEvento(evento.id());
		envio.setIdTransaccion("tx-1");
		try {
			envio.setEventoJson(objectMapper.writeValueAsString(evento));
		} catch (Exception exception) {
			throw new IllegalStateException(exception);
		}
		envio.setDestinatario("destino@example.com");
		envio.setAsunto("Comprobante autorizado");
		envio.setContenidoHtml("<p>Comprobante</p>");
		envio.setRutaXmlAutorizado("/documentos/xml/autorizado.xml");
		envio.setRutaRide("/documentos/pdf/ride.pdf");
		envio.setEstadoEnvio(estado);
		envio.setNumeroIntentos(1);
		envio.setFechaRecepcion(Instant.parse("2026-10-23T12:00:00Z"));
		return envio;
	}
}
