package ec.dalara.factucore.application.service;

import ec.dalara.factucore.domain.shared.EstadoRegistro;
import ec.dalara.factucore.infrastructure.persistence.entity.Catalogo;
import ec.dalara.factucore.infrastructure.persistence.repository.BaseRepository;
import ec.dalara.factucore.infrastructure.persistence.repository.CatalogoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CatalogoService extends BaseService<Catalogo> {

    private final CatalogoRepository catalogoRepository;

    @Override
    protected BaseRepository<Catalogo, Long> getRepository() {
        return catalogoRepository;
    }

    public Optional<Catalogo> obtenerPorCodigo(String codigo) {
        return catalogoRepository.findByCodigo(codigo)
                .filter(catalogo ->
                        EstadoRegistro.ACTIVO.equals(
                                catalogo.getEstadoRegistro()
                        )
                );
    }

    public boolean existePorCodigo(String codigo) {
        return catalogoRepository.findByCodigo(codigo)
                .filter(catalogo ->
                        !EstadoRegistro.ELIMINADO.equals(
                                catalogo.getEstadoRegistro()
                        )
                )
                .isPresent();
    }
}