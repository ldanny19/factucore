package ec.dalara.factucore.application.workflow;

import org.springframework.stereotype.Component;

import ec.dalara.factucore.application.port.out.DocumentoDefinitionProvider;
import ec.dalara.factucore.application.port.out.ComprobanteEvidenciaPort;
import ec.dalara.factucore.application.port.out.XmlGeneratorPort;
import ec.dalara.factucore.domain.shared.MessageCodes;
import ec.dalara.factucore.domain.workflow.ContextoWorkflow;
import ec.dalara.factucore.domain.workflow.EtapaWorkflow;
import ec.dalara.factucore.domain.workflow.ResultadoEtapa;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GeneracionXmlWorkflowStep implements WorkflowStep {

    private final DocumentoDefinitionProvider definitionProvider;
    private final XmlGeneratorPort xmlGenerator;
    private final ComprobanteEvidenciaPort evidenciaPort;

    @Override
    public EtapaWorkflow etapa() {
        return EtapaWorkflow.GENERACION_XML;
    }

    @Override
    public ResultadoEtapa ejecutar(ContextoWorkflow contexto) {
        var solicitud = contexto.getSolicitud();
        var definition = contexto.getDefinicionDocumento();

        if (definition == null) {
            definition = definitionProvider
                    .obtenerDefinicionVigente(solicitud.getTipoDocumento(),
                            solicitud.getFechaInicio().toLocalDateTime())
                    .orElseThrow(() -> new WorkflowException(MessageCodes.XSD_VERSION_NO_ENCONTRADA));
            contexto.setDefinicionDocumento(definition);
        }

        var datos = solicitud.getDatos().stream()
                .collect(java.util.stream.Collectors.toMap(
                        item -> item.getKey(), item -> item.getValue(),
                        (primero, segundo) -> segundo,
                        java.util.LinkedHashMap::new));

        if (contexto.getClaveAcceso() == null || contexto.getClaveAcceso().isBlank()) {
            throw new WorkflowException(MessageCodes.CLAVE_ACCESO_REQUERIDA);
        }

        datos.put("claveAcceso", contexto.getClaveAcceso());
        if (contexto.getSecuencial() != null && !contexto.getSecuencial().isBlank()) {
            datos.put("secuencial", contexto.getSecuencial());
        }

        String xml = xmlGenerator.generar(definition, datos);
        contexto.setXml(xml);
        evidenciaPort.guardarXmlGenerado(contexto.getComprobanteId(), xml, solicitud.getUsuario());

        return ResultadoEtapa.exitosa(etapa(), "COMPLETADA",
                java.util.Map.of("xmlGenerado", true));
    }
}
