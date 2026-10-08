package ec.dalara.factucore.application.workflow;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

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

	private static final Logger LOGGER = LoggerFactory.getLogger(GeneracionXmlWorkflowStep.class);

	private final DocumentoDefinitionProvider definitionProvider;
	private final DocumentoXsdService documentoXsdService;
	private final XmlGeneratorPort xmlGenerator;
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
					.obtenerDefinicion(solicitud.getIdTipoDocumento(), solicitud.getVersionXsd())
					.orElseThrow(() -> new WorkflowException(MessageCodes.XSD_VERSION_NO_ENCONTRADA));
			contexto.setDefinicionDocumento(definition);
		}

		var datos = solicitud.getDatos() == null || solicitud.getDatos().isNull() || !solicitud.getDatos().isObject()
				? new LinkedHashMap<String, Object>()
				: objectMapper.convertValue(solicitud.getDatos(),
						new TypeReference<LinkedHashMap<String, Object>>() {
						});

		if (contexto.getClaveAcceso() == null || contexto.getClaveAcceso().isBlank()) {
			throw new WorkflowException(MessageCodes.CLAVE_ACCESO_REQUERIDA);
		}

		Map<String, Object> contextoFactuCore = new LinkedHashMap<>();
		contextoFactuCore.put("idEmpresa", solicitud.getIdEmpresa());
		contextoFactuCore.put("idEstablecimiento", solicitud.getIdEstablecimiento());
		contextoFactuCore.put("idPuntoEmision", solicitud.getIdPuntoEmision());
		contextoFactuCore.put("idTipoDocumento", solicitud.getIdTipoDocumento());
		contextoFactuCore.put("idTransaccion", solicitud.getIdTransaccion());
		contextoFactuCore.put("versionXsd", solicitud.getVersionXsd());
		contextoFactuCore.put("usuario", solicitud.getUsuario());
		contextoFactuCore.put("canal", solicitud.getCanal());

		Map<String, Object> contextoGenerado = new LinkedHashMap<>(contexto.getValoresGenerados());
		contextoGenerado.put("claveAcceso", contexto.getClaveAcceso());

		if (contexto.getSecuencial() != null && !contexto.getSecuencial().isBlank()) {
			contextoGenerado.put("secuencial", contexto.getSecuencial());
		}

		String xml = xmlGenerator.generar(
				definition,
				datos,
				contextoFactuCore,
				contextoGenerado);
		contexto.setXml(xml);

		LOGGER.info("XML generado antes de validacion XSD:\n{}", xml);

		return ResultadoEtapa.exitosa(etapa(), "COMPLETADA", java.util.Map.of("xmlGenerado", true));
	}
}
