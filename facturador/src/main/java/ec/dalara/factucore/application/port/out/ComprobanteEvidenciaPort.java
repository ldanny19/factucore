package ec.dalara.factucore.application.port.out;

import ec.dalara.factucore.application.port.out.sri.SriResponse;

public interface ComprobanteEvidenciaPort {
    String guardarXmlGenerado(Long comprobanteId, String xmlGenerado, String usuario);
    String guardarXmlFirmado(Long comprobanteId, String xmlFirmado, String usuario);
    String guardarRespuestaSriAutorizacion(Long comprobanteId, SriResponse respuesta, String usuario);
    String guardarRide(Long comprobanteId, byte[] pdf, String usuario);
}