package ec.dalara.factucore.application.port.out;

import ec.dalara.factucore.application.port.out.sri.SriResponse;

public interface ComprobanteEvidenciaPort {

    void guardarXmlFirmado(Long comprobanteId, String xmlFirmado);

    void guardarRespuestaSriRecepcion(Long comprobanteId, SriResponse respuesta);

    void guardarRespuestaSriAutorizacion(Long comprobanteId, SriResponse respuesta);

    void guardarRide(Long comprobanteId, byte[] pdf);
}
