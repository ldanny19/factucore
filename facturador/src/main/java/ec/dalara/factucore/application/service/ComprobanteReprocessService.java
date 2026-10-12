package ec.dalara.factucore.application.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ec.dalara.factucore.application.ApplicationException;
import ec.dalara.factucore.application.contract.request.ComprobanteGeneracionRequest;
import ec.dalara.factucore.application.contract.response.ComprobanteGeneracionResponse;
import ec.dalara.factucore.application.port.out.DocumentoDefinitionProvider;
import ec.dalara.factucore.application.port.out.WorkflowExecutionPort;
import ec.dalara.factucore.domain.shared.EstadoRegistro;
import ec.dalara.factucore.domain.shared.MessageCodes;
import ec.dalara.factucore.domain.workflow.ContextoWorkflow;
import ec.dalara.factucore.domain.workflow.EstadoProceso;
import ec.dalara.factucore.infrastructure.persistence.entity.Comprobante;
import ec.dalara.factucore.infrastructure.persistence.repository.ComprobanteEvidenciaRepository;
import ec.dalara.factucore.infrastructure.persistence.repository.ComprobanteRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ComprobanteReprocessService {

	private static final String XML_GENERADO = "XML_GENERADO";

	private final ComprobanteRepository comprobanteRepository;
	private final ComprobanteEvidenciaRepository evidenciaRepository;
	private final DocumentoDefinitionProvider documentoDefinitionProvider;
	private final WorkflowExecutionPort workflowExecutionPort;
	private final Set<Long> comprobantesEnReproceso = ConcurrentHashMap.newKeySet();

	@Transactional
	public ComprobanteGeneracionResponse reprocesar(Long comprobanteId) {
		if (comprobanteId == null || !comprobantesEnReproceso.add(comprobanteId)) {
			throw new ApplicationException(MessageCodes.COMPROBANTE_EN_PROCESO);
		}
		try {
			Comprobante comprobante = comprobanteRepository
					.findByIdAndEstadoRegistro(comprobanteId, EstadoRegistro.ACTIVO)
					.orElseThrow(() -> new ApplicationException(MessageCodes.WORKFLOW_COMPROBANTE_REQUERIDO));

			validarEstadoReprocesable(comprobante);
			var solicitud = crearSolicitudDeReproceso(comprobante);
			ContextoWorkflow contexto = ContextoWorkflow.existente(comprobante, solicitud);
			contexto.setDefinicionDocumento(documentoDefinitionProvider
					.obtenerDefinicion(comprobante.getDocumentoXsd().getId(), solicitud.getVersionXsd())
					.orElseThrow(() -> new ApplicationException(MessageCodes.COMPROBANTE_DEFINICION_NO_ENCONTRADA)));

			if (requiereXmlGenerado(comprobante.getEstadoProceso())) {
				contexto.setXml(leerXmlGenerado(comprobante));
			}
			if (requiereXmlFirmado(comprobante.getEstadoProceso())) {
				contexto.setXmlFirmado(leerXmlFirmado(comprobante));
			}
			return workflowExecutionPort.reprocesar(contexto);
		} finally {
			comprobantesEnReproceso.remove(comprobanteId);
		}
	}

	@Transactional(readOnly = true)
	public boolean estaPendienteAutorizacion(Long comprobanteId) {
		return comprobanteRepository.findByIdAndEstadoRegistro(comprobanteId, EstadoRegistro.ACTIVO)
				.map(Comprobante::getEstadoProceso).filter(EstadoProceso.AUTORIZACION_PENDIENTE.name()::equals)
				.isPresent();
	}

	private ComprobanteGeneracionRequest crearSolicitudDeReproceso(Comprobante comprobante) {
		return ComprobanteGeneracionRequest.builder().idTransaccion(comprobante.getIdTransaccion())
				.idDocumentoOrigen(comprobante.getIdDocumentoOrigen()).fechaInicio(comprobante.getFechaCreacion())
				.versionXsd(comprobante.getVersionDocumentoXsd().getVersion())
				.usuario(comprobante.getUsuarioModificacion() == null || comprobante.getUsuarioModificacion().isBlank()
						? comprobante.getUsuarioCreacion()
						: comprobante.getUsuarioModificacion())
				.canal("REPROCESO").idEmpresa(comprobante.getEmpresa().getId())
				.idEstablecimiento(comprobante.getEstablecimiento().getId())
				.idPuntoEmision(comprobante.getPuntoEmision().getId())
				.idTipoDocumento(comprobante.getDocumentoXsd().getId()).build();
	}

	private void validarEstadoReprocesable(Comprobante comprobante) {
		String estado = comprobante.getEstadoProceso();
		boolean reprocesable = EstadoProceso.XML_GENERADO.name().equals(estado)
				|| EstadoProceso.XSD_VALIDADO.name().equals(estado) || EstadoProceso.FIRMADO.name().equals(estado)
				|| EstadoProceso.ENVIADO_SRI.name().equals(estado)
				|| EstadoProceso.AUTORIZACION_PENDIENTE.name().equals(estado)
				|| (EstadoProceso.AUTORIZACION_SRI.name().equals(estado)
						&& "AUTORIZADO".equalsIgnoreCase(comprobante.getEstadoSri()))
				|| EstadoProceso.RIDE_GENERADO.name().equals(estado);
		if (!reprocesable) {
			throw new ApplicationException(MessageCodes.WORKFLOW_COMPROBANTE_REQUERIDO);
		}
	}

	private boolean requiereXmlGenerado(String estado) {
		return EstadoProceso.XML_GENERADO.name().equals(estado) || EstadoProceso.XSD_VALIDADO.name().equals(estado);
	}

	private boolean requiereXmlFirmado(String estado) {
		return EstadoProceso.FIRMADO.name().equals(estado) || EstadoProceso.ENVIADO_SRI.name().equals(estado)
				|| EstadoProceso.AUTORIZACION_PENDIENTE.name().equals(estado);
	}

	private String leerXmlGenerado(Comprobante comprobante) {
		var evidencia = evidenciaRepository
				.findByComprobanteIdAndTipoEvidenciaAndActualTrue(comprobante.getId(), XML_GENERADO)
				.orElseThrow(() -> new ApplicationException(MessageCodes.XSD_VERSION_NO_ENCONTRADA));
		try {
			return Files.readString(Path.of(evidencia.getRutaArchivo()));
		} catch (IOException | RuntimeException exception) {
			throw new ApplicationException(MessageCodes.XSD_VERSION_NO_ENCONTRADA);
		}
	}

	private String leerXmlFirmado(Comprobante comprobante) {
		if (comprobante.getRutaXmlFirmado() == null || comprobante.getRutaXmlFirmado().isBlank()) {
			throw new ApplicationException(MessageCodes.FIRMA_XML_REQUERIDO);
		}
		try {
			return Files.readString(Path.of(comprobante.getRutaXmlFirmado()));
		} catch (IOException | RuntimeException exception) {
			throw new ApplicationException(MessageCodes.FIRMA_XML_REQUERIDO);
		}
	}
}
