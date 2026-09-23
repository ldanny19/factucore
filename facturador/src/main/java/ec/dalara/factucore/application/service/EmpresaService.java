package ec.dalara.factucore.application.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ec.dalara.factucore.application.ApplicationException;
import ec.dalara.factucore.domain.empresa.Ruc;
import ec.dalara.factucore.domain.shared.EstadoRegistro;
import ec.dalara.factucore.domain.shared.MessageCodes;
import ec.dalara.factucore.infrastructure.persistence.entity.Empresa;
import ec.dalara.factucore.infrastructure.persistence.repository.BaseRepository;
import ec.dalara.factucore.infrastructure.persistence.repository.EmpresaRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmpresaService extends BaseService<Empresa> {

    private final EmpresaRepository empresaRepository;

    @Override
    protected BaseRepository<Empresa, Long> getRepository() {
        return empresaRepository;
    }

    @Override
    @Transactional
    public Empresa guardar(Empresa empresa) {
        validar(empresa);

        boolean duplicado = empresa.getId() == null
                ? empresaRepository.existsByRuc(empresa.getRuc())
                : empresaRepository.existsByRucAndIdNot(empresa.getRuc(), empresa.getId());

        if (duplicado) {
            throw new ApplicationException(MessageCodes.EMPRESA_RUC_DUPLICADO, empresa.getRuc());
        }

        return super.guardar(empresa);
    }

    public Optional<Empresa> obtenerPorRuc(String ruc) {
        if (ruc == null || ruc.isBlank()) {
            return Optional.empty();
        }

        return empresaRepository.findByRuc(ruc)
                .filter(empresa -> EstadoRegistro.ACTIVO.equals(empresa.getEstadoRegistro()));
    }

    public boolean existePorRuc(String ruc) {
        if (ruc == null || ruc.isBlank()) {
            return false;
        }

        return empresaRepository.findByRuc(ruc)
                .filter(empresa -> !EstadoRegistro.ELIMINADO.equals(empresa.getEstadoRegistro()))
                .isPresent();
    }

    private void validar(Empresa empresa) {
        if (empresa == null) {
            throw new ApplicationException(MessageCodes.EMPRESA_REQUERIDA);
        }

        if (empresa.getRuc() == null || empresa.getRuc().isBlank()) {
            throw new ApplicationException(MessageCodes.EMPRESA_RUC_REQUERIDO);
        }

        new Ruc(empresa.getRuc());

        if (empresa.getRazonSocial() == null || empresa.getRazonSocial().isBlank()) {
            throw new ApplicationException(MessageCodes.EMPRESA_RAZON_SOCIAL_REQUERIDA);
        }

        if (empresa.getDireccionMatriz() == null || empresa.getDireccionMatriz().isBlank()) {
            throw new ApplicationException(MessageCodes.EMPRESA_DIRECCION_MATRIZ_REQUERIDA);
        }

        if (empresa.getObligadoContabilidad() == null) {
            throw new ApplicationException(MessageCodes.EMPRESA_OBLIGADO_CONTABILIDAD_REQUERIDO);
        }

        if (empresa.getContribuyenteRimpe() == null) {
            throw new ApplicationException(MessageCodes.EMPRESA_CONTRIBUYENTE_RIMPE_REQUERIDO);
        }
    }
}
