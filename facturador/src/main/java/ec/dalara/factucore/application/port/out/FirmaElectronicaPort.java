package ec.dalara.factucore.application.port.out;

public interface FirmaElectronicaPort {

    String firmar(String xml, Long idCertificadoFirma);
}