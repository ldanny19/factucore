package ec.dalara.factucore.application.service;

import ec.dalara.factucore.domain.shared.EstadoRegistro;
import ec.dalara.factucore.infrastructure.persistence.entity.Secuencial;
import ec.dalara.factucore.infrastructure.persistence.repository.BaseRepository;
import ec.dalara.factucore.infrastructure.persistence.repository.SecuencialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SecuencialService extends BaseService<Secuencial> {

    private final SecuencialRepository secuencialRepository;

    @Override
    protected BaseRepository<Secuencial, Long> getRepository() {
        return secuencialRepository;
    }

    public Optional<Secuencial> obtenerPorPuntoEmisionYDocumento(
            Long puntoEmisionId,
            String codigoDocumento
    ) {
        return secuencialRepository
                .findByPuntoEmisionIdAndCodigoDocumento(
                        puntoEmisionId,
                        codigoDocumento
                )
                .filter(secuencial ->
                        EstadoRegistro.ACTIVO.equals(
                                secuencial.getEstadoRegistro()
                        )
                );
    }

    public boolean existePorPuntoEmisionYDocumento(
            Long puntoEmisionId,
            String codigoDocumento
    ) {
        return secuencialRepository
                .findByPuntoEmisionIdAndCodigoDocumento(
                        puntoEmisionId,
                        codigoDocumento
                )
                .filter(secuencial ->
                        !EstadoRegistro.ELIMINADO.equals(
                                secuencial.getEstadoRegistro()
                        )
                )
                .isPresent();
    }
}