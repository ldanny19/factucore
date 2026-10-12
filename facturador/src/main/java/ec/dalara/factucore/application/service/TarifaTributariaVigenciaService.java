package ec.dalara.factucore.application.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ec.dalara.factucore.application.ApplicationException;
import ec.dalara.factucore.domain.shared.EstadoRegistro;
import ec.dalara.factucore.domain.shared.MessageCodes;
import ec.dalara.factucore.infrastructure.persistence.entity.TarifaTributariaVigencia;
import ec.dalara.factucore.infrastructure.persistence.repository.BaseRepository;
import ec.dalara.factucore.infrastructure.persistence.repository.TarifaTributariaVigenciaRepository;
import ec.dalara.factucore.infrastructure.persistence.repository.CatalogoItemRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TarifaTributariaVigenciaService extends BaseService<TarifaTributariaVigencia> {

    private static final LocalDate FECHA_MAXIMA = LocalDate.of(9999, 12, 31);

    private final TarifaTributariaVigenciaRepository tarifaRepository;
    private final CatalogoItemRepository catalogoItemRepository;

    @Override
    protected BaseRepository<TarifaTributariaVigencia, Long> getRepository() {
        return tarifaRepository;
    }

    @Override
    @Transactional
    public TarifaTributariaVigencia guardar(TarifaTributariaVigencia tarifa) {
        validar(tarifa);
        tarifa.setCatalogoItem(catalogoItemRepository.findByIdAndEstadoRegistro(
                tarifa.getCatalogoItem().getId(), EstadoRegistro.ACTIVO)
                .orElseThrow(() -> new ApplicationException(
                        MessageCodes.TARIFA_TRIBUTARIA_VIGENCIA_CATALOGO_ITEM_REQUERIDO)));
        Long id = tarifa.getId() == null ? -1L : tarifa.getId();
        if (tarifaRepository.existeSolapamiento(tarifa.getCodigoImpuesto(), tarifa.getCodigoPorcentaje(),
                tarifa.getFechaVigenciaDesde(), tarifa.getFechaVigenciaHasta(), id, FECHA_MAXIMA)) {
            throw new ApplicationException(MessageCodes.TARIFA_TRIBUTARIA_VIGENCIA_SOLAPADA,
                    tarifa.getCodigoImpuesto(), tarifa.getCodigoPorcentaje());
        }
        return super.guardar(tarifa);
    }

    public List<TarifaTributariaVigencia> listarPorImpuestoYPorcentaje(String codigoImpuesto,
            String codigoPorcentaje) {
        return tarifaRepository
                .findByCodigoImpuestoAndCodigoPorcentajeOrderByFechaVigenciaDesdeAsc(codigoImpuesto, codigoPorcentaje)
                .stream().filter(t -> !EstadoRegistro.ELIMINADO.equals(t.getEstadoRegistro())).toList();
    }

    @Transactional(readOnly = true)
    public List<TarifaTributariaVigencia> listarVigentesPorImpuesto(String codigoImpuesto, LocalDate fecha) {
        if (codigoImpuesto == null || codigoImpuesto.isBlank() || fecha == null) {
            return List.of();
        }
        return tarifaRepository.buscarVigentesPorImpuesto(codigoImpuesto, EstadoRegistro.ACTIVO, fecha);
    }

    @Transactional(readOnly = true)
    public Optional<TarifaTributariaVigencia> obtenerVigente(String codigoImpuesto, String codigoPorcentaje,
            LocalDate fecha) {
        if (codigoImpuesto == null || codigoImpuesto.isBlank() || codigoPorcentaje == null
                || codigoPorcentaje.isBlank() || fecha == null) {
            return Optional.empty();
        }
        return tarifaRepository.buscarVigente(codigoImpuesto, codigoPorcentaje, EstadoRegistro.ACTIVO, fecha);
    }

    private void validar(TarifaTributariaVigencia tarifa) {
        if (tarifa == null) {
            throw new ApplicationException(MessageCodes.TARIFA_TRIBUTARIA_VIGENCIA_REQUERIDA);
        }
        if (tarifa.getCatalogoItem() == null || tarifa.getCatalogoItem().getId() == null) {
            throw new ApplicationException(MessageCodes.TARIFA_TRIBUTARIA_VIGENCIA_CATALOGO_ITEM_REQUERIDO);
        }
        if (tarifa.getCodigoImpuesto() == null || tarifa.getCodigoImpuesto().isBlank()
                || tarifa.getCodigoPorcentaje() == null || tarifa.getCodigoPorcentaje().isBlank()
                || tarifa.getTarifa() == null || tarifa.getFechaVigenciaDesde() == null) {
            throw new ApplicationException(MessageCodes.TARIFA_TRIBUTARIA_VIGENCIA_DATOS_REQUERIDOS);
        }
        if (tarifa.getTarifa().signum() < 0) {
            throw new ApplicationException(MessageCodes.TARIFA_TRIBUTARIA_VIGENCIA_TARIFA_INVALIDA);
        }
        if (tarifa.getFechaVigenciaHasta() != null
                && tarifa.getFechaVigenciaHasta().isBefore(tarifa.getFechaVigenciaDesde())) {
            throw new ApplicationException(MessageCodes.TARIFA_TRIBUTARIA_VIGENCIA_RANGO_FECHAS_INVALIDO);
        }
    }
}
