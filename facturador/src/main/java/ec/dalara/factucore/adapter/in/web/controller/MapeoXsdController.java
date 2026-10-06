package ec.dalara.factucore.adapter.in.web.controller;

import java.util.List;

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

@RestController
@RequestMapping("/api/v1/mapeos-xsd")
@RequiredArgsConstructor
@Validated
public class MapeoXsdController {

    private final MapeoXsdService mapeoXsdService;
    private final RespuestaRestFactory respuestas;

    @PostMapping
    public ResponseEntity<AdministracionResponse<List<MapeoXsdResponse>>> crear(
            @Valid @RequestBody AdministracionRequest<List<@Valid MapeoXsdRequest>> request) {
        respuestas.validarFechaInicio(request.getFechaInicio());
        return ResponseEntity.ok(respuestas.exito(request.getIdTransaccion(), request.getFechaInicio(),
                mapeoXsdService.crear(request.getDatos())));
    }

    @GetMapping("/version/{versionDocumentoXsdId}")
    public ResponseEntity<AdministracionResponse<List<MapeoXsdResponse>>> listarPorVersion(
            @PathVariable Long versionDocumentoXsdId) {
        return ResponseEntity.ok(respuestas.exitoConsulta(mapeoXsdService.listarPorVersion(versionDocumentoXsdId)));
    }
}
