package ec.dalara.factucore.application.service;

import ec.dalara.factucore.domain.shared.EstadoRegistro;
import ec.dalara.factucore.infrastructure.persistence.entity.AtributoXsd;
import ec.dalara.factucore.infrastructure.persistence.repository.AtributoXsdRepository;
import ec.dalara.factucore.infrastructure.persistence.repository.BaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AtributoXsdService extends BaseService<AtributoXsd> {

    private final AtributoXsdRepository atributoXsdRepository;

    @Override
    protected BaseRepository<AtributoXsd, Long> getRepository() {
        return atributoXsdRepository;
    }

    public List<AtributoXsd> listarPorElementoXsd(Long elementoXsdId) {
        return atributoXsdRepository
                .findByElementoXsdId(elementoXsdId)
                .stream()
                .filter(atributo ->
                        !EstadoRegistro.ELIMINADO.equals(
                                atributo.getEstadoRegistro()
                        )
                )
                .toList();
    }

    public Optional<AtributoXsd> obtenerPorElementoXsdYNombre(
            Long elementoXsdId,
            String nombre
    ) {
        return atributoXsdRepository
                .findByElementoXsdIdAndNombre(
                        elementoXsdId,
                        nombre
                )
                .filter(atributo ->
                        EstadoRegistro.ACTIVO.equals(
                                atributo.getEstadoRegistro()
                        )
                );
    }
}