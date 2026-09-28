package ec.dalara.factucore.adapter.in.web.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ec.dalara.factucore.application.contract.request.ComprobanteGeneracionRequest;
import ec.dalara.factucore.application.contract.response.ComprobanteGeneracionResponse;
import ec.dalara.factucore.application.port.in.ComprobanteWorkflowPort;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/comprobantes")
@RequiredArgsConstructor
public class ComprobanteController {

    private final ComprobanteWorkflowPort workflowPort;

    @PostMapping
    public ResponseEntity<ComprobanteGeneracionResponse> generar(
            @Valid @RequestBody ComprobanteGeneracionRequest request) {
        return ResponseEntity.ok(workflowPort.procesar(request));
    }

    @PostMapping("/{id}/reprocesar")
    public ResponseEntity<ComprobanteGeneracionResponse> reprocesar(@PathVariable Long id) {
        workflowPort.reprocesar(id);
        return ResponseEntity.ok(ComprobanteGeneracionResponse.builder()
                .exitoso(true)
                .estado("REPROCESADO")
                .build());
    }
}
