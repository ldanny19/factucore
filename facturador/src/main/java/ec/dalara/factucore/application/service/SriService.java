package ec.dalara.factucore.application.service;

import org.springframework.stereotype.Service;

import ec.dalara.factucore.application.ApplicationException;
import ec.dalara.factucore.application.port.out.SriPort;
import ec.dalara.factucore.application.port.out.sri.SriCommunicationException;
import ec.dalara.factucore.application.port.out.sri.SriResponse;
import ec.dalara.factucore.domain.shared.MessageCodes;
import ec.dalara.factucore.infrastructure.configuration.sri.SriProperties;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SriService {

    private final SriPort sriPort;
    private final SriProperties properties;

    public SriResponse enviar(String xml) {
        if (xml == null || xml.isBlank()) {
            throw new ApplicationException(MessageCodes.SRI_XML_REQUERIDO);
        }

        int maxIntentos = Math.max(properties.getEnvio().getMaxIntentos(), 1);
        for (int intento = 1; intento <= maxIntentos; intento++) {
            try {
                return sriPort.recibir(xml);
            } catch (SriCommunicationException exception) {
                if (intento == maxIntentos) {
                    throw new ApplicationException(MessageCodes.SRI_ERROR_COMUNICACION, exception.getMessage());
                }
            }
        }
        throw new ApplicationException(MessageCodes.SRI_ERROR_COMUNICACION);
    }

    public SriResponse autorizar(String claveAcceso) {
        if (claveAcceso == null || claveAcceso.isBlank()) {
            throw new ApplicationException(MessageCodes.SRI_CLAVE_ACCESO_REQUERIDA);
        }

        int maxIntentos = Math.max(properties.getAutorizacion().getMaxIntentos(), 1);
        for (int intento = 1; intento <= maxIntentos; intento++) {
            try {
                return sriPort.autorizar(claveAcceso);
            } catch (SriCommunicationException exception) {
                if (intento == maxIntentos) {
                    throw new ApplicationException(MessageCodes.SRI_ERROR_COMUNICACION, exception.getMessage());
                }
            }
        }
        throw new ApplicationException(MessageCodes.SRI_ERROR_COMUNICACION);
    }
}
