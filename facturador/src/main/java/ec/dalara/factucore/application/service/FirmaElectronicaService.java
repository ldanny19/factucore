package ec.dalara.factucore.application.service;

import ec.dalara.factucore.application.port.out.FirmaElectronicaPort;
import ec.dalara.factucore.domain.firmaelectronica.CertificadoFirmaModel;
import ec.dalara.factucore.infrastructure.persistence.entity.CertificadoFirma;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FirmaElectronicaService {

    private final FirmaElectronicaPort firmaElectronicaPort;
    private final CertificadoFirmaService certificadoFirmaService;

    public String firmar(
            Long empresaId,
            String xml,
            char[] password,
            LocalDateTime fecha
    ) {
        CertificadoFirma certificado =
                certificadoFirmaService
                        .obtenerVigente(empresaId, fecha)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "No existe un certificado de firma vigente"
                                )
                        );

        CertificadoFirmaModel modelo =
                new CertificadoFirmaModel(
                        certificado.getId(),
                        certificado.getEmpresa().getId(),
                        certificado.getNombreArchivo(),
                        certificado.getRutaCertificado(),
                        certificado.getFechaInicio(),
                        certificado.getFechaFin()
                );

        return firmaElectronicaPort.firmar(
                xml,
                modelo,
                password
        );
    }
}