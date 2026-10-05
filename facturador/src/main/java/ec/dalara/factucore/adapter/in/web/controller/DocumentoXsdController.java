package ec.dalara.factucore.adapter.in.web.controller;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ec.dalara.factucore.adapter.in.web.response.RespuestaRestFactory;
import ec.dalara.factucore.application.contract.request.AdministracionRequest;
import ec.dalara.factucore.application.contract.request.DocumentoXsdRequest;
import ec.dalara.factucore.application.contract.response.AdministracionResponse;
import ec.dalara.factucore.application.contract.response.DocumentoXsdResponse;
import ec.dalara.factucore.application.service.XsdConsultaService;
import ec.dalara.factucore.domain.documentoxsd.DocumentDefinitionModel;
import ec.dalara.factucore.domain.documentoxsd.VersionDocumentoXsdModel;
import ec.dalara.factucore.application.port.in.AdministracionPort;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/documentos-xsd")
@RequiredArgsConstructor
public class DocumentoXsdController {
	private final AdministracionPort port;
	private final RespuestaRestFactory respuestas;
	private final XsdConsultaService consulta;

	@PostMapping
	public ResponseEntity<AdministracionResponse<DocumentoXsdResponse>> crear(
			@Valid @RequestBody AdministracionRequest<DocumentoXsdRequest> request) {
		respuestas.validarFechaInicio(request.getFechaInicio());
		return ResponseEntity.ok(respuestas.exito(request.getIdTransaccion(), request.getFechaInicio(),
				port.guardarDocumentoXsd(null, request.getDatos())));
	}

	@PutMapping("/{id}")
	public ResponseEntity<AdministracionResponse<DocumentoXsdResponse>> actualizar(@PathVariable Long id,
			@Valid @RequestBody AdministracionRequest<DocumentoXsdRequest> request) {
		respuestas.validarFechaInicio(request.getFechaInicio());
		return ResponseEntity.ok(respuestas.exito(request.getIdTransaccion(), request.getFechaInicio(),
				port.guardarDocumentoXsd(id, request.getDatos())));
	}

	@GetMapping("/{id}")
	public ResponseEntity<AdministracionResponse<DocumentoXsdResponse>> obtener(@PathVariable Long id) {
		return ResponseEntity.ok(respuestas.exitoConsulta(port.obtenerDocumentoXsd(id)));
	}

	@GetMapping
	public ResponseEntity<AdministracionResponse<List<DocumentoXsdResponse>>> listar() {
		return ResponseEntity.ok(respuestas.exitoConsulta(port.listarDocumentosXsd()));
	}

	@GetMapping("/{id}/versiones")
	public ResponseEntity<AdministracionResponse<List<VersionDocumentoXsdModel>>> listarVersiones(@PathVariable Long id) {
		return ResponseEntity.ok(respuestas.exitoConsulta(consulta.listarVersiones(id)));
	}

	@GetMapping("/versiones/{id}")
	public ResponseEntity<AdministracionResponse<DocumentDefinitionModel>> obtenerDefinicion(@PathVariable Long id) {
		return ResponseEntity.ok(respuestas.exitoConsulta(consulta.obtenerDefinicion(id)));
	}

	@GetMapping(value = "/versiones/{id}/plantilla-json", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> descargarPlantilla(@PathVariable Long id) {
		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=plantilla.json")
				.contentType(MediaType.APPLICATION_JSON).body(consulta.obtenerPlantilla(id));
	}

	@GetMapping(value = "/versiones/{id}/esquema-json", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> descargarEsquema(@PathVariable Long id) {
		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=esquema.json")
				.contentType(MediaType.APPLICATION_JSON).body(consulta.obtenerEsquema(id));
	}

	@PatchMapping("/{id}/inactivar")
	public ResponseEntity<AdministracionResponse<Void>> inactivar(@PathVariable Long id) {
		port.inactivarDocumentoXsd(id);
		return ResponseEntity.ok(respuestas.exitoConsulta(null));
	}

	@PatchMapping("/{id}/reactivar")
	public ResponseEntity<AdministracionResponse<Void>> reactivar(@PathVariable Long id) {
		port.reactivarDocumentoXsd(id);
		return ResponseEntity.ok(respuestas.exitoConsulta(null));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<AdministracionResponse<Void>> eliminar(@PathVariable Long id) {
		port.eliminarDocumentoXsd(id);
		return ResponseEntity.ok(respuestas.exitoConsulta(null));
	}
}
