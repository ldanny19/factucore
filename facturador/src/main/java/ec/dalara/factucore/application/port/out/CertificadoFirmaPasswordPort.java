package ec.dalara.factucore.application.port.out;

public interface CertificadoFirmaPasswordPort {

    char[] obtenerPassword(Long empresaId);
}
