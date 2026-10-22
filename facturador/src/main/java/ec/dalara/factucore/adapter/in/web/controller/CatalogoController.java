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
import ec.dalara.factucore.application.contract.request.AdministracionGlobalRequest;
import ec.dalara.factucore.application.contract.request.CatalogoRequest;
import ec.dalara.factucore.application.contract.response.AdministracionResponse;
import ec.dalara.factucore.application.contract.response.CatalogoResponse;
import ec.dalara.factucore.application.port.in.AdministracionPort;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/catalogos")
@RequiredArgsConstructor
public class CatalogoController {

	private final AdministracionPort port;
	private final RespuestaRestFactory respuestas;

	@PostMapping
	public ResponseEntity<AdministracionResponse<CatalogoResponse>> crear(
			@Valid @RequestBody AdministracionGlobalRequest<CatalogoRequest> request) {
		respuestas.validarFechaInicio(request.getFechaInicio());
		return ResponseEntity.ok(respuestas.exito(request.getIdTransaccion(), request.getFechaInicio(),
				port.guardarCatalogo(null, request.getDatos(), request.getCanal())));
	}

	@PutMapping("/{id}")
	public ResponseEntity<AdministracionResponse<CatalogoResponse>> actualizar(@PathVariable Long id,
			@Valid @RequestBody AdministracionGlobalRequest<CatalogoRequest> request) {
		respuestas.validarFechaInicio(request.getFechaInicio());
		return ResponseEntity.ok(respuestas.exito(request.getIdTransaccion(), request.getFechaInicio(),
				port.guardarCatalogo(id, request.getDatos(), request.getCanal())));
	}

	@GetMapping
	public ResponseEntity<AdministracionResponse<List<CatalogoResponse>>> listar() {
		return ResponseEntity.ok(respuestas.exitoConsulta(port.listarCatalogos()));
	}

	@GetMapping(params = "codigo")
	public ResponseEntity<AdministracionResponse<CatalogoResponse>> obtenerPorCodigo(
			@RequestParam String codigo) {
		return ResponseEntity.ok(respuestas.exitoConsulta(port.obtenerCatalogoPorCodigo(codigo)));
	}

	@GetMapping("/{id}")
	public ResponseEntity<AdministracionResponse<CatalogoResponse>> obtener(@PathVariable Long id) {
		return ResponseEntity.ok(respuestas.exitoConsulta(port.obtenerCatalogo(id)));
	}

	@PatchMapping("/{id}/inactivar")
	public ResponseEntity<AdministracionResponse<Void>> inactivar(@PathVariable Long id) {
		port.inactivarCatalogo(id);
		return ResponseEntity.ok(respuestas.exitoConsulta(null));
	}

	@PatchMapping("/{id}/reactivar")
	public ResponseEntity<AdministracionResponse<Void>> reactivar(@PathVariable Long id) {
		port.reactivarCatalogo(id);
		return ResponseEntity.ok(respuestas.exitoConsulta(null));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<AdministracionResponse<Void>> eliminar(@PathVariable Long id) {
		port.eliminarCatalogo(id);
		return ResponseEntity.ok(respuestas.exitoConsulta(null));
	}
}
