package ec.dalara.factucore.application.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import ec.dalara.factucore.application.ApplicationException;
import ec.dalara.factucore.application.port.out.FirmaElectronicaPort;
import ec.dalara.factucore.domain.shared.MessageCodes;
import ec.dalara.factucore.domain.certificadofirma.CertificadoFirmaModel;
import ec.dalara.factucore.infrastructure.persistence.entity.CertificadoFirma;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FirmaElectronicaService {

	private final FirmaElectronicaPort firmaElectronicaPort;
	private final CertificadoFirmaService certificadoFirmaService;

	public String firmar(Long empresaId, String xml, char[] password, LocalDateTime fecha) {
		CertificadoFirma certificado = certificadoFirmaService.obtenerVigente(empresaId, fecha).orElseThrow(
				() -> new ApplicationException(MessageCodes.FIRMA_CERTIFICADO_VIGENTE_NO_ENCONTRADO,
						empresaId, fecha));

		CertificadoFirmaModel modelo = new CertificadoFirmaModel(certificado.getId(), certificado.getEmpresa().getId(),
				certificado.getNombreArchivo(), certificado.getRutaCertificado(), certificado.getFechaInicio(),
				certificado.getFechaFin());

		return firmaElectronicaPort.firmar(xml, modelo, password);
	}
}