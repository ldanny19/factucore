package ec.dalara.factucore.application.workflow;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import ec.dalara.factucore.application.ApplicationException;

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
public class GeneracionRideWorkflowStep   {

\tprivate static final Logger log = LoggerFactory.getLogger(GeneracionRideWorkflowStep.class);

	private final RidePort ridePort;
	private final ComprobanteEvidenciaPort evidenciaPort;

	public EtapaWorkflow etapa() {
		return EtapaWorkflow.GENERACION_RIDE;
	}

	/** Ejecuta la lógica de negocio y expone a Camel solo la salida de la etapa. */
	public String ejecutar(ContextoWorkflow contexto) {
		String idTransaccion = contexto != null && contexto.getSolicitud() != null
				? contexto.getSolicitud().getIdTransaccion()
				: null;
		log.debug("ID_TRANSACCION={} - Inicia Etapa {}", idTransaccion, etapa());
		try {
			ResultadoEtapa resultado = ejecutarResultado(contexto);
			String salida = contexto.registrarYObtenerSalida(resultado);
			log.debug("ID_TRANSACCION={} - Fin Etapa {} - Resultado={}", idTransaccion, etapa(), salida);
			return salida;
		} catch (RuntimeException exception) {
			log.debug("ID_TRANSACCION={} - Fin Etapa {} - Resultado=ERROR", idTransaccion, etapa());
			throw exception;
		}
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
				throw new ApplicationException(MessageCodes.RIDE_GENERACION_ERROR, "Definición de comprobante requerida");
			}

			byte[] pdf = ridePort.generar(comprobante, definicion.getDocumento().getNombre());
			contexto.setRide(pdf);

			String ruta = evidenciaPort.guardarRide(comprobante.getId(), pdf, contexto.getSolicitud().getUsuario());
			comprobante.setRutaRide(ruta);
			comprobante.setEstadoProceso(EstadoProceso.RIDE_GENERADO.name());
			comprobante.setFechaProximoReproceso(null);

			return ResultadoEtapa.exitosa(etapa(), EstadoProceso.RIDE_GENERADO.name(),
					Map.of("archivoPdfGenerado", true, "rutaRide", ruta));
		} catch (RuntimeException exception) {
			comprobante.setEstadoProceso(EstadoProceso.ERROR.name());
			comprobante.setFechaProximoReproceso(null);
			return ResultadoEtapa.fallida(etapa(), EstadoProceso.ERROR.name(), MessageCodes.RIDE_GENERACION_ERROR,
					exception.getMessage());
		}
	}
}
