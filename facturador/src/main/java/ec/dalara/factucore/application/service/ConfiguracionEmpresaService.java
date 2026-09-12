package ec.dalara.factucore.application.service;

import ec.dalara.factucore.domain.shared.EstadoRegistro;
import ec.dalara.factucore.infrastructure.persistence.entity.ConfiguracionEmpresa;
import ec.dalara.factucore.infrastructure.persistence.repository.BaseRepository;
import ec.dalara.factucore.infrastructure.persistence.repository.ConfiguracionEmpresaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ConfiguracionEmpresaService
        extends BaseService<ConfiguracionEmpresa> {

    private final ConfiguracionEmpresaRepository configuracionEmpresaRepository;

    @Override
    protected BaseRepository<ConfiguracionEmpresa, Long> getRepository() {
        return configuracionEmpresaRepository;
    }

    public List<ConfiguracionEmpresa> listarPorEmpresa(Long empresaId) {
        return configuracionEmpresaRepository
                .findByEmpresaId(empresaId)
                .stream()
                .filter(configuracion ->
                        !EstadoRegistro.ELIMINADO.equals(
                                configuracion.getEstadoRegistro()
                        )
                )
                .toList();
    }

    public Optional<ConfiguracionEmpresa> obtenerPorEmpresaYClave(
            Long empresaId,
            String clave
    ) {
        return configuracionEmpresaRepository
                .findByEmpresaIdAndClave(empresaId, clave)
                .filter(configuracion ->
                        EstadoRegistro.ACTIVO.equals(
                                configuracion.getEstadoRegistro()
                        )
                );
    }

    public boolean existePorEmpresaYClave(
            Long empresaId,
            String clave
    ) {
        return configuracionEmpresaRepository
                .findByEmpresaIdAndClave(empresaId, clave)
                .filter(configuracion ->
                        !EstadoRegistro.ELIMINADO.equals(
                                configuracion.getEstadoRegistro()
                        )
                )
                .isPresent();
    }
}