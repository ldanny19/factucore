package ec.dalara.factucore.adapter.in.web.controller;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ec.dalara.factucore.adapter.in.web.response.RespuestaRestFactory;
import ec.dalara.factucore.application.contract.request.AdministracionRequest;
import ec.dalara.factucore.application.contract.request.MapeoXsdRequest;
import ec.dalara.factucore.application.contract.response.AdministracionResponse;
import ec.dalara.factucore.application.contract.response.MapeoXsdResponse;
import ec.dalara.factucore.application.service.MapeoXsdService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Mapeos XSD", description = "Administra los mapeos entre el modelo canónico y las versiones de documentos.")
@RestController
@RequestMapping("/api/v1/mapeos-xsd")
@RequiredArgsConstructor
@Validated
public class MapeoXsdController {

	private final MapeoXsdService mapeoXsdService;
	private final RespuestaRestFactory respuestas;

	@Operation(summary = "Crear mapeo XSD", description = "Crear mapeo XSD. Administra los mapeos entre el modelo canónico y las versiones de documentos.")

	@ApiResponses(value = {

		@ApiResponse(responseCode = "200", description = "Operación ejecutada correctamente"),

		@ApiResponse(responseCode = "400", description = "Solicitud inválida o datos de entrada no válidos"),

		@ApiResponse(responseCode = "401", description = "No autenticado: se requiere un token JWT válido"),

		@ApiResponse(responseCode = "403", description = "No autorizado para ejecutar la operación"),

		@ApiResponse(responseCode = "409", description = "La operación entra en conflicto con el estado actual del recurso"),

		@ApiResponse(responseCode = "500", description = "Error interno al procesar la solicitud")

	})

	@PostMapping
	public ResponseEntity<AdministracionResponse<List<MapeoXsdResponse>>> crear(
			@Valid @RequestBody AdministracionRequest<List<@Valid MapeoXsdRequest>> request) {
		respuestas.validarFechaInicio(request.getFechaInicio());
		return ResponseEntity.ok(respuestas.exito(request.getIdTransaccion(), request.getFechaInicio(),
				mapeoXsdService.crear(request.getDatos())));
	}

	@Operation(summary = "Listar mapeos por versión documental", description = "Listar mapeos por versión documental. Administra los mapeos entre el modelo canónico y las versiones de documentos.")

	@ApiResponses(value = {

		@ApiResponse(responseCode = "200", description = "Operación ejecutada correctamente"),

		@ApiResponse(responseCode = "400", description = "Solicitud inválida o datos de entrada no válidos"),

		@ApiResponse(responseCode = "401", description = "No autenticado: se requiere un token JWT válido"),

		@ApiResponse(responseCode = "403", description = "No autorizado para ejecutar la operación"),

		@ApiResponse(responseCode = "404", description = "No se encontró el recurso solicitado"),

		@ApiResponse(responseCode = "500", description = "Error interno al procesar la solicitud")

	})

	@GetMapping("/version/{versionDocumentoXsdId}")
	public ResponseEntity<AdministracionResponse<List<MapeoXsdResponse>>> listarPorVersion(
			@PathVariable("versionDocumentoXsdId") Long versionDocumentoXsdId) {
		return ResponseEntity.ok(respuestas.exitoConsulta(mapeoXsdService.listarPorVersion(versionDocumentoXsdId)));
	}
}
