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

import ec.dalara.factucore.application.contract.request.AdministracionRequest;
import ec.dalara.factucore.adapter.in.web.response.RespuestaRestFactory;
import ec.dalara.factucore.application.contract.request.ConfiguracionEmpresaRequest;
import ec.dalara.factucore.application.contract.response.AdministracionResponse;
import ec.dalara.factucore.application.contract.response.ConfiguracionEmpresaResponse;
import ec.dalara.factucore.application.port.in.AdministracionPort;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/configuraciones-empresa")
@RequiredArgsConstructor
public class ConfiguracionEmpresaController {
	private final AdministracionPort port;
	private final RespuestaRestFactory respuestas;

	@PostMapping
	public ResponseEntity<AdministracionResponse<ConfiguracionEmpresaResponse>> crear(
			@Valid @RequestBody AdministracionRequest<ConfiguracionEmpresaRequest> request) {
		respuestas.validarFechaInicio(request.getFechaInicio());
		return ResponseEntity.ok(respuestas.exito(request.getIdTransaccion(), request.getFechaInicio(),
				port.guardarConfiguracion(null, request.getDatos())));
	}

	@PutMapping("/{id}")
	public ResponseEntity<AdministracionResponse<ConfiguracionEmpresaResponse>> actualizar(@PathVariable Long id,
			@Valid @RequestBody AdministracionRequest<ConfiguracionEmpresaRequest> request) {
		respuestas.validarFechaInicio(request.getFechaInicio());
		return ResponseEntity.ok(respuestas.exito(request.getIdTransaccion(), request.getFechaInicio(),
				port.guardarConfiguracion(id, request.getDatos())));
	}

	@GetMapping("/{id}")
	public ResponseEntity<AdministracionResponse<ConfiguracionEmpresaResponse>> obtener(@PathVariable Long id) {
		return ResponseEntity.ok(respuestas.exitoConsulta(port.obtenerConfiguracion(id)));
	}

	@GetMapping
	public ResponseEntity<AdministracionResponse<List<ConfiguracionEmpresaResponse>>> listar(
			@RequestParam Long idEmpresa) {
		return ResponseEntity.ok(respuestas.exitoConsulta(port.listarConfiguraciones(idEmpresa)));
	}

	@PatchMapping("/{id}/inactivar")
	public ResponseEntity<AdministracionResponse<Void>> inactivar(@PathVariable Long id) {
		port.inactivarConfiguracion(id);
		return ResponseEntity.ok(respuestas.exitoConsulta(null));
	}

	@PatchMapping("/{id}/reactivar")
	public ResponseEntity<AdministracionResponse<Void>> reactivar(@PathVariable Long id) {
		port.reactivarConfiguracion(id);
		return ResponseEntity.ok(respuestas.exitoConsulta(null));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<AdministracionResponse<Void>> eliminar(@PathVariable Long id) {
		port.eliminarConfiguracion(id);
		return ResponseEntity.ok(respuestas.exitoConsulta(null));
	}
}
