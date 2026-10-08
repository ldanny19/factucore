package ec.dalara.factucore.application.workflow;

import ec.dalara.factucore.application.workflow.WorkflowEtapaExecutor;

import ec.dalara.factucore.application.workflow.WorkflowResultadoService;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import ec.dalara.factucore.application.ApplicationException;

import ec.dalara.factucore.application.MessageResolver;
import ec.dalara.factucore.application.port.out.ComprobanteEvidenciaPort;
import ec.dalara.factucore.application.port.out.DocumentoDefinitionProvider;
import ec.dalara.factucore.application.port.out.XmlValidatorPort;
import ec.dalara.factucore.application.service.ComprobanteService;
import ec.dalara.factucore.application.service.DocumentoXsdService;
import ec.dalara.factucore.application.service.EmisionService;
import ec.dalara.factucore.application.service.EmpresaService;
import ec.dalara.factucore.application.service.VersionDocumentoXsdService;
import ec.dalara.factucore.domain.shared.EstadoRegistro;
import ec.dalara.factucore.domain.shared.MessageCodes;
import ec.dalara.factucore.domain.workflow.ContextoWorkflow;
import ec.dalara.factucore.domain.workflow.EstadoProceso;
import ec.dalara.factucore.domain.workflow.EtapaWorkflow;
import ec.dalara.factucore.domain.workflow.ResultadoEtapa;
import ec.dalara.factucore.application.validation.ComprobanteValidationResult;
import ec.dalara.factucore.infrastructure.configuration.sri.SriProperties;
import ec.dalara.factucore.infrastructure.persistence.entity.Comprobante;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ValidacionXsdWorkflowStep {

	private final WorkflowEtapaExecutor workflowEtapaExecutor;
	private final MessageResolver messageResolver;
	private final XmlValidatorPort xmlValidator;
	private final ComprobanteEvidenciaPort evidenciaPort;
	private final ComprobanteService comprobanteService;
	private final EmpresaService empresaService;
	private final EmisionService emisionService;
	private final DocumentoDefinitionProvider definitionProvider;
	private final DocumentoXsdService documentoXsdService;
	private final VersionDocumentoXsdService versionDocumentoXsdService;
	private final SriProperties sriProperties;

	public EtapaWorkflow etapa() {
		return EtapaWorkflow.VALIDACION_XSD;
	}

	/**
	 * Valida el XML y, únicamente si la validación es exitosa, persiste el
	 * comprobante. La persistencia forma parte de esta etapa y no constituye un
	 * nodo independiente del workflow.
	 */
	public String ejecutar(ContextoWorkflow contexto) {
		return workflowEtapaExecutor.ejecutar(contexto, etapa(), () -> ejecutarResultado(contexto));
	}

	private ResultadoEtapa ejecutarResultado(ContextoWorkflow contexto) {
		if (contexto == null || contexto.getXml() == null || contexto.getXml().isBlank()) {
			throw new ApplicationException(MessageCodes.FIRMA_XML_REQUERIDO);
		}

		var definition = contexto.getDefinicionDocumento();
		if (definition == null) {
			throw new ApplicationException(MessageCodes.COMPROBANTE_DEFINICION_NO_ENCONTRADA);
		}

		// Si el XML no cumple el XSD, se lanza la excepción y no se persiste.
		ComprobanteValidationResult validacion = xmlValidator.validar(contexto.getXml(), definition);
		contexto.setValidacionXsd(validacion);
		if (!validacion.esValido()) {
			String codigo = validacion.getErrores().isEmpty() ? MessageCodes.WORKFLOW_ETAPA_ERROR : validacion.getErrores().get(0).getCodigo();
			String mensaje = validacion.getErrores().isEmpty() ? null : validacion.getErrores().get(0).getMensaje();
			return ResultadoEtapa.fallida(etapa(), EstadoProceso.ERROR.name(), codigo, mensaje,
					java.util.Map.of("validacion", validacion));
		}

		// La persistencia ocurre solamente después de validar correctamente el XML.
		persistirComprobante(contexto, definition);

		evidenciaPort.guardarXmlGenerado(contexto.getComprobanteId(), contexto.getXml(),
				contexto.getSolicitud().getUsuario());

		contexto.getComprobante().setEstadoProceso(EstadoProceso.XSD_VALIDADO.name());

		return ResultadoEtapa.exitosa(etapa(), EstadoProceso.XSD_VALIDADO.name());
	}

	private void persistirComprobante(ContextoWorkflow contexto,
			ec.dalara.factucore.domain.documentoxsd.DocumentDefinitionModel definition) {
		var solicitud = contexto.getSolicitud();

		if (solicitud == null) {
			throw new ApplicationException(MessageCodes.COMPROBANTE_REQUEST_REQUERIDO);
		}

		if (contexto.getClaveAcceso() == null || contexto.getClaveAcceso().isBlank()) {
			throw new ApplicationException(MessageCodes.CLAVE_ACCESO_REQUERIDA);
		}

		// En una reanudación el comprobante ya existe; no se crea otro registro.
		if (contexto.getComprobante() != null) {
			return;
		}

		var empresa = empresaService.obtenerPorId(solicitud.getIdEmpresa())
				.orElseThrow(() -> new ApplicationException(MessageCodes.COMPROBANTE_EMPRESA_REQUERIDA));

		var emision = emisionService.resolver(solicitud.getIdEmpresa(), solicitud.getIdEstablecimiento(),
				solicitud.getIdPuntoEmision());

		var documentoXsd = documentoXsdService.obtenerPorCodigo(definition.getDocumento().getCodigo())
				.orElseThrow(() -> new ApplicationException(MessageCodes.COMPROBANTE_DEFINICION_NO_ENCONTRADA));

		var versionXsd = versionDocumentoXsdService.obtenerPorId(definition.getVersion().getId())
				.orElseThrow(() -> new ApplicationException(MessageCodes.XSD_VERSION_NO_ENCONTRADA));

		var existente = comprobanteService.obtenerPorClaveAcceso(contexto.getClaveAcceso())
				.filter(c -> !EstadoRegistro.ELIMINADO.equals(c.getEstadoRegistro()));

		if (existente.isPresent()) {
			contexto.asignarComprobante(existente.get());
			return;
		}

		Comprobante comprobante = Comprobante.builder()
				.empresa(empresa)
				.establecimiento(emision.establecimiento())
				.puntoEmision(emision.puntoEmision())
				.documentoXsd(documentoXsd)
				.versionDocumentoXsd(versionXsd)
				.idTransaccion(solicitud.getIdTransaccion())
				.ambiente(resolverAmbiente(sriProperties.getAmbiente()))
				.tipoEmision("1")
				.codigoDocumento(definition.getDocumento().getCodigo())
				.secuencial(contexto.getSecuencial())
				.claveAcceso(contexto.getClaveAcceso())
				.fechaEmision(solicitud.getFechaInicio().toLocalDate())
				.razonSocialEmisor(empresa.getRazonSocial())
				.nombreComercialEmisor(empresa.getNombreComercial())
				.rucEmisor(empresa.getRuc())
				.direccionMatrizEmisor(empresa.getDireccionMatriz())
				.direccionEstablecimientoEmisor(emision.establecimiento().getDireccion())
				.estadoProceso(EstadoProceso.CLAVE_ACCESO_GENERADA.name())
				.estadoRegistro(EstadoRegistro.ACTIVO)
				.usuarioCreacion(solicitud.getUsuario())
				.fechaCreacion(LocalDateTime.now())
				.build();

		comprobante = comprobanteService.guardar(comprobante);
		contexto.asignarComprobante(comprobante);
	}

	private String resolverAmbiente(String ambiente) {
		if ("PRUEBAS".equalsIgnoreCase(ambiente)) {
			return "1";
		}
		if ("PRODUCCION".equalsIgnoreCase(ambiente)) {
			return "2";
		}
		throw new ApplicationException(MessageCodes.SRI_AMBIENTE_REQUERIDO);
	}
}
