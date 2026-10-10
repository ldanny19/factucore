package ec.dalara.factucore.application.service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.springframework.stereotype.Service;

import ec.dalara.factucore.application.ApplicationException;
import ec.dalara.factucore.application.MessageResolver;
import ec.dalara.factucore.application.contract.request.ComprobanteGeneracionRequest;
import ec.dalara.factucore.application.contract.response.ComprobanteGeneracionResponse;
import ec.dalara.factucore.application.contract.response.MensajeResponse;
import ec.dalara.factucore.application.contract.response.ResultadoResponse;
import ec.dalara.factucore.application.port.in.ComprobanteWorkflowPort;
import ec.dalara.factucore.application.port.out.WorkflowExecutionPort;
import ec.dalara.factucore.domain.shared.MessageCodes;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ComprobanteWorkflowService implements ComprobanteWorkflowPort {

	private final WorkflowExecutionPort workflowExecutionPort;
	private final ComprobanteSolicitudEnProcesoService solicitudEnProcesoService;
	private final MessageResolver messageResolver;

	@Override
	public ComprobanteGeneracionResponse procesar(ComprobanteGeneracionRequest request) {
		if (request == null || request.getIdEmpresa() == null || request.getIdDocumentoOrigen() == null
				|| request.getIdTransaccion() == null || request.getIdTransaccion().isBlank()) {
			return workflowExecutionPort.ejecutar(request);
		}

		boolean reclamada = solicitudEnProcesoService.reclamar(request.getIdEmpresa(),
				request.getIdDocumentoOrigen(), request.getIdTransaccion());
		if (!reclamada) {
			return respuestaEnProceso(request);
		}

		try {
			return workflowExecutionPort.ejecutar(request);
		} finally {
			solicitudEnProcesoService.liberar(request.getIdEmpresa(),
					request.getIdDocumentoOrigen(), request.getIdTransaccion());
		}
	}

	private ComprobanteGeneracionResponse respuestaEnProceso(ComprobanteGeneracionRequest request) {
		String codigo = MessageCodes.COMPROBANTE_EN_PROCESO;
		String mensaje = messageResolver.resolver(codigo, request.getIdDocumentoOrigen());
		MensajeResponse error = MensajeResponse.builder().codigo(codigo).mensaje(mensaje).build();
		OffsetDateTime inicio = request.getFechaInicio() == null ? null
				: request.getFechaInicio().atOffset(ZoneOffset.UTC);
		return ComprobanteGeneracionResponse.builder()
				.idTransaccion(request.getIdTransaccion())
				.fechaInicio(inicio)
				.fechaFin(OffsetDateTime.now(ZoneOffset.UTC))
				.exitoso(false)
				.codigo(codigo)
				.mensaje(mensaje)
				.estadoSri("EN_PROCESO")
				.resultado(ResultadoResponse.builder().errores(List.of(error)).build())
				.build();
	}

	@Override
	public void reprocesar(Long comprobanteId) {
		if (comprobanteId == null) {
			throw new ApplicationException(MessageCodes.WORKFLOW_COMPROBANTE_REQUERIDO);
		}
		workflowExecutionPort.reprocesar(comprobanteId);
	}
}
