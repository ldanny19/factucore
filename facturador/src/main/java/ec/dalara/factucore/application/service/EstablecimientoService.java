package ec.dalara.factucore.application.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ec.dalara.factucore.application.ApplicationException;
import ec.dalara.factucore.domain.shared.EstadoRegistro;
import ec.dalara.factucore.domain.shared.MessageCodes;
import ec.dalara.factucore.infrastructure.persistence.entity.Establecimiento;
import ec.dalara.factucore.infrastructure.persistence.repository.BaseRepository;
import ec.dalara.factucore.infrastructure.persistence.repository.EstablecimientoRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EstablecimientoService extends BaseService<Establecimiento> {

    private final EstablecimientoRepository establecimientoRepository;

    @Override
    protected BaseRepository<Establecimiento, Long> getRepository() {
        return establecimientoRepository;
    }

    @Override
    @Transactional
    public Establecimiento guardar(Establecimiento establecimiento) {
        validar(establecimiento);

        boolean duplicado = establecimiento.getId() == null
                ? establecimientoRepository.existsByEmpresaIdAndCodigo(
                        establecimiento.getEmpresa().getId(), establecimiento.getCodigo())
                : establecimientoRepository.existsByEmpresaIdAndCodigoAndIdNot(
                        establecimiento.getEmpresa().getId(), establecimiento.getCodigo(), establecimiento.getId());

        if (duplicado) {
            throw new ApplicationException(
                    MessageCodes.ESTABLECIMIENTO_EMPRESA_CODIGO_DUPLICADO,
                    establecimiento.getEmpresa().getId(), establecimiento.getCodigo());
        }

        return super.guardar(establecimiento);
    }

    public List<Establecimiento> listarPorEmpresa(Long empresaId) {
        if (empresaId == null) {
            return List.of();
        }

        return establecimientoRepository.findByEmpresaId(empresaId).stream()
                .filter(establecimiento -> !EstadoRegistro.ELIMINADO.equals(establecimiento.getEstadoRegistro()))
                .toList();
    }

    public Optional<Establecimiento> obtenerPorEmpresaYCodigo(Long empresaId, String codigo) {
        if (empresaId == null || codigo == null || codigo.isBlank()) {
            return Optional.empty();
        }

        return establecimientoRepository.findByEmpresaIdAndCodigo(empresaId, codigo)
                .filter(establecimiento -> EstadoRegistro.ACTIVO.equals(establecimiento.getEstadoRegistro()));
    }

    public boolean existePorEmpresaYCodigo(Long empresaId, String codigo) {
        if (empresaId == null || codigo == null || codigo.isBlank()) {
            return false;
        }

        return establecimientoRepository.findByEmpresaIdAndCodigo(empresaId, codigo)
                .filter(establecimiento -> !EstadoRegistro.ELIMINADO.equals(establecimiento.getEstadoRegistro()))
                .isPresent();
    }

    private void validar(Establecimiento establecimiento) {
        if (establecimiento == null) {
            throw new ApplicationException(MessageCodes.ESTABLECIMIENTO_REQUERIDO);
        }

        if (establecimiento.getEmpresa() == null || establecimiento.getEmpresa().getId() == null) {
            throw new ApplicationException(MessageCodes.ESTABLECIMIENTO_EMPRESA_REQUERIDA);
        }

        if (establecimiento.getCodigo() == null || establecimiento.getCodigo().isBlank()) {
            throw new ApplicationException(MessageCodes.ESTABLECIMIENTO_CODIGO_REQUERIDO);
        }

        if (!establecimiento.getCodigo().matches("\\d{3}")) {
            throw new ApplicationException(MessageCodes.ESTABLECIMIENTO_CODIGO_FORMATO_INVALIDO);
        }

        if (establecimiento.getDireccion() == null || establecimiento.getDireccion().isBlank()) {
            throw new ApplicationException(MessageCodes.ESTABLECIMIENTO_DIRECCION_REQUERIDA);
        }
    }
}
