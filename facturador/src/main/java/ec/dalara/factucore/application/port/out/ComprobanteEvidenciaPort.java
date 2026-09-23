package ec.dalara.factucore.application.port.out;

import ec.dalara.factucore.application.port.out.sri.SriResponse;

public interface ComprobanteEvidenciaPort {

    String guardarXmlFirmado(Long comprobanteId, String xmlFirmado);

    String guardarRespuestaSriRecepcion(Long comprobanteId, SriResponse respuesta);

    String guardarRespuestaSriAutorizacion(Long comprobanteId, SriResponse respuesta);

    String guardarRide(Long comprobanteId, byte[] pdf);
}
