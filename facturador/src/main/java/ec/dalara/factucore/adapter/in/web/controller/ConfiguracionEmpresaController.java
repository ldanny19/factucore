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
import ec.dalara.factucore.application.contract.request.ConfiguracionEmpresaRequest;
import ec.dalara.factucore.application.contract.response.AdministracionResponse;
import ec.dalara.factucore.application.contract.response.ConfiguracionEmpresaResponse;
import ec.dalara.factucore.application.port.in.AdministracionPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Configuración de empresa", description = "Administra la configuración funcional de cada empresa.")
@RestController
@RequestMapping("/api/v1/configuraciones-empresa")
@RequiredArgsConstructor
public class ConfiguracionEmpresaController {
	private final AdministracionPort port;
	private final RespuestaRestFactory respuestas;

	@Operation(summary = "Crear configuración de empresa", description = "Crear configuración de empresa. Administra la configuración funcional de cada empresa.")

	@ApiResponses(value = {

			@ApiResponse(responseCode = "200", description = "Operación ejecutada correctamente"),

			@ApiResponse(responseCode = "400", description = "Solicitud inválida o datos de entrada no válidos"),

			@ApiResponse(responseCode = "401", description = "No autenticado: se requiere un token JWT válido"),

			@ApiResponse(responseCode = "403", description = "No autorizado para ejecutar la operación"),

			@ApiResponse(responseCode = "409", description = "La operación entra en conflicto con el estado actual del recurso"),

			@ApiResponse(responseCode = "500", description = "Error interno al procesar la solicitud")

	})

	@PostMapping
	public ResponseEntity<AdministracionResponse<ConfiguracionEmpresaResponse>> crear(
			@Valid @RequestBody AdministracionRequest<ConfiguracionEmpresaRequest> request) {
		respuestas.validarFechaInicio(request.getFechaInicio());
		return ResponseEntity.ok(respuestas.exito(request.getIdTransaccion(), request.getFechaInicio(),
				port.guardarConfiguracion(null, request.getDatos())));
	}

	@Operation(summary = "Actualizar configuración de empresa", description = "Actualizar configuración de empresa. Administra la configuración funcional de cada empresa.")

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
	public ResponseEntity<AdministracionResponse<ConfiguracionEmpresaResponse>> actualizar(@PathVariable("id") Long id,
			@Valid @RequestBody AdministracionRequest<ConfiguracionEmpresaRequest> request) {
		respuestas.validarFechaInicio(request.getFechaInicio());
		return ResponseEntity.ok(respuestas.exito(request.getIdTransaccion(), request.getFechaInicio(),
				port.guardarConfiguracion(id, request.getDatos())));
	}

	@Operation(summary = "Consultar configuración de empresa", description = "Consultar configuración de empresa. Administra la configuración funcional de cada empresa.")

	@ApiResponses(value = {

			@ApiResponse(responseCode = "200", description = "Operación ejecutada correctamente"),

			@ApiResponse(responseCode = "400", description = "Solicitud inválida o datos de entrada no válidos"),

			@ApiResponse(responseCode = "401", description = "No autenticado: se requiere un token JWT válido"),

			@ApiResponse(responseCode = "403", description = "No autorizado para ejecutar la operación"),

			@ApiResponse(responseCode = "404", description = "No se encontró el recurso solicitado"),

			@ApiResponse(responseCode = "500", description = "Error interno al procesar la solicitud")

	})

	@GetMapping("/{id}")
	public ResponseEntity<AdministracionResponse<ConfiguracionEmpresaResponse>> obtener(@PathVariable("id") Long id) {
		return ResponseEntity.ok(respuestas.exitoConsulta(port.obtenerConfiguracion(id)));
	}

	@Operation(summary = "Listar configuración de empresa", description = "Listar configuración de empresa. Administra la configuración funcional de cada empresa.")

	@ApiResponses(value = {

			@ApiResponse(responseCode = "200", description = "Operación ejecutada correctamente"),

			@ApiResponse(responseCode = "400", description = "Solicitud inválida o datos de entrada no válidos"),

			@ApiResponse(responseCode = "401", description = "No autenticado: se requiere un token JWT válido"),

			@ApiResponse(responseCode = "403", description = "No autorizado para ejecutar la operación"),

			@ApiResponse(responseCode = "500", description = "Error interno al procesar la solicitud")

	})

	@GetMapping
	public ResponseEntity<AdministracionResponse<List<ConfiguracionEmpresaResponse>>> listar(
			@RequestParam("idEmpresa") Long idEmpresa) {
		return ResponseEntity.ok(respuestas.exitoConsulta(port.listarConfiguraciones(idEmpresa)));
	}

	@Operation(summary = "Inactivar configuración de empresa", description = "Inactivar configuración de empresa. Administra la configuración funcional de cada empresa.")

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
		port.inactivarConfiguracion(id);
		return ResponseEntity.ok(respuestas.exitoConsulta(null));
	}

	@Operation(summary = "Reactivar configuración de empresa", description = "Reactivar configuración de empresa. Administra la configuración funcional de cada empresa.")

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
		port.reactivarConfiguracion(id);
		return ResponseEntity.ok(respuestas.exitoConsulta(null));
	}

	@Operation(summary = "Eliminar configuración de empresa", description = "Eliminar configuración de empresa. Administra la configuración funcional de cada empresa.")

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
		port.eliminarConfiguracion(id);
		return ResponseEntity.ok(respuestas.exitoConsulta(null));
	}
}
