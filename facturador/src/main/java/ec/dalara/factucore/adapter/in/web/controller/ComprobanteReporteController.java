package ec.dalara.factucore.adapter.in.web.controller;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ec.dalara.factucore.adapter.in.web.response.RespuestaRestFactory;
import ec.dalara.factucore.application.contract.request.ComprobanteReporteFiltro;
import ec.dalara.factucore.application.contract.response.AdministracionResponse;
import ec.dalara.factucore.application.contract.response.ComprobanteReporteDetalleResponse;
import ec.dalara.factucore.application.contract.response.ComprobanteReporteEmpresaResponse;
import ec.dalara.factucore.application.contract.response.ComprobanteReportePaginaResponse;
import ec.dalara.factucore.application.contract.response.ComprobanteReporteResumenResponse;
import ec.dalara.factucore.application.service.ComprobanteReporteService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequestMapping("/api/v1/comprobantes/reportes")
@RequiredArgsConstructor
public class ComprobanteReporteController {

	private final ComprobanteReporteService reporteService;
	private final RespuestaRestFactory respuestas;

	@GetMapping("/empresas")
	public ResponseEntity<AdministracionResponse<List<ComprobanteReporteEmpresaResponse>>> listarEmpresas() {
		return ResponseEntity.ok(respuestas.exitoConsulta(reporteService.listarEmpresas()));
	}

	@GetMapping
	public ResponseEntity<AdministracionResponse<ComprobanteReportePaginaResponse>> buscar(
			@Valid @ModelAttribute ComprobanteReporteFiltro filtro,
			@RequestParam(defaultValue = "0") @Min(0) int pagina,
			@RequestParam(defaultValue = "25") @Min(1) @Max(100) int tamano) {
		return ResponseEntity.ok(respuestas.exitoConsulta(reporteService.buscar(filtro, pagina, tamano)));
	}

	@GetMapping("/resumen")
	public ResponseEntity<AdministracionResponse<ComprobanteReporteResumenResponse>> resumir(
			@Valid @ModelAttribute ComprobanteReporteFiltro filtro) {
		return ResponseEntity.ok(respuestas.exitoConsulta(reporteService.resumir(filtro)));
	}

	@GetMapping("/{id}")
	public ResponseEntity<AdministracionResponse<ComprobanteReporteDetalleResponse>> obtenerDetalle(
			@Valid @ModelAttribute ComprobanteReporteFiltro filtro, @PathVariable Long id) {
		return ResponseEntity.ok(
				respuestas.exitoConsulta(reporteService.obtenerDetalle(filtro.getIdEmpresa(), id)));
	}

	@GetMapping("/exportacion.csv")
	public ResponseEntity<byte[]> exportarCsv(@Valid @ModelAttribute ComprobanteReporteFiltro filtro) {
		byte[] contenido = reporteService.exportarCsv(filtro);
		return ResponseEntity.ok().contentType(new MediaType("text", "csv"))
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"comprobantes.csv\"")
				.body(contenido);
	}
}
