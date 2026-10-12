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
import org.springframework.web.bind.annotation.RestController;

import ec.dalara.factucore.adapter.in.web.response.RespuestaRestFactory;
import ec.dalara.factucore.application.contract.request.AdministracionRequest;
import ec.dalara.factucore.application.contract.request.SecuencialRequest;
import ec.dalara.factucore.application.contract.response.AdministracionResponse;
import ec.dalara.factucore.application.contract.response.SecuencialResponse;
import ec.dalara.factucore.application.port.in.AdministracionPort;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Secuenciales", description = "Administra los secuenciales de emisión de comprobantes.")
@RestController
@RequestMapping("/api/v1/secuenciales")
@RequiredArgsConstructor
public class SecuencialController {
	private final AdministracionPort port;
	private final RespuestaRestFactory respuestas;

	@Operation(summary = "Crear secuencial", description = "Crear secuencial. Administra los secuenciales de emisión de comprobantes.")

	@ApiResponses(value = {

		@ApiResponse(responseCode = "200", description = "Operación ejecutada correctamente"),

		@ApiResponse(responseCode = "400", description = "Solicitud inválida o datos de entrada no válidos"),

		@ApiResponse(responseCode = "401", description = "No autenticado: se requiere un token JWT válido"),

		@ApiResponse(responseCode = "403", description = "No autorizado para ejecutar la operación"),

		@ApiResponse(responseCode = "409", description = "La operación entra en conflicto con el estado actual del recurso"),

		@ApiResponse(responseCode = "500", description = "Error interno al procesar la solicitud")

	})

	@PostMapping
	public ResponseEntity<AdministracionResponse<SecuencialResponse>> crear(
			@Valid @RequestBody AdministracionRequest<SecuencialRequest> request) {
		respuestas.validarFechaInicio(request.getFechaInicio());
		return ResponseEntity.ok(respuestas.exito(request.getIdTransaccion(), request.getFechaInicio(),
				port.guardarSecuencial(null, request.getDatos())));
	}

	@Operation(summary = "Actualizar secuencial", description = "Actualizar secuencial. Administra los secuenciales de emisión de comprobantes.")

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
	public ResponseEntity<AdministracionResponse<SecuencialResponse>> actualizar(@PathVariable("id") Long id,
			@Valid @RequestBody AdministracionRequest<SecuencialRequest> request) {
		respuestas.validarFechaInicio(request.getFechaInicio());
		return ResponseEntity.ok(respuestas.exito(request.getIdTransaccion(), request.getFechaInicio(),
				port.guardarSecuencial(id, request.getDatos())));
	}

	@Operation(summary = "Consultar secuencial", description = "Consultar secuencial. Administra los secuenciales de emisión de comprobantes.")

	@ApiResponses(value = {

		@ApiResponse(responseCode = "200", description = "Operación ejecutada correctamente"),

		@ApiResponse(responseCode = "400", description = "Solicitud inválida o datos de entrada no válidos"),

		@ApiResponse(responseCode = "401", description = "No autenticado: se requiere un token JWT válido"),

		@ApiResponse(responseCode = "403", description = "No autorizado para ejecutar la operación"),

		@ApiResponse(responseCode = "404", description = "No se encontró el recurso solicitado"),

		@ApiResponse(responseCode = "500", description = "Error interno al procesar la solicitud")

	})

	@GetMapping("/{id}")
	public ResponseEntity<AdministracionResponse<SecuencialResponse>> obtener(@PathVariable("id") Long id) {
		return ResponseEntity.ok(respuestas.exitoConsulta(port.obtenerSecuencial(id)));
	}

	@Operation(summary = "Listar secuencial", description = "Listar secuencial. Administra los secuenciales de emisión de comprobantes.")

	@ApiResponses(value = {

		@ApiResponse(responseCode = "200", description = "Operación ejecutada correctamente"),

		@ApiResponse(responseCode = "400", description = "Solicitud inválida o datos de entrada no válidos"),

		@ApiResponse(responseCode = "401", description = "No autenticado: se requiere un token JWT válido"),

		@ApiResponse(responseCode = "403", description = "No autorizado para ejecutar la operación"),

		@ApiResponse(responseCode = "500", description = "Error interno al procesar la solicitud")

	})

	@GetMapping
	public ResponseEntity<AdministracionResponse<List<SecuencialResponse>>> listar() {
		return ResponseEntity.ok(respuestas.exitoConsulta(port.listarSecuenciales()));
	}

	@Operation(summary = "Inactivar secuencial", description = "Inactivar secuencial. Administra los secuenciales de emisión de comprobantes.")

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
		port.inactivarSecuencial(id);
		return ResponseEntity.ok(respuestas.exitoConsulta(null));
	}

	@Operation(summary = "Reactivar secuencial", description = "Reactivar secuencial. Administra los secuenciales de emisión de comprobantes.")

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
		port.reactivarSecuencial(id);
		return ResponseEntity.ok(respuestas.exitoConsulta(null));
	}

	@Operation(summary = "Eliminar secuencial", description = "Eliminar secuencial. Administra los secuenciales de emisión de comprobantes.")

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
		port.eliminarSecuencial(id);
		return ResponseEntity.ok(respuestas.exitoConsulta(null));
	}
}
