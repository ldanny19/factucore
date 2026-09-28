package ec.dalara.factucore.adapter.in.web.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ec.dalara.factucore.application.contract.request.*;
import ec.dalara.factucore.application.contract.response.*;
import ec.dalara.factucore.application.port.in.AdministracionPort;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/secuenciales")
@RequiredArgsConstructor
public class SecuencialController {
    private final AdministracionPort port;
    private final RespuestaRestFactory respuestas;

    @PostMapping
    public ResponseEntity<AdministracionResponse<SecuencialResponse>> crear(@Valid @RequestBody AdministracionRequest<SecuencialRequest> request) {
        return ResponseEntity.ok(respuestas.exito(request.getIdTransaccion(), request.getFechaInicio(), port.guardarSecuencial(null, request.getDatos())));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdministracionResponse<SecuencialResponse>> actualizar(@PathVariable Long id, @Valid @RequestBody AdministracionRequest<SecuencialRequest> request) {
        return ResponseEntity.ok(respuestas.exito(request.getIdTransaccion(), request.getFechaInicio(), port.guardarSecuencial(id, request.getDatos())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdministracionResponse<SecuencialResponse>> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(respuestas.exitoConsulta(port.obtenerSecuencial(id)));
    }

    @GetMapping
    public ResponseEntity<AdministracionResponse<List<SecuencialResponse>>> listar() {
        return ResponseEntity.ok(respuestas.exitoConsulta(port.listarSecuenciales()));
    }

    @PatchMapping("/{id}/inactivar")
    public ResponseEntity<AdministracionResponse<Void>> inactivar(@PathVariable Long id) { port.inactivarSecuencial(id); return ResponseEntity.ok(respuestas.exitoConsulta(null)); }
    @PatchMapping("/{id}/reactivar")
    public ResponseEntity<AdministracionResponse<Void>> reactivar(@PathVariable Long id) { port.reactivarSecuencial(id); return ResponseEntity.ok(respuestas.exitoConsulta(null)); }
    @DeleteMapping("/{id}")
    public ResponseEntity<AdministracionResponse<Void>> eliminar(@PathVariable Long id) { port.eliminarSecuencial(id); return ResponseEntity.ok(respuestas.exitoConsulta(null)); }
}
