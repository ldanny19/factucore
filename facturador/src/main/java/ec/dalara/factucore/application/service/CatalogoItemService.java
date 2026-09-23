package ec.dalara.factucore.application.service;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ec.dalara.factucore.application.ApplicationException;
import ec.dalara.factucore.domain.shared.EstadoRegistro;
import ec.dalara.factucore.domain.shared.MessageCodes;
import ec.dalara.factucore.infrastructure.persistence.entity.CatalogoItem;
import ec.dalara.factucore.infrastructure.persistence.repository.BaseRepository;
import ec.dalara.factucore.infrastructure.persistence.repository.CatalogoItemRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CatalogoItemService extends BaseService<CatalogoItem> {
    private final CatalogoItemRepository catalogoItemRepository;
    @Override protected BaseRepository<CatalogoItem, Long> getRepository() { return catalogoItemRepository; }

    @Override
    @Transactional
    public CatalogoItem guardar(CatalogoItem item) {
        if (item == null) throw new ApplicationException(MessageCodes.CATALOGO_ITEM_REQUERIDO);
        if (item.getCatalogo() == null || item.getCatalogo().getId() == null)
            throw new ApplicationException(MessageCodes.CATALOGO_ITEM_CATALOGO_REQUERIDO);
        if (item.getCodigo() == null || item.getCodigo().isBlank())
            throw new ApplicationException(MessageCodes.CATALOGO_ITEM_CODIGO_REQUERIDO);
        if (item.getNombre() == null || item.getNombre().isBlank())
            throw new ApplicationException(MessageCodes.CATALOGO_ITEM_NOMBRE_REQUERIDO);
        boolean duplicado = item.getId() == null
                ? catalogoItemRepository.existsByCatalogoIdAndCodigo(item.getCatalogo().getId(), item.getCodigo())
                : catalogoItemRepository.existsByCatalogoIdAndCodigoAndIdNot(item.getCatalogo().getId(), item.getCodigo(), item.getId());
        if (duplicado) throw new ApplicationException(MessageCodes.CATALOGO_ITEM_DUPLICADO, item.getCatalogo().getId(), item.getCodigo());
        return super.guardar(item);
    }

    public List<CatalogoItem> listarPorCatalogo(Long catalogoId) {
        return catalogoItemRepository.findByCatalogoId(catalogoId).stream().filter(i -> !EstadoRegistro.ELIMINADO.equals(i.getEstadoRegistro())).toList();
    }
    public Optional<CatalogoItem> obtenerPorCatalogoYCodigo(Long catalogoId, String codigo) {
        return catalogoItemRepository.findByCatalogoIdAndCodigo(catalogoId, codigo).filter(i -> EstadoRegistro.ACTIVO.equals(i.getEstadoRegistro()));
    }
    public boolean existePorCatalogoYCodigo(Long catalogoId, String codigo) {
        return catalogoItemRepository.findByCatalogoIdAndCodigo(catalogoId, codigo).filter(i -> !EstadoRegistro.ELIMINADO.equals(i.getEstadoRegistro())).isPresent();
    }
}