package ec.dalara.factucore.application.service;

import ec.dalara.factucore.domain.shared.EstadoRegistro;
import ec.dalara.factucore.infrastructure.persistence.entity.Establecimiento;
import ec.dalara.factucore.infrastructure.persistence.repository.BaseRepository;
import ec.dalara.factucore.infrastructure.persistence.repository.EstablecimientoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EstablecimientoService extends BaseService<Establecimiento> {

    private final EstablecimientoRepository establecimientoRepository;

    @Override
    protected BaseRepository<Establecimiento, Long> getRepository() {
        return establecimientoRepository;
    }

    public List<Establecimiento> listarPorEmpresa(Long empresaId) {
        return establecimientoRepository.findByEmpresaId(empresaId)
                .stream()
                .filter(establecimiento ->
                        !EstadoRegistro.ELIMINADO.equals(
                                establecimiento.getEstadoRegistro()
                        )
                )
                .toList();
    }

    public Optional<Establecimiento> obtenerPorEmpresaYCodigo(
            Long empresaId,
            String codigo
    ) {
        return establecimientoRepository
                .findByEmpresaIdAndCodigo(empresaId, codigo)
                .filter(establecimiento ->
                        EstadoRegistro.ACTIVO.equals(
                                establecimiento.getEstadoRegistro()
                        )
                );
    }

    public boolean existePorEmpresaYCodigo(
            Long empresaId,
            String codigo
    ) {
        return establecimientoRepository
                .findByEmpresaIdAndCodigo(empresaId, codigo)
                .filter(establecimiento ->
                        !EstadoRegistro.ELIMINADO.equals(
                                establecimiento.getEstadoRegistro()
                        )
                )
                .isPresent();
    }
}