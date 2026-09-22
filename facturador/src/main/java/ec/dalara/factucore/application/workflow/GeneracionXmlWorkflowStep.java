package ec.dalara.factucore.application.workflow;

import org.springframework.stereotype.Component;

import ec.dalara.factucore.application.port.out.DocumentoDefinitionProvider;
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

    @Override
    public EtapaWorkflow etapa() {
        return EtapaWorkflow.GENERACION_XML;
    }

    @Override
    public ResultadoEtapa ejecutar(ContextoWorkflow contexto) {
        var solicitud = contexto.getSolicitud();

        var definition = definitionProvider
                .obtenerDefinicionVigente(solicitud.getTipoDocumento(), solicitud.getFechaInicio().toLocalDateTime())
                .orElseThrow(() -> new WorkflowException(MessageCodes.XSD_VERSION_NO_ENCONTRADA));

        var datos = solicitud.getDatos().stream()
                .collect(java.util.stream.Collectors.toMap(
                        item -> item.getKey(), item -> item.getValue(),
                        (primero, segundo) -> segundo,
                        java.util.LinkedHashMap::new));

        if (contexto.getClaveAcceso() == null || contexto.getClaveAcceso().isBlank()) {
            throw new WorkflowException(MessageCodes.CLAVE_ACCESO_REQUERIDA);
        }

        // Valores generados por el workflow tienen precedencia sobre los datos de entrada.
        datos.put("claveAcceso", contexto.getClaveAcceso());

        if (contexto.getSecuencial() != null && !contexto.getSecuencial().isBlank()) {
            datos.put("secuencial", contexto.getSecuencial());
        }

        String xml = xmlGenerator.generar(definition, datos);
        contexto.setXml(xml);

        return ResultadoEtapa.exitosa(
                EtapaWorkflow.GENERACION_XML, "COMPLETADA",
                java.util.Map.of("xmlGenerado", true));
    }
}
