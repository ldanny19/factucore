package ec.dalara.factucore.adapter.in.web.controller;

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
import ec.dalara.factucore.application.contract.request.PuntoEmisionRequest;
import ec.dalara.factucore.application.contract.response.AdministracionResponse;
import ec.dalara.factucore.application.contract.response.PuntoEmisionResponse;
import ec.dalara.factucore.application.port.in.AdministracionPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Puntos de emisión", description = "Administra los puntos de emisión de cada establecimiento.")
@RestController
@RequestMapping("/api/v1/puntos-emision")
@RequiredArgsConstructor
public class PuntoEmisionController {
	private final AdministracionPort port;
	private final RespuestaRestFactory respuestas;

	@Operation(summary = "Crear punto de emisión", description = "Crear punto de emisión. Administra los puntos de emisión de cada establecimiento.")

	@ApiResponses(value = {

			@ApiResponse(responseCode = "200", description = "Operación ejecutada correctamente"),

			@ApiResponse(responseCode = "400", description = "Solicitud inválida o datos de entrada no válidos"),

			@ApiResponse(responseCode = "401", description = "No autenticado: se requiere un token JWT válido"),

			@ApiResponse(responseCode = "403", description = "No autorizado para ejecutar la operación"),

			@ApiResponse(responseCode = "409", description = "La operación entra en conflicto con el estado actual del recurso"),

			@ApiResponse(responseCode = "500", description = "Error interno al procesar la solicitud")

	})

	@PostMapping
	public ResponseEntity<AdministracionResponse<PuntoEmisionResponse>> crear(
			@Valid @RequestBody AdministracionRequest<PuntoEmisionRequest> request) {
		respuestas.validarFechaInicio(request.getFechaInicio());
		return ResponseEntity.ok(respuestas.exito(request.getIdTransaccion(), request.getFechaInicio(),
				port.guardarPuntoEmision(null, request.getDatos())));
	}

	@Operation(summary = "Actualizar punto de emisión", description = "Actualizar punto de emisión. Administra los puntos de emisión de cada establecimiento.")

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
	public ResponseEntity<AdministracionResponse<PuntoEmisionResponse>> actualizar(@PathVariable("id") Long id,
			@Valid @RequestBody AdministracionRequest<PuntoEmisionRequest> request) {
		respuestas.validarFechaInicio(request.getFechaInicio());
		return ResponseEntity.ok(respuestas.exito(request.getIdTransaccion(), request.getFechaInicio(),
				port.guardarPuntoEmision(id, request.getDatos())));
	}

	@Operation(summary = "Consultar punto de emisión", description = "Consultar punto de emisión. Administra los puntos de emisión de cada establecimiento.")

	@ApiResponses(value = {

			@ApiResponse(responseCode = "200", description = "Operación ejecutada correctamente"),

			@ApiResponse(responseCode = "400", description = "Solicitud inválida o datos de entrada no válidos"),

			@ApiResponse(responseCode = "401", description = "No autenticado: se requiere un token JWT válido"),

			@ApiResponse(responseCode = "403", description = "No autorizado para ejecutar la operación"),

			@ApiResponse(responseCode = "404", description = "No se encontró el recurso solicitado"),

			@ApiResponse(responseCode = "500", description = "Error interno al procesar la solicitud")

	})

	@GetMapping("/{id}")
	public ResponseEntity<AdministracionResponse<PuntoEmisionResponse>> obtener(@PathVariable("id") Long id) {
		return ResponseEntity.ok(respuestas.exitoConsulta(port.obtenerPuntoEmision(id)));
	}

	@Operation(summary = "Listar punto de emisión", description = "Listar punto de emisión. Administra los puntos de emisión de cada establecimiento.")

	@ApiResponses(value = {

			@ApiResponse(responseCode = "200", description = "Operación ejecutada correctamente"),

			@ApiResponse(responseCode = "400", description = "Solicitud inválida o datos de entrada no válidos"),

			@ApiResponse(responseCode = "401", description = "No autenticado: se requiere un token JWT válido"),

			@ApiResponse(responseCode = "403", description = "No autorizado para ejecutar la operación"),

			@ApiResponse(responseCode = "500", description = "Error interno al procesar la solicitud")

	})

	@GetMapping
	public ResponseEntity<AdministracionResponse<List<PuntoEmisionResponse>>> listar(
			@RequestParam("idEstablecimiento") Long idEstablecimiento) {
		return ResponseEntity.ok(respuestas.exitoConsulta(port.listarPuntosEmision(idEstablecimiento)));
	}

	@Operation(summary = "Inactivar punto de emisión", description = "Inactivar punto de emisión. Administra los puntos de emisión de cada establecimiento.")

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
		port.inactivarPuntoEmision(id);
		return ResponseEntity.ok(respuestas.exitoConsulta(null));
	}

	@Operation(summary = "Reactivar punto de emisión", description = "Reactivar punto de emisión. Administra los puntos de emisión de cada establecimiento.")

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
		port.reactivarPuntoEmision(id);
		return ResponseEntity.ok(respuestas.exitoConsulta(null));
	}

	@Operation(summary = "Eliminar punto de emisión", description = "Eliminar punto de emisión. Administra los puntos de emisión de cada establecimiento.")

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
		port.eliminarPuntoEmision(id);
		return ResponseEntity.ok(respuestas.exitoConsulta(null));
	}
}
