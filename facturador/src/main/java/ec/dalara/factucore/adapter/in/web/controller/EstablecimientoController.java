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
import ec.dalara.factucore.application.contract.request.EstablecimientoRequest;
import ec.dalara.factucore.application.contract.response.AdministracionResponse;
import ec.dalara.factucore.application.contract.response.EstablecimientoResponse;
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
	public ResponseEntity<AdministracionResponse<EstablecimientoResponse>> crear(
			@Valid @RequestBody AdministracionRequest<EstablecimientoRequest> request) {
		respuestas.validarFechaInicio(request.getFechaInicio());
		return ResponseEntity.ok(respuestas.exito(request.getIdTransaccion(), request.getFechaInicio(),
				port.guardarEstablecimiento(null, request.getDatos())));
	}

	@PutMapping("/{id}")
	public ResponseEntity<AdministracionResponse<EstablecimientoResponse>> actualizar(@PathVariable Long id,
			@Valid @RequestBody AdministracionRequest<EstablecimientoRequest> request) {
		respuestas.validarFechaInicio(request.getFechaInicio());
		return ResponseEntity.ok(respuestas.exito(request.getIdTransaccion(), request.getFechaInicio(),
				port.guardarEstablecimiento(id, request.getDatos())));
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
	public ResponseEntity<AdministracionResponse<Void>> inactivar(@PathVariable Long id) {
		port.inactivarEstablecimiento(id);
		return ResponseEntity.ok(respuestas.exitoConsulta(null));
	}

	@PatchMapping("/{id}/reactivar")
	public ResponseEntity<AdministracionResponse<Void>> reactivar(@PathVariable Long id) {
		port.reactivarEstablecimiento(id);
		return ResponseEntity.ok(respuestas.exitoConsulta(null));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<AdministracionResponse<Void>> eliminar(@PathVariable Long id) {
		port.eliminarEstablecimiento(id);
		return ResponseEntity.ok(respuestas.exitoConsulta(null));
	}
}
