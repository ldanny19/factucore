package ec.dalara.factucore.application.service;

import ec.dalara.factucore.domain.shared.EstadoRegistro;
import ec.dalara.factucore.infrastructure.persistence.entity.EnumeracionXsd;
import ec.dalara.factucore.infrastructure.persistence.repository.BaseRepository;
import ec.dalara.factucore.infrastructure.persistence.repository.EnumeracionXsdRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EnumeracionXsdService
        extends BaseService<EnumeracionXsd> {

    private final EnumeracionXsdRepository enumeracionXsdRepository;

    @Override
    protected BaseRepository<EnumeracionXsd, Long> getRepository() {
        return enumeracionXsdRepository;
    }

    public List<EnumeracionXsd> listarPorElementoXsd(Long elementoXsdId) {
        return enumeracionXsdRepository
                .findByElementoXsdId(elementoXsdId)
                .stream()
                .filter(enumeracion ->
                        !EstadoRegistro.ELIMINADO.equals(
                                enumeracion.getEstadoRegistro()
                        )
                )
                .toList();
    }

    public Optional<EnumeracionXsd> obtenerPorElementoXsdYValor(
            Long elementoXsdId,
            String valor
    ) {
        return enumeracionXsdRepository
                .findByElementoXsdIdAndValor(
                        elementoXsdId,
                        valor
                )
                .filter(enumeracion ->
                        EstadoRegistro.ACTIVO.equals(
                                enumeracion.getEstadoRegistro()
                        )
                );
    }

    public boolean existePorElementoXsdYValor(
            Long elementoXsdId,
            String valor
    ) {
        return enumeracionXsdRepository
                .findByElementoXsdIdAndValor(
                        elementoXsdId,
                        valor
                )
                .filter(enumeracion ->
                        !EstadoRegistro.ELIMINADO.equals(
                                enumeracion.getEstadoRegistro()
                        )
                )
                .isPresent();
    }
}