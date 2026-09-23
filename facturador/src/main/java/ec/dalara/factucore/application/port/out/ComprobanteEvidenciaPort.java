package ec.dalara.factucore.application.port.out;

import ec.dalara.factucore.application.port.out.sri.SriResponse;

public interface ComprobanteEvidenciaPort {

    void guardarXmlGenerado(Long comprobanteId, String xml);

    void guardarXmlFirmado(Long comprobanteId, String xmlFirmado);

    void guardarRespuestaSriRecepcion(Long comprobanteId, SriResponse respuesta);

    void guardarRespuestaSriAutorizacion(Long comprobanteId, SriResponse respuesta);
}
