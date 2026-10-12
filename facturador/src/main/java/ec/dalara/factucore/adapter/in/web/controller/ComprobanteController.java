package ec.dalara.factucore.adapter.in.web.controller;

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

@RestController
@RequestMapping("/api/v1/comprobantes")
@RequiredArgsConstructor
public class ComprobanteController {

	private final ComprobanteWorkflowPort workflowPort;
	private final ComprobanteReprocessService reprocessService;

	@PostMapping
	public ResponseEntity<ComprobanteGeneracionResponse> generar(
			@Valid @RequestBody ComprobanteGeneracionRequest request) {
		return ResponseEntity.ok(workflowPort.procesar(request));
	}

	@PostMapping("/{comprobanteId}/reprocesar")
	public ResponseEntity<ComprobanteGeneracionResponse> reprocesar(@PathVariable Long comprobanteId) {
		return ResponseEntity.ok(reprocessService.reprocesar(comprobanteId));
	}

}
