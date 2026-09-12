package ec.dalara.factucore.application.service;

import ec.dalara.factucore.domain.shared.EstadoRegistro;
import ec.dalara.factucore.infrastructure.persistence.entity.ElementoXsd;
import ec.dalara.factucore.infrastructure.persistence.repository.BaseRepository;
import ec.dalara.factucore.infrastructure.persistence.repository.ElementoXsdRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ElementoXsdService extends BaseService<ElementoXsd> {

    private final ElementoXsdRepository elementoXsdRepository;

    @Override
    protected BaseRepository<ElementoXsd, Long> getRepository() {
        return elementoXsdRepository;
    }

    public List<ElementoXsd> listarPorVersionDocumentoXsd(
            Long versionDocumentoXsdId
    ) {
        return elementoXsdRepository
                .findByVersionDocumentoXsdId(versionDocumentoXsdId)
                .stream()
                .filter(elemento ->
                        !EstadoRegistro.ELIMINADO.equals(
                                elemento.getEstadoRegistro()
                        )
                )
                .toList();
    }

    public List<ElementoXsd> listarPorElementoPadre(
            Long elementoPadreId
    ) {
        return elementoXsdRepository
                .findByElementoPadreId(elementoPadreId)
                .stream()
                .filter(elemento ->
                        !EstadoRegistro.ELIMINADO.equals(
                                elemento.getEstadoRegistro()
                        )
                )
                .toList();
    }
}