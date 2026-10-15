package ec.dalara.factucore.application.service;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import ec.dalara.factucore.domain.shared.EstadoRegistro;
import ec.dalara.factucore.domain.workflow.EstadoProceso;
import ec.dalara.factucore.infrastructure.persistence.repository.ComprobanteRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ComprobanteAutorizacionReprocessScheduler {

	private static final Logger LOGGER = LoggerFactory.getLogger(ComprobanteAutorizacionReprocessScheduler.class);
	private static final String CODIGO_PENDIENTE_SRI = "70";

	private final ComprobanteRepository comprobanteRepository;
	private final ComprobanteReprocessService comprobanteReprocessService;

	@Scheduled(fixedDelayString = "${factucore.sri.autorizacion.espera-reintento-ms:5000}")
	public void reprocesarPendientesCodigo70() {
		var pendientes = comprobanteRepository
				.findTop100ByEstadoProcesoAndCodigoErrorAndFechaProximoReprocesoLessThanEqualAndEstadoRegistroOrderByFechaProximoReprocesoAsc(
						EstadoProceso.AUTORIZACION_PENDIENTE.name(), CODIGO_PENDIENTE_SRI, LocalDateTime.now(),
						EstadoRegistro.ACTIVO);

		for (var comprobante : pendientes) {
			try {
				LOGGER.debug("Reconsultando autorización SRI para comprobante {} con código 70.", comprobante.getId());
				comprobanteReprocessService.reprocesar(comprobante.getId());
			} catch (RuntimeException exception) {
				LOGGER.warn("No fue posible reconsultar la autorización SRI del comprobante {} con código 70.",
						comprobante.getId(), exception);
			}
		}
	}
}
