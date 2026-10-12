package ec.dalara.factucore.adapter.in.web.controller;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

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
import ec.dalara.factucore.application.contract.request.EstablecimientoRequest;
import ec.dalara.factucore.application.contract.response.AdministracionResponse;
import ec.dalara.factucore.application.contract.response.EstablecimientoResponse;
import ec.dalara.factucore.application.port.in.AdministracionPort;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Establecimientos", description = "Administra los establecimientos asociados a las empresas.")
@RestController
@RequestMapping("/api/v1/establecimientos")
@RequiredArgsConstructor
public class EstablecimientoController {
	private final AdministracionPort port;
	private final RespuestaRestFactory respuestas;

	@Operation(summary = "Crear establecimiento", description = "Crear establecimiento. Administra los establecimientos asociados a las empresas.")

	@ApiResponses(value = {

		@ApiResponse(responseCode = "200", description = "Operación ejecutada correctamente"),

		@ApiResponse(responseCode = "400", description = "Solicitud inválida o datos de entrada no válidos"),

		@ApiResponse(responseCode = "401", description = "No autenticado: se requiere un token JWT válido"),

		@ApiResponse(responseCode = "403", description = "No autorizado para ejecutar la operación"),

		@ApiResponse(responseCode = "409", description = "La operación entra en conflicto con el estado actual del recurso"),

		@ApiResponse(responseCode = "500", description = "Error interno al procesar la solicitud")

	})

	@PostMapping
	public ResponseEntity<AdministracionResponse<EstablecimientoResponse>> crear(
			@Valid @RequestBody AdministracionRequest<EstablecimientoRequest> request) {
		respuestas.validarFechaInicio(request.getFechaInicio());
		return ResponseEntity.ok(respuestas.exito(request.getIdTransaccion(), request.getFechaInicio(),
				port.guardarEstablecimiento(null, request.getDatos())));
	}

	@Operation(summary = "Actualizar establecimiento", description = "Actualizar establecimiento. Administra los establecimientos asociados a las empresas.")

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
	public ResponseEntity<AdministracionResponse<EstablecimientoResponse>> actualizar(@PathVariable("id") Long id,
			@Valid @RequestBody AdministracionRequest<EstablecimientoRequest> request) {
		respuestas.validarFechaInicio(request.getFechaInicio());
		return ResponseEntity.ok(respuestas.exito(request.getIdTransaccion(), request.getFechaInicio(),
				port.guardarEstablecimiento(id, request.getDatos())));
	}

	@Operation(summary = "Consultar establecimiento", description = "Consultar establecimiento. Administra los establecimientos asociados a las empresas.")

	@ApiResponses(value = {

		@ApiResponse(responseCode = "200", description = "Operación ejecutada correctamente"),

		@ApiResponse(responseCode = "400", description = "Solicitud inválida o datos de entrada no válidos"),

		@ApiResponse(responseCode = "401", description = "No autenticado: se requiere un token JWT válido"),

		@ApiResponse(responseCode = "403", description = "No autorizado para ejecutar la operación"),

		@ApiResponse(responseCode = "404", description = "No se encontró el recurso solicitado"),

		@ApiResponse(responseCode = "500", description = "Error interno al procesar la solicitud")

	})

	@GetMapping("/{id}")
	public ResponseEntity<AdministracionResponse<EstablecimientoResponse>> obtener(@PathVariable("id") Long id) {
		return ResponseEntity.ok(respuestas.exitoConsulta(port.obtenerEstablecimiento(id)));
	}

	@Operation(summary = "Listar establecimiento", description = "Listar establecimiento. Administra los establecimientos asociados a las empresas.")

	@ApiResponses(value = {

		@ApiResponse(responseCode = "200", description = "Operación ejecutada correctamente"),

		@ApiResponse(responseCode = "400", description = "Solicitud inválida o datos de entrada no válidos"),

		@ApiResponse(responseCode = "401", description = "No autenticado: se requiere un token JWT válido"),

		@ApiResponse(responseCode = "403", description = "No autorizado para ejecutar la operación"),

		@ApiResponse(responseCode = "500", description = "Error interno al procesar la solicitud")

	})

	@GetMapping
	public ResponseEntity<AdministracionResponse<List<EstablecimientoResponse>>> listar(@RequestParam("idEmpresa") Long idEmpresa) {
		return ResponseEntity.ok(respuestas.exitoConsulta(port.listarEstablecimientos(idEmpresa)));
	}

	@Operation(summary = "Inactivar establecimiento", description = "Inactivar establecimiento. Administra los establecimientos asociados a las empresas.")

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
	public ResponseEntity<AdministracionResponse<Void>> inactivar(@PathVariable("id") Long id) {
		port.inactivarEstablecimiento(id);
		return ResponseEntity.ok(respuestas.exitoConsulta(null));
	}

	@Operation(summary = "Reactivar establecimiento", description = "Reactivar establecimiento. Administra los establecimientos asociados a las empresas.")

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
	public ResponseEntity<AdministracionResponse<Void>> reactivar(@PathVariable("id") Long id) {
		port.reactivarEstablecimiento(id);
		return ResponseEntity.ok(respuestas.exitoConsulta(null));
	}

	@Operation(summary = "Eliminar establecimiento", description = "Eliminar establecimiento. Administra los establecimientos asociados a las empresas.")

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
	public ResponseEntity<AdministracionResponse<Void>> eliminar(@PathVariable("id") Long id) {
		port.eliminarEstablecimiento(id);
		return ResponseEntity.ok(respuestas.exitoConsulta(null));
	}
}
