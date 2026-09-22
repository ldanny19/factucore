package ec.dalara.factucore.application.service;

import ec.dalara.factucore.application.ApplicationException;
import ec.dalara.factucore.application.contract.request.ComprobanteGeneracionRequest;
import ec.dalara.factucore.application.contract.response.ComprobanteGeneracionResponse;
import ec.dalara.factucore.application.port.in.ComprobanteWorkflowPort;
import ec.dalara.factucore.application.port.out.RidePort;
import ec.dalara.factucore.application.port.out.XmlGeneratorPort;
import ec.dalara.factucore.application.port.out.XmlValidatorPort;
import ec.dalara.factucore.application.port.out.sri.SriResponse;
import ec.dalara.factucore.domain.claveacceso.ClaveAccesoDatos;
import ec.dalara.factucore.domain.claveacceso.ClaveAccesoModel;
import ec.dalara.factucore.domain.documentoxsd.DocumentDefinitionModel;
import ec.dalara.factucore.infrastructure.persistence.entity.Comprobante;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ComprobanteWorkflowService
        implements ComprobanteWorkflowPort {

    private final ComprobanteService comprobanteService;
    private final SecuencialService secuencialService;
    private final ClaveAccesoService claveAccesoService;
    private final DocumentoDefinitionProvider documentoDefinitionProvider;
    private final XmlGeneratorPort xmlGeneratorPort;
    private final XmlValidatorPort xmlValidatorPort;
    private final FirmaElectronicaService firmaElectronicaService;
    private final SriService sriService;
    private final RidePort ridePort;
    private final ComprobanteAuditoriaService comprobanteAuditoriaService;

    @Override
    @Transactional
    public ComprobanteGeneracionResponse procesar(
            ComprobanteGeneracionRequest request
    ) {
        validarSolicitud(request);

        /*
         * La implementación completa del workflow debe ejecutarse
         * mediante las etapas externalizadas en Camel.
         *
         * Este servicio representa el punto de entrada de aplicación
         * y no debe convertirse en un orquestador monolítico de todas
         * las etapas.
         */
        throw new ApplicationException(
                "FACTUCORE.WORKFLOW.CONFIGURACION.NO_DISPONIBLE"
        );
    }

    @Override
    @Transactional(readOnly = true)
    public void reprocesar(Long comprobanteId) {

        if (comprobanteId == null) {
            throw new ApplicationException(
                    "FACTUCORE.WORKFLOW.COMPROBANTE.REQUERIDO"
            );
        }

        comprobanteService.obtener(comprobanteId)
                .orElseThrow(() ->
                        new ApplicationException(
                                "FACTUCORE.COMPROBANTE.NO_ENCONTRADO",
                                comprobanteId
                        )
                );
    }

    private void validarSolicitud(
            ComprobanteGeneracionRequest request
    ) {
        if (request == null) {
            throw new ApplicationException(
                    "FACTUCORE.WORKFLOW.SOLICITUD.REQUERIDA"
            );
        }
    }
}