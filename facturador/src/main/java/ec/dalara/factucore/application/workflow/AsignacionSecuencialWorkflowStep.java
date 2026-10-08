package ec.dalara.factucore.application.workflow;

import ec.dalara.factucore.application.workflow.WorkflowEtapaExecutor;

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

	private final WorkflowEtapaExecutor workflowEtapaExecutor;
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
		return workflowEtapaExecutor.ejecutar(contexto, etapa(), () -> ejecutarResultado(contexto));
	}

	private ResultadoEtapa ejecutarResultado(ContextoWorkflow contexto) {
		var solicitud = contexto.getSolicitud();

		var emision = emisionService.resolver(solicitud.getIdEmpresa(), solicitud.getIdEstablecimiento(),
				solicitud.getIdPuntoEmision());

		var documento = documentoXsdService.obtenerPorId(solicitud.getIdTipoDocumento())
				.orElseThrow(() -> new ApplicationException(MessageCodes.SECUENCIAL_NO_ENCONTRADO));


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