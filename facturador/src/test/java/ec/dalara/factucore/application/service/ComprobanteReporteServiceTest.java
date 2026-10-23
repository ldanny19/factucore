package ec.dalara.factucore.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import ec.dalara.factucore.application.contract.request.ComprobanteReporteFiltro;
import ec.dalara.factucore.infrastructure.persistence.entity.Comprobante;
import ec.dalara.factucore.infrastructure.persistence.entity.DocumentoXsd;
import ec.dalara.factucore.infrastructure.persistence.entity.Empresa;
import ec.dalara.factucore.infrastructure.persistence.entity.Establecimiento;
import ec.dalara.factucore.infrastructure.persistence.entity.PuntoEmision;
import ec.dalara.factucore.infrastructure.persistence.repository.ComprobanteAuditoriaRepository;
import ec.dalara.factucore.infrastructure.persistence.repository.ComprobanteReporteDiaProjection;
import ec.dalara.factucore.infrastructure.persistence.repository.ComprobanteRepository;
import ec.dalara.factucore.infrastructure.persistence.repository.EmpresaRepository;

@ExtendWith(MockitoExtension.class)
class ComprobanteReporteServiceTest {

	@Mock
	private ComprobanteRepository comprobanteRepository;

	@Mock
	private ComprobanteAuditoriaRepository auditoriaRepository;

	@Mock
	private EmpresaRepository empresaRepository;

	private ComprobanteReporteService service;

	@BeforeEach
	void setUp() {
		service = new ComprobanteReporteService(comprobanteRepository, auditoriaRepository, empresaRepository);
	}

	@Test
	void buscarDebeRequerirEmpresa() {
		var exception = assertThrows(ResponseStatusException.class, () -> service.buscar(new ComprobanteReporteFiltro(), 0, 25));

		assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
		verify(comprobanteRepository, never()).findAll(any(Specification.class), any(Pageable.class));
	}

	@Test
	void buscarDebeRechazarRangoDeFechasInvertido() {
		var filtro = new ComprobanteReporteFiltro();
		filtro.setIdEmpresa(1L);
		filtro.setFechaDesde(LocalDate.of(2026, 2, 1));
		filtro.setFechaHasta(LocalDate.of(2026, 1, 1));

		var exception = assertThrows(ResponseStatusException.class, () -> service.resumir(filtro));

		assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
		verify(comprobanteRepository, never()).count(any(Specification.class));
	}

	@Test
	void resumirDebeDevolverConteosDiariosEnGruposExcluyentes() {
		var filtro = new ComprobanteReporteFiltro();
		filtro.setIdEmpresa(1L);
		filtro.setFechaDesde(LocalDate.of(2026, 10, 22));
		filtro.setFechaHasta(LocalDate.of(2026, 10, 23));
		when(comprobanteRepository.agruparPorDia(any(Specification.class))).thenReturn(List.of(
				new ComprobanteReporteDiaProjection(LocalDate.of(2026, 10, 22), 10, 4, 2, 1, 2),
				new ComprobanteReporteDiaProjection(LocalDate.of(2026, 10, 23), 8, 3, 1, 2, 1)));

		var resumen = service.resumir(filtro);

		assertEquals(18, resumen.getTotal());
		assertEquals(7, resumen.getAutorizados());
		assertEquals(3, resumen.getPendientes());
		assertEquals(3, resumen.getNoAutorizados());
		assertEquals(3, resumen.getConError());
		assertEquals(2, resumen.getOtros());
		assertEquals(2, resumen.getPorDia().size());
		assertEquals(LocalDate.of(2026, 10, 22), resumen.getPorDia().get(0).getFecha());
		assertEquals(1, resumen.getPorDia().get(0).getOtros());
		verify(comprobanteRepository).agruparPorDia(any(Specification.class));
	}

	@Test
	void exportarCsvDebeNeutralizarFormulasYMapearNumeroComprobante() {
		var comprobante = comprobanteDePrueba();
		var filtro = new ComprobanteReporteFiltro();
		filtro.setIdEmpresa(1L);
		when(comprobanteRepository.count(any(Specification.class))).thenReturn(1L);
		when(comprobanteRepository.findAll(any(Specification.class), any(Pageable.class)))
				.thenReturn(new PageImpl<>(List.of(comprobante)));

		String csv = new String(service.exportarCsv(filtro), java.nio.charset.StandardCharsets.UTF_8);

		assertTrue(csv.contains("\"001-002-000000003\""));
		assertTrue(csv.contains("\"'=HYPERLINK(\"\"https://example.invalid\"\")\""));
		verify(comprobanteRepository).findAll(any(Specification.class), any(Pageable.class));
	}

	@Test
	void obtenerDetalleDebeOcultarComprobantesDeOtraEmpresa() {
		when(comprobanteRepository.findByIdAndEmpresaIdAndEstadoRegistro(anyLong(), anyLong(), any()))
				.thenReturn(java.util.Optional.empty());

		var exception = assertThrows(ResponseStatusException.class, () -> service.obtenerDetalle(2L, 5L));

		assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
		verify(auditoriaRepository, never()).findByComprobanteIdAndEstadoRegistroOrderByFechaInicioDesc(anyLong(),
				any());
	}

	private Comprobante comprobanteDePrueba() {
		return Comprobante.builder().id(4L).empresa(Empresa.builder().id(1L).build())
				.establecimiento(Establecimiento.builder().id(2L).codigo("001").build())
				.puntoEmision(PuntoEmision.builder().id(3L).codigo("002").build())
				.documentoXsd(DocumentoXsd.builder().id(6L).nombre("Factura").build())
				.fechaEmision(LocalDate.of(2026, 10, 23)).codigoDocumento("01").secuencial("000000003")
				.claveAcceso("123456789").razonSocialEmisor("Emisor de prueba").rucEmisor("1790012345001")
				.razonSocialReceptor("=HYPERLINK(\"https://example.invalid\")").estadoProceso("AUTORIZACION_SRI")
				.estadoSri("AUTORIZADO").estadoRegistro("ACTIVO").build();
	}
}
