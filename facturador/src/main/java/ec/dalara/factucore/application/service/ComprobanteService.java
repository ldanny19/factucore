package ec.dalara.factucore.application.service;

import ec.dalara.factucore.domain.shared.EstadoRegistro;
import ec.dalara.factucore.infrastructure.persistence.entity.Comprobante;
import ec.dalara.factucore.infrastructure.persistence.repository.BaseRepository;
import ec.dalara.factucore.infrastructure.persistence.repository.ComprobanteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ComprobanteService extends BaseService<Comprobante> {

    private final ComprobanteRepository comprobanteRepository;

    @Override
    protected BaseRepository<Comprobante, Long> getRepository() {
        return comprobanteRepository;
    }

    public Optional<Comprobante> obtenerPorClaveAcceso(
            String claveAcceso
    ) {
        return comprobanteRepository
                .findByClaveAcceso(claveAcceso)
                .filter(comprobante ->
                        EstadoRegistro.ACTIVO.equals(
                                comprobante.getEstadoRegistro()
                        )
                );
    }

    public boolean existePorClaveAcceso(
            String claveAcceso
    ) {
        return comprobanteRepository
                .findByClaveAcceso(claveAcceso)
                .filter(comprobante ->
                        !EstadoRegistro.ELIMINADO.equals(
                                comprobante.getEstadoRegistro()
                        )
                )
                .isPresent();
    }

    public Optional<Comprobante> obtenerPorEmpresaEstablecimientoPuntoEmisionDocumentoSecuencial(
            Long empresaId,
            Long establecimientoId,
            Long puntoEmisionId,
            String codigoDocumento,
            String secuencial
    ) {
        return comprobanteRepository
                .findByEmpresaIdAndEstablecimientoIdAndPuntoEmisionIdAndCodigoDocumentoAndSecuencial(
                        empresaId,
                        establecimientoId,
                        puntoEmisionId,
                        codigoDocumento,
                        secuencial
                )
                .filter(comprobante ->
                        EstadoRegistro.ACTIVO.equals(
                                comprobante.getEstadoRegistro()
                        )
                );
    }
}