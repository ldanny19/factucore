package ec.dalara.factucore.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import ec.dalara.factucore.application.ApplicationException;
import ec.dalara.factucore.domain.shared.EstadoRegistro;
import ec.dalara.factucore.infrastructure.persistence.entity.TarifaTributariaVigencia;
import ec.dalara.factucore.infrastructure.persistence.repository.CatalogoItemRepository;
import ec.dalara.factucore.infrastructure.persistence.repository.TarifaTributariaVigenciaRepository;

class TarifaTributariaVigenciaServiceTest {

	private final TarifaTributariaVigenciaRepository tarifaRepository = mock(TarifaTributariaVigenciaRepository.class);
	private final CatalogoItemRepository catalogoItemRepository = mock(CatalogoItemRepository.class);
	private final TarifaTributariaVigenciaService service = new TarifaTributariaVigenciaService(
			tarifaRepository, catalogoItemRepository);

	@Test
	void listarSinFiltrosRetornaTodasLasTarifasOrdenadas() {
		var tarifaIva = tarifa("2", "0", LocalDate.of(2026, 1, 1));
		var tarifaRenta = tarifa("1", "2", LocalDate.of(2026, 1, 1));
		var tarifaAnterior = tarifa("1", "2", LocalDate.of(2025, 1, 1));
		when(tarifaRepository.findByEstadoRegistroNot(EstadoRegistro.ELIMINADO))
				.thenReturn(List.of(tarifaIva, tarifaRenta, tarifaAnterior));

		List<TarifaTributariaVigencia> resultado = service.listarPorImpuestoYPorcentaje(null, null);

		assertEquals(List.of(tarifaAnterior, tarifaRenta, tarifaIva), resultado);
		verify(tarifaRepository).findByEstadoRegistroNot(EstadoRegistro.ELIMINADO);
	}

	@Test
	void listarPorPorcentajeSinImpuestoEsRechazado() {
		assertThrows(ApplicationException.class, () -> service.listarPorImpuestoYPorcentaje(null, "2"));
	}

	private TarifaTributariaVigencia tarifa(String impuesto, String porcentaje, LocalDate desde) {
		return TarifaTributariaVigencia.builder()
				.codigoImpuesto(impuesto)
				.codigoPorcentaje(porcentaje)
				.fechaVigenciaDesde(desde)
				.estadoRegistro(EstadoRegistro.ACTIVO)
				.build();
	}
}
