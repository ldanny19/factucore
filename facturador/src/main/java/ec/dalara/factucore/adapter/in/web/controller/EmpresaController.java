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
import org.springframework.web.bind.annotation.RestController;

import ec.dalara.factucore.adapter.in.web.response.RespuestaRestFactory;
import ec.dalara.factucore.application.contract.request.AdministracionRequest;
import ec.dalara.factucore.application.contract.request.EmpresaRequest;
import ec.dalara.factucore.application.contract.response.AdministracionResponse;
import ec.dalara.factucore.application.contract.response.EmpresaResponse;
import ec.dalara.factucore.application.port.in.AdministracionPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Empresas", description = "Administra las empresas emisoras registradas en FactuCore.")
@RestController
@RequestMapping("/api/v1/empresas")
@RequiredArgsConstructor
public class EmpresaController {
	private final AdministracionPort port;
	private final RespuestaRestFactory respuestas;

	@Operation(summary = "Crear empresa", description = "Crear empresa. Administra las empresas emisoras registradas en FactuCore.")

	@ApiResponses(value = {

			@ApiResponse(responseCode = "200", description = "Operación ejecutada correctamente"),

			@ApiResponse(responseCode = "400", description = "Solicitud inválida o datos de entrada no válidos"),

			@ApiResponse(responseCode = "401", description = "No autenticado: se requiere un token JWT válido"),

			@ApiResponse(responseCode = "403", description = "No autorizado para ejecutar la operación"),

			@ApiResponse(responseCode = "409", description = "La operación entra en conflicto con el estado actual del recurso"),

			@ApiResponse(responseCode = "500", description = "Error interno al procesar la solicitud")

	})

	@PostMapping
	public ResponseEntity<AdministracionResponse<EmpresaResponse>> crear(
			@Valid @RequestBody AdministracionRequest<EmpresaRequest> request) {
		respuestas.validarFechaInicio(request.getFechaInicio());
		return ResponseEntity.ok(respuestas.exito(request.getIdTransaccion(), request.getFechaInicio(),
				port.guardarEmpresa(null, request.getDatos())));
	}

	@Operation(summary = "Actualizar empresa", description = "Actualizar empresa. Administra las empresas emisoras registradas en FactuCore.")

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
	public ResponseEntity<AdministracionResponse<EmpresaResponse>> actualizar(@PathVariable("id") Long id,
			@Valid @RequestBody AdministracionRequest<EmpresaRequest> request) {
		respuestas.validarFechaInicio(request.getFechaInicio());
		return ResponseEntity.ok(respuestas.exito(request.getIdTransaccion(), request.getFechaInicio(),
				port.guardarEmpresa(id, request.getDatos())));
	}

	@Operation(summary = "Consultar empresa", description = "Consultar empresa. Administra las empresas emisoras registradas en FactuCore.")

	@ApiResponses(value = {

			@ApiResponse(responseCode = "200", description = "Operación ejecutada correctamente"),

			@ApiResponse(responseCode = "400", description = "Solicitud inválida o datos de entrada no válidos"),

			@ApiResponse(responseCode = "401", description = "No autenticado: se requiere un token JWT válido"),

			@ApiResponse(responseCode = "403", description = "No autorizado para ejecutar la operación"),

			@ApiResponse(responseCode = "404", description = "No se encontró el recurso solicitado"),

			@ApiResponse(responseCode = "500", description = "Error interno al procesar la solicitud")

	})

	@GetMapping("/{id}")
	public ResponseEntity<AdministracionResponse<EmpresaResponse>> obtener(@PathVariable("id") Long id) {
		return ResponseEntity.ok(respuestas.exitoConsulta(port.obtenerEmpresa(id)));
	}

	@Operation(summary = "Listar empresa", description = "Listar empresa. Administra las empresas emisoras registradas en FactuCore.")

	@ApiResponses(value = {

			@ApiResponse(responseCode = "200", description = "Operación ejecutada correctamente"),

			@ApiResponse(responseCode = "400", description = "Solicitud inválida o datos de entrada no válidos"),

			@ApiResponse(responseCode = "401", description = "No autenticado: se requiere un token JWT válido"),

			@ApiResponse(responseCode = "403", description = "No autorizado para ejecutar la operación"),

			@ApiResponse(responseCode = "500", description = "Error interno al procesar la solicitud")

	})

	@GetMapping
	public ResponseEntity<AdministracionResponse<List<EmpresaResponse>>> listar() {
		return ResponseEntity.ok(respuestas.exitoConsulta(port.listarEmpresas()));
	}

	@Operation(summary = "Inactivar empresa", description = "Inactivar empresa. Administra las empresas emisoras registradas en FactuCore.")

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
		port.inactivarEmpresa(id);
		return ResponseEntity.ok(respuestas.exitoConsulta(null));
	}

	@Operation(summary = "Reactivar empresa", description = "Reactivar empresa. Administra las empresas emisoras registradas en FactuCore.")

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
		port.reactivarEmpresa(id);
		return ResponseEntity.ok(respuestas.exitoConsulta(null));
	}

	@Operation(summary = "Eliminar empresa", description = "Eliminar empresa. Administra las empresas emisoras registradas en FactuCore.")

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
		port.eliminarEmpresa(id);
		return ResponseEntity.ok(respuestas.exitoConsulta(null));
	}
}
