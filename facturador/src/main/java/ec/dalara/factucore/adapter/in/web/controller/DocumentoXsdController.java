package ec.dalara.factucore.adapter.in.web.controller;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

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
import ec.dalara.factucore.application.port.in.AdministracionPort;
import ec.dalara.factucore.application.service.XsdConsultaService;
import ec.dalara.factucore.domain.documentoxsd.DocumentDefinitionModel;
import ec.dalara.factucore.domain.documentoxsd.VersionDocumentoXsdModel;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Definiciones de documentos XSD", description = "Administra definiciones documentales, versiones, esquemas y plantillas JSON.")
@RestController
@RequestMapping("/api/v1/documentos-xsd")
@RequiredArgsConstructor
public class DocumentoXsdController {
	private final AdministracionPort port;
	private final RespuestaRestFactory respuestas;
	private final XsdConsultaService consulta;

	@Operation(summary = "Crear definición documental XSD", description = "Crear definición documental XSD. Administra definiciones documentales, versiones, esquemas y plantillas JSON.")

	@ApiResponses(value = {

		@ApiResponse(responseCode = "200", description = "Operación ejecutada correctamente"),

		@ApiResponse(responseCode = "400", description = "Solicitud inválida o datos de entrada no válidos"),

		@ApiResponse(responseCode = "401", description = "No autenticado: se requiere un token JWT válido"),

		@ApiResponse(responseCode = "403", description = "No autorizado para ejecutar la operación"),

		@ApiResponse(responseCode = "409", description = "La operación entra en conflicto con el estado actual del recurso"),

		@ApiResponse(responseCode = "500", description = "Error interno al procesar la solicitud")

	})

	@PostMapping
	public ResponseEntity<AdministracionResponse<DocumentoXsdResponse>> crear(
			@Valid @RequestBody AdministracionRequest<DocumentoXsdRequest> request) {
		respuestas.validarFechaInicio(request.getFechaInicio());
		return ResponseEntity.ok(respuestas.exito(request.getIdTransaccion(), request.getFechaInicio(),
				port.guardarDocumentoXsd(null, request.getDatos())));
	}

	@Operation(summary = "Actualizar definición documental XSD", description = "Actualizar definición documental XSD. Administra definiciones documentales, versiones, esquemas y plantillas JSON.")

	@ApiResponses(value = {

		@ApiResponse(responseCode = "200", description = "Operación ejecutada correctamente"),

		@ApiResponse(responseCode = "400", description = "Solicitud inválida o datos de entrada no válidos"),

		@ApiResponse(responseCode = "401", description = "No autenticado: se requiere un token JWT válido"),

		@ApiResponse(responseCode = "403", description = "No autorizado para ejecutar la operación"),

		@ApiResponse(responseCode = "404", description = "No se encontró el recurso solicitado"),

		@ApiResponse(responseCode = "409", description = "La operación entra en conflicto con el estado actual del recurso"),

		@ApiResponse(responseCode = "500", description = "Error interno al procesar la solicitud")

	})

	@PutMapping("/{id}")
	public ResponseEntity<AdministracionResponse<DocumentoXsdResponse>> actualizar(@PathVariable("id") Long id,
			@Valid @RequestBody AdministracionRequest<DocumentoXsdRequest> request) {
		respuestas.validarFechaInicio(request.getFechaInicio());
		return ResponseEntity.ok(respuestas.exito(request.getIdTransaccion(), request.getFechaInicio(),
				port.guardarDocumentoXsd(id, request.getDatos())));
	}

	@Operation(summary = "Consultar definición documental XSD", description = "Consultar definición documental XSD. Administra definiciones documentales, versiones, esquemas y plantillas JSON.")

	@ApiResponses(value = {

		@ApiResponse(responseCode = "200", description = "Operación ejecutada correctamente"),

		@ApiResponse(responseCode = "400", description = "Solicitud inválida o datos de entrada no válidos"),

		@ApiResponse(responseCode = "401", description = "No autenticado: se requiere un token JWT válido"),

		@ApiResponse(responseCode = "403", description = "No autorizado para ejecutar la operación"),

		@ApiResponse(responseCode = "404", description = "No se encontró el recurso solicitado"),

		@ApiResponse(responseCode = "500", description = "Error interno al procesar la solicitud")

	})

	@GetMapping("/{id}")
	public ResponseEntity<AdministracionResponse<DocumentoXsdResponse>> obtener(@PathVariable("id") Long id) {
		return ResponseEntity.ok(respuestas.exitoConsulta(port.obtenerDocumentoXsd(id)));
	}

	@Operation(summary = "Listar definición documental XSD", description = "Listar definición documental XSD. Administra definiciones documentales, versiones, esquemas y plantillas JSON.")

	@ApiResponses(value = {

		@ApiResponse(responseCode = "200", description = "Operación ejecutada correctamente"),

		@ApiResponse(responseCode = "400", description = "Solicitud inválida o datos de entrada no válidos"),

		@ApiResponse(responseCode = "401", description = "No autenticado: se requiere un token JWT válido"),

		@ApiResponse(responseCode = "403", description = "No autorizado para ejecutar la operación"),

		@ApiResponse(responseCode = "500", description = "Error interno al procesar la solicitud")

	})

	@GetMapping
	public ResponseEntity<AdministracionResponse<List<DocumentoXsdResponse>>> listar() {
		return ResponseEntity.ok(respuestas.exitoConsulta(port.listarDocumentosXsd()));
	}

	@Operation(summary = "Listar versiones documentales", description = "Listar versiones documentales. Administra definiciones documentales, versiones, esquemas y plantillas JSON.")

	@ApiResponses(value = {

		@ApiResponse(responseCode = "200", description = "Operación ejecutada correctamente"),

		@ApiResponse(responseCode = "400", description = "Solicitud inválida o datos de entrada no válidos"),

		@ApiResponse(responseCode = "401", description = "No autenticado: se requiere un token JWT válido"),

		@ApiResponse(responseCode = "403", description = "No autorizado para ejecutar la operación"),

		@ApiResponse(responseCode = "404", description = "No se encontró el recurso solicitado"),

		@ApiResponse(responseCode = "500", description = "Error interno al procesar la solicitud")

	})

	@GetMapping("/{id}/versiones")
	public ResponseEntity<AdministracionResponse<List<VersionDocumentoXsdModel>>> listarVersiones(
			@PathVariable("id") Long id) {
		return ResponseEntity.ok(respuestas.exitoConsulta(consulta.listarVersiones(id)));
	}

	@Operation(summary = "Consultar definición documental", description = "Consultar definición documental. Administra definiciones documentales, versiones, esquemas y plantillas JSON.")

	@ApiResponses(value = {

		@ApiResponse(responseCode = "200", description = "Operación ejecutada correctamente"),

		@ApiResponse(responseCode = "400", description = "Solicitud inválida o datos de entrada no válidos"),

		@ApiResponse(responseCode = "401", description = "No autenticado: se requiere un token JWT válido"),

		@ApiResponse(responseCode = "403", description = "No autorizado para ejecutar la operación"),

		@ApiResponse(responseCode = "404", description = "No se encontró el recurso solicitado"),

		@ApiResponse(responseCode = "500", description = "Error interno al procesar la solicitud")

	})

	@GetMapping("/versiones/{id}")
	public ResponseEntity<AdministracionResponse<DocumentDefinitionModel>> obtenerDefinicion(@PathVariable("id") Long id) {
		return ResponseEntity.ok(respuestas.exitoConsulta(consulta.obtenerDefinicion(id)));
	}

	@Operation(summary = "Descargar plantilla JSON", description = "Descargar plantilla JSON. Administra definiciones documentales, versiones, esquemas y plantillas JSON.")

	@ApiResponses(value = {

		@ApiResponse(responseCode = "200", description = "Operación ejecutada correctamente"),

		@ApiResponse(responseCode = "400", description = "Solicitud inválida o datos de entrada no válidos"),

		@ApiResponse(responseCode = "401", description = "No autenticado: se requiere un token JWT válido"),

		@ApiResponse(responseCode = "403", description = "No autorizado para ejecutar la operación"),

		@ApiResponse(responseCode = "404", description = "No se encontró el recurso solicitado"),

		@ApiResponse(responseCode = "500", description = "Error interno al procesar la solicitud")

	})

	@GetMapping(value = "/versiones/{id}/plantilla-json", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> descargarPlantilla(@PathVariable("id") Long id) {
		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=plantilla.json")
				.contentType(MediaType.APPLICATION_JSON).body(consulta.obtenerPlantilla(id));
	}

	@Operation(summary = "Descargar esquema JSON", description = "Descargar esquema JSON. Administra definiciones documentales, versiones, esquemas y plantillas JSON.")

	@ApiResponses(value = {

		@ApiResponse(responseCode = "200", description = "Operación ejecutada correctamente"),

		@ApiResponse(responseCode = "400", description = "Solicitud inválida o datos de entrada no válidos"),

		@ApiResponse(responseCode = "401", description = "No autenticado: se requiere un token JWT válido"),

		@ApiResponse(responseCode = "403", description = "No autorizado para ejecutar la operación"),

		@ApiResponse(responseCode = "404", description = "No se encontró el recurso solicitado"),

		@ApiResponse(responseCode = "500", description = "Error interno al procesar la solicitud")

	})

	@GetMapping(value = "/versiones/{id}/esquema-json", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> descargarEsquema(@PathVariable("id") Long id) {
		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=esquema.json")
				.contentType(MediaType.APPLICATION_JSON).body(consulta.obtenerEsquema(id));
	}

	@Operation(summary = "Inactivar definición documental XSD", description = "Inactivar definición documental XSD. Administra definiciones documentales, versiones, esquemas y plantillas JSON.")

	@ApiResponses(value = {

		@ApiResponse(responseCode = "200", description = "Operación ejecutada correctamente"),

		@ApiResponse(responseCode = "400", description = "Solicitud inválida o datos de entrada no válidos"),

		@ApiResponse(responseCode = "401", description = "No autenticado: se requiere un token JWT válido"),

		@ApiResponse(responseCode = "403", description = "No autorizado para ejecutar la operación"),

		@ApiResponse(responseCode = "404", description = "No se encontró el recurso solicitado"),

		@ApiResponse(responseCode = "409", description = "La operación entra en conflicto con el estado actual del recurso"),

		@ApiResponse(responseCode = "500", description = "Error interno al procesar la solicitud")

	})

	@PatchMapping("/{id}/inactivar")
	public ResponseEntity<AdministracionResponse<Void>> inactivar(@PathVariable("id") Long id) {
		port.inactivarDocumentoXsd(id);
		return ResponseEntity.ok(respuestas.exitoConsulta(null));
	}

	@Operation(summary = "Reactivar definición documental XSD", description = "Reactivar definición documental XSD. Administra definiciones documentales, versiones, esquemas y plantillas JSON.")

	@ApiResponses(value = {

		@ApiResponse(responseCode = "200", description = "Operación ejecutada correctamente"),

		@ApiResponse(responseCode = "400", description = "Solicitud inválida o datos de entrada no válidos"),

		@ApiResponse(responseCode = "401", description = "No autenticado: se requiere un token JWT válido"),

		@ApiResponse(responseCode = "403", description = "No autorizado para ejecutar la operación"),

		@ApiResponse(responseCode = "404", description = "No se encontró el recurso solicitado"),

		@ApiResponse(responseCode = "409", description = "La operación entra en conflicto con el estado actual del recurso"),

		@ApiResponse(responseCode = "500", description = "Error interno al procesar la solicitud")

	})

	@PatchMapping("/{id}/reactivar")
	public ResponseEntity<AdministracionResponse<Void>> reactivar(@PathVariable("id") Long id) {
		port.reactivarDocumentoXsd(id);
		return ResponseEntity.ok(respuestas.exitoConsulta(null));
	}

	@Operation(summary = "Eliminar definición documental XSD", description = "Eliminar definición documental XSD. Administra definiciones documentales, versiones, esquemas y plantillas JSON.")

	@ApiResponses(value = {

		@ApiResponse(responseCode = "200", description = "Operación ejecutada correctamente"),

		@ApiResponse(responseCode = "400", description = "Solicitud inválida o datos de entrada no válidos"),

		@ApiResponse(responseCode = "401", description = "No autenticado: se requiere un token JWT válido"),

		@ApiResponse(responseCode = "403", description = "No autorizado para ejecutar la operación"),

		@ApiResponse(responseCode = "404", description = "No se encontró el recurso solicitado"),

		@ApiResponse(responseCode = "409", description = "La operación entra en conflicto con el estado actual del recurso"),

		@ApiResponse(responseCode = "500", description = "Error interno al procesar la solicitud")

	})

	@DeleteMapping("/{id}")
	public ResponseEntity<AdministracionResponse<Void>> eliminar(@PathVariable("id") Long id) {
		port.eliminarDocumentoXsd(id);
		return ResponseEntity.ok(respuestas.exitoConsulta(null));
	}
}
