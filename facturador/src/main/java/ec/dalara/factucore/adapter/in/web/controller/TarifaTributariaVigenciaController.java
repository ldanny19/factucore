package ec.dalara.factucore.adapter.in.web.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ec.dalara.factucore.adapter.in.web.response.RespuestaRestFactory;
import ec.dalara.factucore.application.contract.request.AdministracionRequest;
import ec.dalara.factucore.application.contract.request.TarifaTributariaVigenciaRequest;
import ec.dalara.factucore.application.contract.response.AdministracionResponse;
import ec.dalara.factucore.application.contract.response.TarifaTributariaVigenciaResponse;
import ec.dalara.factucore.application.service.TarifaTributariaVigenciaService;
import ec.dalara.factucore.infrastructure.persistence.entity.CatalogoItem;
import ec.dalara.factucore.infrastructure.persistence.entity.TarifaTributariaVigencia;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Tarifas tributarias", description = "Administra las tarifas tributarias y consulta su vigencia temporal.")
@RestController
@RequestMapping("/api/v1/tarifas-tributarias")
@RequiredArgsConstructor
public class TarifaTributariaVigenciaController {

	private final TarifaTributariaVigenciaService tarifaService;
	private final RespuestaRestFactory respuestas;

	@Operation(summary = "Crear tarifa tributaria", description = "Crear tarifa tributaria. Administra las tarifas tributarias y consulta su vigencia temporal.")

	@ApiResponses(value = {

			@ApiResponse(responseCode = "200", description = "Operación ejecutada correctamente"),

			@ApiResponse(responseCode = "400", description = "Solicitud inválida o datos de entrada no válidos"),

			@ApiResponse(responseCode = "401", description = "No autenticado: se requiere un token JWT válido"),

			@ApiResponse(responseCode = "403", description = "No autorizado para ejecutar la operación"),

			@ApiResponse(responseCode = "409", description = "La operación entra en conflicto con el estado actual del recurso"),

			@ApiResponse(responseCode = "500", description = "Error interno al procesar la solicitud")

	})

	@PostMapping
	public ResponseEntity<AdministracionResponse<TarifaTributariaVigenciaResponse>> crear(
			@Valid @RequestBody AdministracionRequest<TarifaTributariaVigenciaRequest> request) {
		respuestas.validarFechaInicio(request.getFechaInicio());
		TarifaTributariaVigencia tarifa = toEntity(null, request.getDatos());
		tarifa.setEstadoRegistro("A");
		tarifa.setUsuarioCreacion(request.getCanal());
		tarifa.setFechaCreacion(LocalDateTime.now());
		tarifa = tarifaService.guardar(tarifa);
		return ResponseEntity
				.ok(respuestas.exito(request.getIdTransaccion(), request.getFechaInicio(), toResponse(tarifa)));
	}

	@Operation(summary = "Actualizar tarifa tributaria", description = "Actualizar tarifa tributaria. Administra las tarifas tributarias y consulta su vigencia temporal.")

	@ApiResponses(value = {

			@ApiResponse(responseCode = "200", description = "Operación ejecutada correctamente"),

			@ApiResponse(responseCode = "400", description = "Solicitud inválida o datos de entrada no válidos"),

			@ApiResponse(responseCode = "401", description = "No autenticado: se requiere un token JWT válido"),

			@ApiResponse(responseCode = "403", description = "No autorizado para ejecutar la operación"),

			@ApiResponse(responseCode = "404", description = "No se encontró el recurso solicitado"),

			@ApiResponse(responseCode = "409", description = "La operación entra en conflicto con el estado actual del recurso"),

			@ApiResponse(responseCode = "500", description = "Error interno al procesar la solicitud")

	})

	@PutMapping("/{id}")
	public ResponseEntity<AdministracionResponse<TarifaTributariaVigenciaResponse>> actualizar(@PathVariable Long id,
			@Valid @RequestBody AdministracionRequest<TarifaTributariaVigenciaRequest> request) {
		respuestas.validarFechaInicio(request.getFechaInicio());
		TarifaTributariaVigencia actual = tarifaService.obtenerPorId(id)
				.orElseThrow(() -> new ec.dalara.factucore.application.ApplicationException(
						ec.dalara.factucore.domain.shared.MessageCodes.REGISTRO_NO_ENCONTRADO, id));
		TarifaTributariaVigencia cambios = toEntity(id, request.getDatos());
		cambios.setEstadoRegistro(actual.getEstadoRegistro());
		cambios.setUsuarioCreacion(actual.getUsuarioCreacion());
		cambios.setFechaCreacion(actual.getFechaCreacion());
		cambios.setUsuarioModificacion(request.getCanal());
		cambios.setFechaModificacion(LocalDateTime.now());
		cambios.setObservacion(request.getDatos().getObservacion());
		return ResponseEntity.ok(respuestas.exito(request.getIdTransaccion(), request.getFechaInicio(),
				toResponse(tarifaService.guardar(cambios))));
	}

	@Operation(summary = "Consultar tarifa tributaria", description = "Consultar tarifa tributaria. Administra las tarifas tributarias y consulta su vigencia temporal.")

	@ApiResponses(value = {

			@ApiResponse(responseCode = "200", description = "Operación ejecutada correctamente"),

			@ApiResponse(responseCode = "400", description = "Solicitud inválida o datos de entrada no válidos"),

			@ApiResponse(responseCode = "401", description = "No autenticado: se requiere un token JWT válido"),

			@ApiResponse(responseCode = "403", description = "No autorizado para ejecutar la operación"),

			@ApiResponse(responseCode = "404", description = "No se encontró el recurso solicitado"),

			@ApiResponse(responseCode = "500", description = "Error interno al procesar la solicitud")

	})

	@GetMapping("/{id}")
	public ResponseEntity<AdministracionResponse<TarifaTributariaVigenciaResponse>> obtener(@PathVariable Long id) {
		TarifaTributariaVigencia tarifa = tarifaService.obtenerPorId(id)
				.orElseThrow(() -> new ec.dalara.factucore.application.ApplicationException(
						ec.dalara.factucore.domain.shared.MessageCodes.REGISTRO_NO_ENCONTRADO, id));
		return ResponseEntity.ok(respuestas.exitoConsulta(toResponse(tarifa)));
	}

	@Operation(summary = "Listar tarifa tributaria", description = "Listar tarifa tributaria. Administra las tarifas tributarias y consulta su vigencia temporal.")

	@ApiResponses(value = {

			@ApiResponse(responseCode = "200", description = "Operación ejecutada correctamente"),

			@ApiResponse(responseCode = "400", description = "Solicitud inválida o datos de entrada no válidos"),

			@ApiResponse(responseCode = "401", description = "No autenticado: se requiere un token JWT válido"),

			@ApiResponse(responseCode = "403", description = "No autorizado para ejecutar la operación"),

			@ApiResponse(responseCode = "500", description = "Error interno al procesar la solicitud")

	})

	@GetMapping
	public ResponseEntity<AdministracionResponse<List<TarifaTributariaVigenciaResponse>>> listar(
			@RequestParam String codigoImpuesto, @RequestParam(required = false) String codigoPorcentaje) {
		List<TarifaTributariaVigenciaResponse> tarifas = tarifaService
				.listarPorImpuestoYPorcentaje(codigoImpuesto, codigoPorcentaje).stream().map(this::toResponse).toList();
		return ResponseEntity.ok(respuestas.exitoConsulta(tarifas));
	}

	@Operation(summary = "Consultar tarifa tributaria vigente", description = "Consultar tarifa tributaria vigente. Administra las tarifas tributarias y consulta su vigencia temporal.")

	@ApiResponses(value = {

			@ApiResponse(responseCode = "200", description = "Operación ejecutada correctamente"),

			@ApiResponse(responseCode = "400", description = "Solicitud inválida o datos de entrada no válidos"),

			@ApiResponse(responseCode = "401", description = "No autenticado: se requiere un token JWT válido"),

			@ApiResponse(responseCode = "403", description = "No autorizado para ejecutar la operación"),

			@ApiResponse(responseCode = "404", description = "No se encontró el recurso solicitado"),

			@ApiResponse(responseCode = "500", description = "Error interno al procesar la solicitud")

	})

	@GetMapping("/vigente")
	public ResponseEntity<AdministracionResponse<TarifaTributariaVigenciaResponse>> obtenerVigente(
			@RequestParam String codigoImpuesto, @RequestParam String codigoPorcentaje,
			@RequestParam java.time.LocalDate fecha) {
		TarifaTributariaVigenciaResponse tarifa = tarifaService.obtenerVigente(codigoImpuesto, codigoPorcentaje, fecha)
				.map(this::toResponse).orElse(null);
		return ResponseEntity.ok(respuestas.exitoConsulta(tarifa));
	}

	@Operation(summary = "Inactivar tarifa tributaria", description = "Inactivar tarifa tributaria. Administra las tarifas tributarias y consulta su vigencia temporal.")

	@ApiResponses(value = {

			@ApiResponse(responseCode = "200", description = "Operación ejecutada correctamente"),

			@ApiResponse(responseCode = "400", description = "Solicitud inválida o datos de entrada no válidos"),

			@ApiResponse(responseCode = "401", description = "No autenticado: se requiere un token JWT válido"),

			@ApiResponse(responseCode = "403", description = "No autorizado para ejecutar la operación"),

			@ApiResponse(responseCode = "404", description = "No se encontró el recurso solicitado"),

			@ApiResponse(responseCode = "409", description = "La operación entra en conflicto con el estado actual del recurso"),

			@ApiResponse(responseCode = "500", description = "Error interno al procesar la solicitud")

	})

	@PatchMapping("/{id}/inactivar")
	public ResponseEntity<AdministracionResponse<Void>> inactivar(@PathVariable Long id) {
		tarifaService.inactivar(id);
		return ResponseEntity.ok(respuestas.exitoConsulta(null));
	}

	@Operation(summary = "Reactivar tarifa tributaria", description = "Reactivar tarifa tributaria. Administra las tarifas tributarias y consulta su vigencia temporal.")

	@ApiResponses(value = {

			@ApiResponse(responseCode = "200", description = "Operación ejecutada correctamente"),

			@ApiResponse(responseCode = "400", description = "Solicitud inválida o datos de entrada no válidos"),

			@ApiResponse(responseCode = "401", description = "No autenticado: se requiere un token JWT válido"),

			@ApiResponse(responseCode = "403", description = "No autorizado para ejecutar la operación"),

			@ApiResponse(responseCode = "404", description = "No se encontró el recurso solicitado"),

			@ApiResponse(responseCode = "409", description = "La operación entra en conflicto con el estado actual del recurso"),

			@ApiResponse(responseCode = "500", description = "Error interno al procesar la solicitud")

	})

	@PatchMapping("/{id}/reactivar")
	public ResponseEntity<AdministracionResponse<Void>> reactivar(@PathVariable Long id) {
		tarifaService.reactivar(id);
		return ResponseEntity.ok(respuestas.exitoConsulta(null));
	}

	@Operation(summary = "Eliminar tarifa tributaria", description = "Eliminar tarifa tributaria. Administra las tarifas tributarias y consulta su vigencia temporal.")

	@ApiResponses(value = {

			@ApiResponse(responseCode = "200", description = "Operación ejecutada correctamente"),

			@ApiResponse(responseCode = "400", description = "Solicitud inválida o datos de entrada no válidos"),

			@ApiResponse(responseCode = "401", description = "No autenticado: se requiere un token JWT válido"),

			@ApiResponse(responseCode = "403", description = "No autorizado para ejecutar la operación"),

			@ApiResponse(responseCode = "404", description = "No se encontró el recurso solicitado"),

			@ApiResponse(responseCode = "409", description = "La operación entra en conflicto con el estado actual del recurso"),

			@ApiResponse(responseCode = "500", description = "Error interno al procesar la solicitud")

	})

	@DeleteMapping("/{id}")
	public ResponseEntity<AdministracionResponse<Void>> eliminar(@PathVariable Long id) {
		tarifaService.eliminar(id);
		return ResponseEntity.ok(respuestas.exitoConsulta(null));
	}

	private TarifaTributariaVigencia toEntity(Long id, TarifaTributariaVigenciaRequest request) {
		return TarifaTributariaVigencia.builder().id(id)
				.catalogoItem(CatalogoItem.builder().id(request.getIdCatalogoItem()).build())
				.codigoImpuesto(request.getCodigoImpuesto()).codigoPorcentaje(request.getCodigoPorcentaje())
				.tarifa(request.getTarifa()).fechaVigenciaDesde(request.getFechaVigenciaDesde())
				.fechaVigenciaHasta(request.getFechaVigenciaHasta()).observacion(request.getObservacion()).build();
	}

	private TarifaTributariaVigenciaResponse toResponse(TarifaTributariaVigencia tarifa) {
		return TarifaTributariaVigenciaResponse.builder().id(tarifa.getId())
				.idCatalogoItem(tarifa.getCatalogoItem().getId()).codigoImpuesto(tarifa.getCodigoImpuesto())
				.codigoPorcentaje(tarifa.getCodigoPorcentaje()).tarifa(tarifa.getTarifa())
				.fechaVigenciaDesde(tarifa.getFechaVigenciaDesde()).fechaVigenciaHasta(tarifa.getFechaVigenciaHasta())
				.estadoRegistro(tarifa.getEstadoRegistro()).usuarioCreacion(tarifa.getUsuarioCreacion())
				.usuarioModificacion(tarifa.getUsuarioModificacion()).fechaCreacion(tarifa.getFechaCreacion())
				.fechaModificacion(tarifa.getFechaModificacion()).observacion(tarifa.getObservacion()).build();
	}
}
