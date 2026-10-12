package ec.dalara.factucore.application.port.out;

import ec.dalara.factucore.domain.certificadofirma.CertificadoFirmaModel;

public interface FirmaElectronicaPort {

	String firmar(String xml, CertificadoFirmaModel certificado, char[] password);
}