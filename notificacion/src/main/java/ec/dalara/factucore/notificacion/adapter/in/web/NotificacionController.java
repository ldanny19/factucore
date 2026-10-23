package ec.dalara.factucore.notificacion.adapter.in.web;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import ec.dalara.factucore.notificacion.application.service.NotificacionPersistenciaService;
import ec.dalara.factucore.notificacion.infrastructure.persistence.NotificacionEnvio;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/notificaciones/comprobantes")
@RequiredArgsConstructor
public class NotificacionController {

	private final NotificacionPersistenciaService persistenciaService;

	@GetMapping("/{idTransaccion}/envios")
	public ResponseEntity<List<NotificacionEnvioResponse>> consultarEnvios(
			@PathVariable String idTransaccion) {
		return ResponseEntity.ok(persistenciaService.historial(idTransaccion).stream()
				.map(NotificacionEnvioResponse::from).toList());
	}

	@PostMapping("/{idTransaccion}/reenvios")
	public ResponseEntity<NotificacionEnvioResponse> solicitarReenvio(@PathVariable String idTransaccion,
			@RequestHeader("Idempotency-Key") String idSolicitudReenvio, @AuthenticationPrincipal Jwt jwt) {
		try {
			UUID.fromString(idSolicitudReenvio);
		} catch (IllegalArgumentException exception) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
		}
		String usuarioSolicitud = jwt.getClaimAsString("preferred_username");
		if (usuarioSolicitud == null || usuarioSolicitud.isBlank()) {
			usuarioSolicitud = jwt.getSubject();
		}
		NotificacionEnvio envio = persistenciaService.solicitarReenvio(idTransaccion, idSolicitudReenvio,
				usuarioSolicitud);
		return ResponseEntity.accepted().body(NotificacionEnvioResponse.from(envio));
	}

	public record NotificacionEnvioResponse(Long id, Long idEnvioOrigen, String destinatario, String estado,
			int numeroIntentos, String usuarioSolicitud, java.time.Instant fechaRecepcion,
			java.time.Instant fechaEnvio) {
		static NotificacionEnvioResponse from(NotificacionEnvio envio) {
			return new NotificacionEnvioResponse(envio.getId(), envio.getIdEnvioOrigen(), envio.getDestinatario(),
					envio.getEstadoEnvio().name(), envio.getNumeroIntentos(), envio.getUsuarioSolicitud(),
					envio.getFechaRecepcion(), envio.getFechaEnvio());
		}
	}
}
