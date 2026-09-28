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
@RequestMapping("/api/v1/puntos-emision")
@RequiredArgsConstructor
public class PuntoEmisionController {
    private final AdministracionPort port;
    private final RespuestaRestFactory respuestas;

    @PostMapping
    public ResponseEntity<AdministracionResponse<PuntoEmisionResponse>> crear(@Valid @RequestBody AdministracionRequest<PuntoEmisionRequest> request) {
        return ResponseEntity.ok(respuestas.exito(request.getIdTransaccion(), request.getFechaInicio(), port.guardarPuntoEmision(null, request.getDatos())));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdministracionResponse<PuntoEmisionResponse>> actualizar(@PathVariable Long id, @Valid @RequestBody AdministracionRequest<PuntoEmisionRequest> request) {
        return ResponseEntity.ok(respuestas.exito(request.getIdTransaccion(), request.getFechaInicio(), port.guardarPuntoEmision(id, request.getDatos())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdministracionResponse<PuntoEmisionResponse>> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(respuestas.exitoConsulta(port.obtenerPuntoEmision(id)));
    }

    @GetMapping
    public ResponseEntity<AdministracionResponse<List<PuntoEmisionResponse>>> listar(@RequestParam Long idEstablecimiento) {
        return ResponseEntity.ok(respuestas.exitoConsulta(port.listarPuntosEmision(idEstablecimiento)));
    }

    @PatchMapping("/{id}/inactivar")
    public ResponseEntity<AdministracionResponse<Void>> inactivar(@PathVariable Long id) { port.inactivarPuntoEmision(id); return ResponseEntity.ok(respuestas.exitoConsulta(null)); }
    @PatchMapping("/{id}/reactivar")
    public ResponseEntity<AdministracionResponse<Void>> reactivar(@PathVariable Long id) { port.reactivarPuntoEmision(id); return ResponseEntity.ok(respuestas.exitoConsulta(null)); }
    @DeleteMapping("/{id}")
    public ResponseEntity<AdministracionResponse<Void>> eliminar(@PathVariable Long id) { port.eliminarPuntoEmision(id); return ResponseEntity.ok(respuestas.exitoConsulta(null)); }
}
