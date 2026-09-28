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
@RequestMapping("/api/v1/establecimientos")
@RequiredArgsConstructor
public class EstablecimientoController {
    private final AdministracionPort port;
    private final RespuestaRestFactory respuestas;

    @PostMapping
    public ResponseEntity<AdministracionResponse<EstablecimientoResponse>> crear(@Valid @RequestBody AdministracionRequest<EstablecimientoRequest> request) {
        return ResponseEntity.ok(respuestas.exito(request.getIdTransaccion(), request.getFechaInicio(), port.guardarEstablecimiento(null, request.getDatos())));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdministracionResponse<EstablecimientoResponse>> actualizar(@PathVariable Long id, @Valid @RequestBody AdministracionRequest<EstablecimientoRequest> request) {
        return ResponseEntity.ok(respuestas.exito(request.getIdTransaccion(), request.getFechaInicio(), port.guardarEstablecimiento(id, request.getDatos())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdministracionResponse<EstablecimientoResponse>> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(respuestas.exitoConsulta(port.obtenerEstablecimiento(id)));
    }

    @GetMapping
    public ResponseEntity<AdministracionResponse<List<EstablecimientoResponse>>> listar(@RequestParam Long idEmpresa) {
        return ResponseEntity.ok(respuestas.exitoConsulta(port.listarEstablecimientos(idEmpresa)));
    }

    @PatchMapping("/{id}/inactivar")
    public ResponseEntity<AdministracionResponse<Void>> inactivar(@PathVariable Long id) { port.inactivarEstablecimiento(id); return ResponseEntity.ok(respuestas.exitoConsulta(null)); }
    @PatchMapping("/{id}/reactivar")
    public ResponseEntity<AdministracionResponse<Void>> reactivar(@PathVariable Long id) { port.reactivarEstablecimiento(id); return ResponseEntity.ok(respuestas.exitoConsulta(null)); }
    @DeleteMapping("/{id}")
    public ResponseEntity<AdministracionResponse<Void>> eliminar(@PathVariable Long id) { port.eliminarEstablecimiento(id); return ResponseEntity.ok(respuestas.exitoConsulta(null)); }
}
