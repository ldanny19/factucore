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
import ec.dalara.factucore.application.contract.request.CatalogoItemRequest;
import ec.dalara.factucore.application.contract.response.AdministracionResponse;
import ec.dalara.factucore.application.contract.response.CatalogoItemResponse;
import ec.dalara.factucore.application.port.in.AdministracionPort;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/catalogo-items")
@RequiredArgsConstructor
public class CatalogoItemController {

	private final AdministracionPort port;
	private final RespuestaRestFactory respuestas;

	@PostMapping
	public ResponseEntity<AdministracionResponse<CatalogoItemResponse>> crear(
			@Valid @RequestBody AdministracionGlobalRequest<CatalogoItemRequest> request) {
		respuestas.validarFechaInicio(request.getFechaInicio());
		return ResponseEntity.ok(respuestas.exito(request.getIdTransaccion(), request.getFechaInicio(),
				port.guardarCatalogoItem(null, request.getDatos(), request.getCanal())));
	}

	@PutMapping("/{id}")
	public ResponseEntity<AdministracionResponse<CatalogoItemResponse>> actualizar(@PathVariable Long id,
			@Valid @RequestBody AdministracionGlobalRequest<CatalogoItemRequest> request) {
		respuestas.validarFechaInicio(request.getFechaInicio());
		return ResponseEntity.ok(respuestas.exito(request.getIdTransaccion(), request.getFechaInicio(),
				port.guardarCatalogoItem(id, request.getDatos(), request.getCanal())));
	}

	@GetMapping
	public ResponseEntity<AdministracionResponse<List<CatalogoItemResponse>>> listar(
			@RequestParam(required = false) Long catalogoId) {
		return ResponseEntity.ok(respuestas.exitoConsulta(port.listarCatalogoItems(catalogoId)));
	}

	@GetMapping("/{id}")
	public ResponseEntity<AdministracionResponse<CatalogoItemResponse>> obtener(@PathVariable Long id) {
		return ResponseEntity.ok(respuestas.exitoConsulta(port.obtenerCatalogoItem(id)));
	}

	@PatchMapping("/{id}/inactivar")
	public ResponseEntity<AdministracionResponse<Void>> inactivar(@PathVariable Long id) {
		port.inactivarCatalogoItem(id);
		return ResponseEntity.ok(respuestas.exitoConsulta(null));
	}

	@PatchMapping("/{id}/reactivar")
	public ResponseEntity<AdministracionResponse<Void>> reactivar(@PathVariable Long id) {
		port.reactivarCatalogoItem(id);
		return ResponseEntity.ok(respuestas.exitoConsulta(null));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<AdministracionResponse<Void>> eliminar(@PathVariable Long id) {
		port.eliminarCatalogoItem(id);
		return ResponseEntity.ok(respuestas.exitoConsulta(null));
	}
}
