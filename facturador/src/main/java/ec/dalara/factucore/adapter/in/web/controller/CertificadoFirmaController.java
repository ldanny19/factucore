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
import ec.dalara.factucore.application.contract.request.CertificadoFirmaRequest;
import ec.dalara.factucore.application.contract.response.AdministracionResponse;
import ec.dalara.factucore.application.contract.response.CertificadoFirmaResponse;
import ec.dalara.factucore.application.port.in.AdministracionPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Certificados de firma", description = "Administra los certificados de firma electrónica asociados a una empresa.")
@RestController
@RequestMapping("/api/v1/certificados-firma")
@RequiredArgsConstructor
public class CertificadoFirmaController {

	private final AdministracionPort port;
	private final RespuestaRestFactory respuestas;

	@Operation(summary = "Crear certificado de firma", description = "Crear certificado de firma. Administra los certificados de firma electrónica asociados a una empresa.")

	@ApiResponses(value = {

			@ApiResponse(responseCode = "200", description = "Operación ejecutada correctamente"),

			@ApiResponse(responseCode = "400", description = "Solicitud inválida o datos de entrada no válidos"),

			@ApiResponse(responseCode = "401", description = "No autenticado: se requiere un token JWT válido"),

			@ApiResponse(responseCode = "403", description = "No autorizado para ejecutar la operación"),

			@ApiResponse(responseCode = "409", description = "La operación entra en conflicto con el estado actual del recurso"),

			@ApiResponse(responseCode = "500", description = "Error interno al procesar la solicitud")

	})

	@PostMapping
	public ResponseEntity<AdministracionResponse<CertificadoFirmaResponse>> crear(
			@Valid @RequestBody AdministracionRequest<CertificadoFirmaRequest> request) {
		respuestas.validarFechaInicio(request.getFechaInicio());
		return ResponseEntity.ok(respuestas.exito(request.getIdTransaccion(), request.getFechaInicio(),
				port.guardarCertificadoFirma(null, request.getDatos())));
	}

	@Operation(summary = "Actualizar certificado de firma", description = "Actualizar certificado de firma. Administra los certificados de firma electrónica asociados a una empresa.")

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
	public ResponseEntity<AdministracionResponse<CertificadoFirmaResponse>> actualizar(@PathVariable("id") Long id,
			@Valid @RequestBody AdministracionRequest<CertificadoFirmaRequest> request) {
		respuestas.validarFechaInicio(request.getFechaInicio());
		return ResponseEntity.ok(respuestas.exito(request.getIdTransaccion(), request.getFechaInicio(),
				port.guardarCertificadoFirma(id, request.getDatos())));
	}

	@Operation(summary = "Consultar certificado de firma", description = "Consultar certificado de firma. Administra los certificados de firma electrónica asociados a una empresa.")

	@ApiResponses(value = {

			@ApiResponse(responseCode = "200", description = "Operación ejecutada correctamente"),

			@ApiResponse(responseCode = "400", description = "Solicitud inválida o datos de entrada no válidos"),

			@ApiResponse(responseCode = "401", description = "No autenticado: se requiere un token JWT válido"),

			@ApiResponse(responseCode = "403", description = "No autorizado para ejecutar la operación"),

			@ApiResponse(responseCode = "404", description = "No se encontró el recurso solicitado"),

			@ApiResponse(responseCode = "500", description = "Error interno al procesar la solicitud")

	})

	@GetMapping("/{id}")
	public ResponseEntity<AdministracionResponse<CertificadoFirmaResponse>> obtener(@PathVariable("id") Long id) {
		return ResponseEntity.ok(respuestas.exitoConsulta(port.obtenerCertificadoFirma(id)));
	}

	@Operation(summary = "Listar certificado de firma", description = "Listar certificado de firma. Administra los certificados de firma electrónica asociados a una empresa.")

	@ApiResponses(value = {

			@ApiResponse(responseCode = "200", description = "Operación ejecutada correctamente"),

			@ApiResponse(responseCode = "400", description = "Solicitud inválida o datos de entrada no válidos"),

			@ApiResponse(responseCode = "401", description = "No autenticado: se requiere un token JWT válido"),

			@ApiResponse(responseCode = "403", description = "No autorizado para ejecutar la operación"),

			@ApiResponse(responseCode = "500", description = "Error interno al procesar la solicitud")

	})

	@GetMapping
	public ResponseEntity<AdministracionResponse<List<CertificadoFirmaResponse>>> listar(
			@RequestParam("idEmpresa") Long idEmpresa) {
		return ResponseEntity.ok(respuestas.exitoConsulta(port.listarCertificadosFirma(idEmpresa)));
	}

	@Operation(summary = "Inactivar certificado de firma", description = "Inactivar certificado de firma. Administra los certificados de firma electrónica asociados a una empresa.")

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
		port.inactivarCertificadoFirma(id);
		return ResponseEntity.ok(respuestas.exitoConsulta(null));
	}

	@Operation(summary = "Reactivar certificado de firma", description = "Reactivar certificado de firma. Administra los certificados de firma electrónica asociados a una empresa.")

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
		port.reactivarCertificadoFirma(id);
		return ResponseEntity.ok(respuestas.exitoConsulta(null));
	}

	@Operation(summary = "Eliminar certificado de firma", description = "Eliminar certificado de firma. Administra los certificados de firma electrónica asociados a una empresa.")

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
		port.eliminarCertificadoFirma(id);
		return ResponseEntity.ok(respuestas.exitoConsulta(null));
	}
}
