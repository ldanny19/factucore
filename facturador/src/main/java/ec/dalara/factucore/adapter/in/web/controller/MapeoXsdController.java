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

import ec.dalara.factucore.application.contract.request.MapeoXsdRequest;
import ec.dalara.factucore.application.contract.response.MapeoXsdResponse;
import ec.dalara.factucore.application.service.MapeoXsdService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/mapeos-xsd")
@RequiredArgsConstructor
@Validated
public class MapeoXsdController {

    private final MapeoXsdService mapeoXsdService;

    @PostMapping
    public ResponseEntity<MapeoXsdResponse> crear(@Valid @RequestBody MapeoXsdRequest request) {
        return ResponseEntity.ok(mapeoXsdService.crear(request));
    }

    @GetMapping("/version/{versionDocumentoXsdId}")
    public ResponseEntity<List<MapeoXsdResponse>> listarPorVersion(
            @PathVariable Long versionDocumentoXsdId) {
        return ResponseEntity.ok(mapeoXsdService.listarPorVersion(versionDocumentoXsdId));
    }
}