package ec.dalara.factucore.application.service;

import ec.dalara.factucore.application.contract.request.ComprobanteGeneracionRequest;
import ec.dalara.factucore.application.contract.response.ComprobanteGeneracionResponse;
import ec.dalara.factucore.application.port.in.ComprobanteWorkflowPort;
import ec.dalara.factucore.domain.workflow.EstadoProceso;
import ec.dalara.factucore.infrastructure.persistence.entity.Comprobante;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ComprobanteWorkflowService implements ComprobanteWorkflowPort {

    private final ComprobanteService comprobanteService;
    private final ComprobanteAuditoriaService comprobanteAuditoriaService;

    @Override
    @Transactional
    public ComprobanteGeneracionResponse procesar(
            ComprobanteGeneracionRequest request
    ) {
        validarRequest(request);

        /*
         * La implementación de cada etapa se conecta posteriormente
         * mediante los servicios/ports existentes:
         *
         * VALIDACIÓN
         * SECUENCIAL
         * CLAVE DE ACCESO
         * XML
         * XSD
         * FIRMA
         * SRI
         * AUTORIZACIÓN
         * RIDE
         *
         * Camel será quien determine el orden del workflow.
         */

        throw new UnsupportedOperationException(
                "Workflow pendiente de conexión con las etapas existentes"
        );
    }

    @Override
    @Transactional
    public void reprocesar(Long comprobanteId) {

        Comprobante comprobante = comprobanteService.obtenerPorId(comprobanteId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Comprobante no encontrado: " + comprobanteId
                ));

        EstadoProceso estado = convertirEstado(comprobante.getEstadoProceso());

        switch (estado) {
            case RECIBIDO,
                 VALIDADO,
                 SECUENCIAL_ASIGNADO,
                 CLAVE_ACCESO_GENERADA,
                 XML_GENERADO,
                 XML_VALIDADO,
                 FIRMADO,
                 ENVIADO_SRI,
                 AUTORIZADO,
                 RIDE_GENERADO,
                 ERROR -> continuarDesdeEstado(comprobante, estado);

            default -> throw new IllegalStateException(
                    "Estado de proceso no soportado: "
                            + comprobante.getEstadoProceso()
            );
        }
    }

    private void continuarDesdeEstado(
            Comprobante comprobante,
            EstadoProceso estado
    ) {
        /*
         * Esta lógica será invocada por el workflow Camel.
         *
         * No se reinicia el comprobante desde cero.
         * Se continúa desde la etapa persistida.
         */
    }

    private EstadoProceso convertirEstado(String estado) {
        if (estado == null || estado.isBlank()) {
            return EstadoProceso.RECIBIDO;
        }

        return EstadoProceso.valueOf(estado);
    }

    private void validarRequest(ComprobanteGeneracionRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("La solicitud es requerida");
        }
    }
}