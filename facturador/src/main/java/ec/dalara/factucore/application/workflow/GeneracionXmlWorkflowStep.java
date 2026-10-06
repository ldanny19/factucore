package ec.dalara.factucore.application.workflow;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import ec.dalara.factucore.application.port.out.ComprobanteEvidenciaPort;
import ec.dalara.factucore.application.port.out.DocumentoDefinitionProvider;
import ec.dalara.factucore.application.port.out.XmlGeneratorPort;
import ec.dalara.factucore.application.service.DocumentoXsdService;
import ec.dalara.factucore.domain.shared.MessageCodes;
import ec.dalara.factucore.domain.workflow.ContextoWorkflow;
import ec.dalara.factucore.domain.workflow.EtapaWorkflow;
import ec.dalara.factucore.domain.workflow.ResultadoEtapa;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GeneracionXmlWorkflowStep implements WorkflowStep {

	private final DocumentoDefinitionProvider definitionProvider;
	private final DocumentoXsdService documentoXsdService;
	private final XmlGeneratorPort xmlGenerator;
	private final ComprobanteEvidenciaPort evidenciaPort;
	private final ObjectMapper objectMapper;

	@Override
	public EtapaWorkflow etapa() {
		return EtapaWorkflow.GENERACION_XML;
	}

	@Override
	public ResultadoEtapa ejecutar(ContextoWorkflow contexto) {
		var solicitud = contexto.getSolicitud();
		var definition = contexto.getDefinicionDocumento();

		if (definition == null) {
			var documento = documentoXsdService.obtenerPorId(solicitud.getIdTipoDocumento())
					.orElseThrow(() -> new WorkflowException(MessageCodes.COMPROBANTE_DEFINICION_NO_ENCONTRADA));

				definition = definitionProvider
					.obtenerDefinicionVigente(documento.getCodigo(),
							solicitud.getFechaInicio().toLocalDateTime())
					.orElseThrow(() -> new WorkflowException(MessageCodes.XSD_VERSION_NO_ENCONTRADA));
			contexto.setDefinicionDocumento(definition);
		}

		var datos = solicitud.getDatos() == null || solicitud.getDatos().isNull() || !solicitud.getDatos().isObject()
				? new java.util.LinkedHashMap<String, Object>()
				: objectMapper.convertValue(solicitud.getDatos(),
						new TypeReference<java.util.LinkedHashMap<String, Object>>() {});

		if (contexto.getClaveAcceso() == null || contexto.getClaveAcceso().isBlank()) {
			throw new WorkflowException(MessageCodes.CLAVE_ACCESO_REQUERIDA);
		}

		datos.put("claveAcceso", contexto.getClaveAcceso());
		if (contexto.getSecuencial() != null && !contexto.getSecuencial().isBlank()) {
			datos.put("secuencial", contexto.getSecuencial());
		}

		String xml = xmlGenerator.generar(definition, datos, contexto.getValoresGenerados());
		contexto.setXml(xml);
		evidenciaPort.guardarXmlGenerado(contexto.getComprobanteId(), xml, solicitud.getUsuario());

		return ResultadoEtapa.exitosa(etapa(), "COMPLETADA", java.util.Map.of("xmlGenerado", true));
	}
}
