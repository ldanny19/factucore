package ec.dalara.factucore.adapter.in.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ec.dalara.factucore.application.contract.request.ComprobanteGeneracionRequest;
import ec.dalara.factucore.application.contract.response.ComprobanteGeneracionResponse;
import ec.dalara.factucore.application.port.in.ComprobanteWorkflowPort;
import ec.dalara.factucore.application.service.ComprobanteReprocessService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Comprobantes electrónicos", description = "Genera comprobantes electrónicos y permite reanudar su procesamiento.")
@RestController
@RequestMapping("/api/v1/comprobantes")
@RequiredArgsConstructor
public class ComprobanteController {

	private final ComprobanteWorkflowPort workflowPort;
	private final ComprobanteReprocessService reprocessService;

	@Operation(summary = "Generar comprobante electrónico", description = "Generar comprobante electrónico. Genera comprobantes electrónicos y permite reanudar su procesamiento.")

	@ApiResponses(value = {

		@ApiResponse(responseCode = "200", description = "Operación ejecutada correctamente"),

		@ApiResponse(responseCode = "400", description = "Solicitud inválida o datos de entrada no válidos"),

		@ApiResponse(responseCode = "401", description = "No autenticado: se requiere un token JWT válido"),

		@ApiResponse(responseCode = "403", description = "No autorizado para ejecutar la operación"),

		@ApiResponse(responseCode = "409", description = "La operación entra en conflicto con el estado actual del recurso"),

		@ApiResponse(responseCode = "500", description = "Error interno al procesar la solicitud")

	})

	@PostMapping
	public ResponseEntity<ComprobanteGeneracionResponse> generar(
			@Valid @RequestBody ComprobanteGeneracionRequest request) {
		return ResponseEntity.ok(workflowPort.procesar(request));
	}

	@Operation(summary = "Reprocesar comprobante electrónico", description = "Reprocesar comprobante electrónico. Genera comprobantes electrónicos y permite reanudar su procesamiento.")

	@ApiResponses(value = {

		@ApiResponse(responseCode = "200", description = "Operación ejecutada correctamente"),

		@ApiResponse(responseCode = "400", description = "Solicitud inválida o datos de entrada no válidos"),

		@ApiResponse(responseCode = "401", description = "No autenticado: se requiere un token JWT válido"),

		@ApiResponse(responseCode = "403", description = "No autorizado para ejecutar la operación"),

		@ApiResponse(responseCode = "404", description = "No se encontró el recurso solicitado"),

		@ApiResponse(responseCode = "409", description = "La operación entra en conflicto con el estado actual del recurso"),

		@ApiResponse(responseCode = "500", description = "Error interno al procesar la solicitud")

	})

	@PostMapping("/{comprobanteId}/reprocesar")
	public ResponseEntity<ComprobanteGeneracionResponse> reprocesar(@PathVariable Long comprobanteId) {
		return ResponseEntity.ok(reprocessService.reprocesar(comprobanteId));
	}

}
