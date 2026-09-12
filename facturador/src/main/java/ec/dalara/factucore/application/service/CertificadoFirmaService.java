package ec.dalara.factucore.application.service;

import ec.dalara.factucore.domain.shared.EstadoRegistro;
import ec.dalara.factucore.infrastructure.persistence.entity.CertificadoFirma;
import ec.dalara.factucore.infrastructure.persistence.repository.BaseRepository;
import ec.dalara.factucore.infrastructure.persistence.repository.CertificadoFirmaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CertificadoFirmaService extends BaseService<CertificadoFirma> {

    private final CertificadoFirmaRepository certificadoFirmaRepository;

    @Override
    protected BaseRepository<CertificadoFirma, Long> getRepository() {
        return certificadoFirmaRepository;
    }

    public List<CertificadoFirma> listarPorEmpresa(Long empresaId) {
        return certificadoFirmaRepository
                .findByEmpresaId(empresaId)
                .stream()
                .filter(certificado ->
                        !EstadoRegistro.ELIMINADO.equals(
                                certificado.getEstadoRegistro()
                        )
                )
                .toList();
    }
}