package ec.dalara.factucore.application.service;

import ec.dalara.factucore.domain.shared.EstadoRegistro;
import ec.dalara.factucore.infrastructure.persistence.entity.CatalogoItem;
import ec.dalara.factucore.infrastructure.persistence.repository.BaseRepository;
import ec.dalara.factucore.infrastructure.persistence.repository.CatalogoItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CatalogoItemService extends BaseService<CatalogoItem> {

    private final CatalogoItemRepository catalogoItemRepository;

    @Override
    protected BaseRepository<CatalogoItem, Long> getRepository() {
        return catalogoItemRepository;
    }

    public List<CatalogoItem> listarPorCatalogo(Long catalogoId) {
        return catalogoItemRepository
                .findByCatalogoId(catalogoId)
                .stream()
                .filter(item ->
                        !EstadoRegistro.ELIMINADO.equals(
                                item.getEstadoRegistro()
                        )
                )
                .toList();
    }

    public Optional<CatalogoItem> obtenerPorCatalogoYCodigo(
            Long catalogoId,
            String codigo
    ) {
        return catalogoItemRepository
                .findByCatalogoIdAndCodigo(catalogoId, codigo)
                .filter(item ->
                        EstadoRegistro.ACTIVO.equals(
                                item.getEstadoRegistro()
                        )
                );
    }

    public boolean existePorCatalogoYCodigo(
            Long catalogoId,
            String codigo
    ) {
        return catalogoItemRepository
                .findByCatalogoIdAndCodigo(catalogoId, codigo)
                .filter(item ->
                        !EstadoRegistro.ELIMINADO.equals(
                                item.getEstadoRegistro()
                        )
                )
                .isPresent();
    }
}