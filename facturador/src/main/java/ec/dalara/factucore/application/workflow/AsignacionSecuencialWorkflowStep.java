package ec.dalara.factucore.application.workflow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ec.dalara.factucore.application.service.ComprobanteService;
import ec.dalara.factucore.application.service.DocumentoXsdService;
import ec.dalara.factucore.application.service.EmisionService;
import ec.dalara.factucore.application.service.SecuencialService;
import ec.dalara.factucore.domain.shared.MessageCodes;
import ec.dalara.factucore.domain.workflow.ContextoWorkflow;
import ec.dalara.factucore.domain.workflow.EtapaWorkflow;
import ec.dalara.factucore.domain.workflow.ResultadoEtapa;
import ec.dalara.factucore.application.ApplicationException;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AsignacionSecuencialWorkflowStep  {

	private static final Logger log = LoggerFactory.getLogger(AsignacionSecuencialWorkflowStep.class);

	private final EmisionService emisionService;
	private final DocumentoXsdService documentoXsdService;
	private final SecuencialService secuencialService;
	private final ComprobanteService comprobanteService;

	public EtapaWorkflow etapa() {
		return EtapaWorkflow.ASIGNACION_SECUENCIAL;
	}

	/**
	 * Ejecuta la etapa y devuelve exclusivamente su resultado para Camel.
	 * El Bean no conoce ni decide el siguiente nodo del workflow.
	 */
	@Transactional
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
		var solicitud = contexto.getSolicitud();

		var emision = emisionService.resolver(solicitud.getIdEmpresa(), solicitud.getIdEstablecimiento(),
				solicitud.getIdPuntoEmision());

		var documento = documentoXsdService.obtenerPorId(solicitud.getIdTipoDocumento())
				.orElseThrow(() -> new ApplicationException(MessageCodes.SECUENCIAL_NO_ENCONTRADO));

		log.info(
				"Buscando secuencial: empresaId={}, establecimientoId={}, puntoEmisionRequestId={}, puntoEmisionId={}, tipoDocumentoRequestId={}, codigoDocumento={}, estadoRegistro={}",
				solicitud.getIdEmpresa(), solicitud.getIdEstablecimiento(), solicitud.getIdPuntoEmision(),
				emision.puntoEmision().getId(), solicitud.getIdTipoDocumento(), documento.getCodigo(), "A");

		var secuencial = secuencialService.bloquearSecuencial(emision.puntoEmision().getId(), documento.getCodigo());

		var existente = comprobanteService.obtenerPorEmpresaEIdTransaccion(solicitud.getIdEmpresa(),
				solicitud.getIdTransaccion());

		if (existente.isPresent()) {
			contexto.asignarComprobante(existente.get());
			contexto.setClaveAcceso(existente.get().getClaveAcceso());
			contexto.setSecuencial(existente.get().getSecuencial());
			contexto.marcarIdempotente();

			return ResultadoEtapa.exitosa(EtapaWorkflow.ASIGNACION_SECUENCIAL, "IDEMPOTENTE");
		}

		Long siguiente = secuencialService.consumirSiguienteSecuencial(secuencial);
		contexto.setSecuencial(String.format("%09d", siguiente));

		return ResultadoEtapa.exitosa(EtapaWorkflow.ASIGNACION_SECUENCIAL, "COMPLETADA");
	}
}