package ec.dalara.factucore.application.service;

import ec.dalara.factucore.domain.shared.EstadoRegistro;
import ec.dalara.factucore.infrastructure.persistence.entity.Empresa;
import ec.dalara.factucore.infrastructure.persistence.repository.BaseRepository;
import ec.dalara.factucore.infrastructure.persistence.repository.EmpresaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmpresaService extends BaseService<Empresa> {

    private final EmpresaRepository empresaRepository;

    @Override
    protected BaseRepository<Empresa, Long> getRepository() {
        return empresaRepository;
    }

    public Optional<Empresa> obtenerPorRuc(String ruc) {
        return empresaRepository.findByRuc(ruc)
                .filter(empresa ->
                        EstadoRegistro.ACTIVO.equals(
                                empresa.getEstadoRegistro()
                        )
                );
    }

    public boolean existePorRuc(String ruc) {
        return empresaRepository.findByRuc(ruc)
                .filter(empresa ->
                        !EstadoRegistro.ELIMINADO.equals(
                                empresa.getEstadoRegistro()
                        )
                )
                .isPresent();
    }
}