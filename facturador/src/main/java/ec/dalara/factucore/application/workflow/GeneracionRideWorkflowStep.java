package ec.dalara.factucore.application.workflow;

import ec.dalara.factucore.application.workflow.WorkflowEtapaExecutor;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;

import ec.dalara.factucore.application.ApplicationException;
import ec.dalara.factucore.application.MessageResolver;


import ec.dalara.factucore.application.port.out.ComprobanteEvidenciaPort;
import ec.dalara.factucore.application.port.out.RidePort;
import ec.dalara.factucore.domain.shared.MessageCodes;
import ec.dalara.factucore.domain.workflow.ContextoWorkflow;
import ec.dalara.factucore.domain.workflow.EstadoProceso;
import ec.dalara.factucore.domain.workflow.EtapaWorkflow;
import ec.dalara.factucore.domain.workflow.ResultadoEtapa;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GeneracionRideWorkflowStep {

	private static final Logger LOGGER = LoggerFactory.getLogger(GeneracionRideWorkflowStep.class);

	private final WorkflowEtapaExecutor workflowEtapaExecutor;
	private final MessageResolver messageResolver;
	private final RidePort ridePort;
	private final ComprobanteEvidenciaPort evidenciaPort;

	public EtapaWorkflow etapa() {
		return EtapaWorkflow.GENERACION_RIDE;
	}

	/** Ejecuta la lógica de negocio y expone a Camel solo la salida de la etapa. */
	public String ejecutar(ContextoWorkflow contexto) {
		return workflowEtapaExecutor.ejecutar(contexto, etapa(), () -> ejecutarResultado(contexto));
	}

	private ResultadoEtapa ejecutarResultado(ContextoWorkflow contexto) {
		if (contexto == null || contexto.getComprobante() == null) {
			throw new ApplicationException(MessageCodes.RIDE_COMPROBANTE_REQUERIDO);
		}

		var comprobante = contexto.getComprobante();
		try {
			var definicion = contexto.getDefinicionDocumento();
			if (definicion == null || definicion.getDocumento() == null
					|| definicion.getDocumento().getNombre() == null) {
				throw new ApplicationException(MessageCodes.RIDE_DATOS_INVALIDOS);
			}

			byte[] pdf = ridePort.generar(comprobante, definicion.getDocumento().getNombre());
			contexto.setRide(pdf);

			String ruta = evidenciaPort.guardarRide(comprobante.getId(), pdf, contexto.getSolicitud().getUsuario());
			comprobante.setRutaRide(ruta);
			comprobante.setEstadoProceso(EstadoProceso.RIDE_GENERADO.name());
			comprobante.setFechaProximoReproceso(null);

			return ResultadoEtapa.exitosa(etapa(), EstadoProceso.RIDE_GENERADO.name(),
					Map.of("archivoPdfGenerado", true, "rutaRide", ruta));
		} catch (ApplicationException exception) {
			throw exception;
		} catch (RuntimeException exception) {
			comprobante.setEstadoProceso(EstadoProceso.ERROR.name());
			comprobante.setFechaProximoReproceso(null);
			String mensaje = messageResolver.resolver(MessageCodes.RIDE_GENERACION_ERROR);
			String idTransaccion = contexto.getSolicitud() == null ? null : contexto.getSolicitud().getIdTransaccion();
			LOGGER.error(messageResolver.resolver(MessageCodes.LOG_WORKFLOW_ETAPA_ERROR, idTransaccion, etapa(),
					MessageCodes.RIDE_GENERACION_ERROR, mensaje), exception);
			return ResultadoEtapa.fallida(etapa(), EstadoProceso.ERROR.name(), MessageCodes.RIDE_GENERACION_ERROR, mensaje);
		}
	}
}
