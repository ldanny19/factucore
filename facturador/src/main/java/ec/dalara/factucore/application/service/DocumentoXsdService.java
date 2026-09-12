package ec.dalara.factucore.application.service;

import ec.dalara.factucore.domain.shared.EstadoRegistro;
import ec.dalara.factucore.infrastructure.persistence.entity.DocumentoXsd;
import ec.dalara.factucore.infrastructure.persistence.repository.BaseRepository;
import ec.dalara.factucore.infrastructure.persistence.repository.DocumentoXsdRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DocumentoXsdService extends BaseService<DocumentoXsd> {

    private final DocumentoXsdRepository documentoXsdRepository;

    @Override
    protected BaseRepository<DocumentoXsd, Long> getRepository() {
        return documentoXsdRepository;
    }

    public Optional<DocumentoXsd> obtenerPorCodigo(String codigo) {
        return documentoXsdRepository.findByCodigo(codigo)
                .filter(documento ->
                        EstadoRegistro.ACTIVO.equals(
                                documento.getEstadoRegistro()
                        )
                );
    }

    public boolean existePorCodigo(String codigo) {
        return documentoXsdRepository.findByCodigo(codigo)
                .filter(documento ->
                        !EstadoRegistro.ELIMINADO.equals(
                                documento.getEstadoRegistro()
                        )
                )
                .isPresent();
    }
}