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
@RequestMapping("/api/v1/empresas")
@RequiredArgsConstructor
public class EmpresaController {
    private final AdministracionPort port;
    private final RespuestaRestFactory respuestas;

    @PostMapping
    public ResponseEntity<AdministracionResponse<EmpresaResponse>> crear(@Valid @RequestBody AdministracionRequest<EmpresaRequest> request) {
        return ResponseEntity.ok(respuestas.exito(request.getIdTransaccion(), request.getFechaInicio(), port.guardarEmpresa(null, request.getDatos())));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdministracionResponse<EmpresaResponse>> actualizar(@PathVariable Long id, @Valid @RequestBody AdministracionRequest<EmpresaRequest> request) {
        return ResponseEntity.ok(respuestas.exito(request.getIdTransaccion(), request.getFechaInicio(), port.guardarEmpresa(id, request.getDatos())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdministracionResponse<EmpresaResponse>> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(respuestas.exitoConsulta(port.obtenerEmpresa(id)));
    }

    @GetMapping
    public ResponseEntity<AdministracionResponse<List<EmpresaResponse>>> listar() {
        return ResponseEntity.ok(respuestas.exitoConsulta(port.listarEmpresas()));
    }

    @PatchMapping("/{id}/inactivar")
    public ResponseEntity<AdministracionResponse<Void>> inactivar(@PathVariable Long id) { port.inactivarEmpresa(id); return ResponseEntity.ok(respuestas.exitoConsulta(null)); }
    @PatchMapping("/{id}/reactivar")
    public ResponseEntity<AdministracionResponse<Void>> reactivar(@PathVariable Long id) { port.reactivarEmpresa(id); return ResponseEntity.ok(respuestas.exitoConsulta(null)); }
    @DeleteMapping("/{id}")
    public ResponseEntity<AdministracionResponse<Void>> eliminar(@PathVariable Long id) { port.eliminarEmpresa(id); return ResponseEntity.ok(respuestas.exitoConsulta(null)); }
}
