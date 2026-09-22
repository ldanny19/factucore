package ec.dalara.factucore.application.validation;

import ec.dalara.factucore.application.MessageResolver;
import ec.dalara.factucore.application.contract.request.ComprobanteGeneracionRequest;
import ec.dalara.factucore.application.contract.request.DatoComprobanteRequest;
import ec.dalara.factucore.application.port.out.DocumentoDefinitionProvider;
import ec.dalara.factucore.domain.documentoxsd.DocumentDefinitionModel;
import ec.dalara.factucore.domain.shared.MessageCodes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class ComprobanteGeneracionValidator
        implements ComprobanteValidator {

    private final MessageResolver messageResolver;
    private final DocumentoDefinitionProvider documentoDefinitionProvider;
    private final DocumentDefinitionDataValidator documentDefinitionDataValidator;

    @Override
    public ComprobanteValidationResult validar(
            ComprobanteGeneracionRequest request
    ) {
        ComprobanteValidationResult resultado =
                new ComprobanteValidationResult(messageResolver);

        if (request == null) {
            resultado.agregarError(
                    MessageCodes.COMPROBANTE_REQUEST_REQUERIDO,
                    null
            );

            return resultado;
        }

        validarTipoDocumento(request, resultado);
        validarFechaInicio(request, resultado);
        validarDatos(request, resultado);

        if (!resultado.esValido()) {
            return resultado;
        }

        validarDefinicionYDatos(request, resultado);

        return resultado;
    }

    private void validarTipoDocumento(
            ComprobanteGeneracionRequest request,
            ComprobanteValidationResult resultado
    ) {
        if (request.getTipoDocumento() == null
                || request.getTipoDocumento().isBlank()) {

            resultado.agregarError(
                    MessageCodes.COMPROBANTE_TIPO_DOCUMENTO_REQUERIDO,
                    "tipoDocumento"
            );
        }
    }

    private void validarFechaInicio(
            ComprobanteGeneracionRequest request,
            ComprobanteValidationResult resultado
    ) {
        if (request.getFechaInicio() == null) {

            resultado.agregarError(
                    MessageCodes.COMPROBANTE_FECHA_INICIO_REQUERIDA,
                    "fechaInicio"
            );
        }
    }

    private void validarDatos(
            ComprobanteGeneracionRequest request,
            ComprobanteValidationResult resultado
    ) {
        List<DatoComprobanteRequest> datos =
                request.getDatos();

        if (datos == null || datos.isEmpty()) {

            resultado.agregarError(
                    MessageCodes.COMPROBANTE_DATOS_REQUERIDOS,
                    "datos"
            );

            return;
        }

        Set<String> claves = new HashSet<>();

        for (int i = 0; i < datos.size(); i++) {

            DatoComprobanteRequest dato =
                    datos.get(i);

            String campo =
                    "datos[" + i + "]";

            if (dato == null) {

                resultado.agregarError(
                        MessageCodes.COMPROBANTE_DATO_KEY_REQUERIDA,
                        campo
                );

                continue;
            }

            if (dato.getKey() == null
                    || dato.getKey().isBlank()) {

                resultado.agregarError(
                        MessageCodes.COMPROBANTE_DATO_KEY_REQUERIDA,
                        campo + ".key"
                );

                continue;
            }

            if (!claves.add(dato.getKey())) {

                resultado.agregarError(
                        MessageCodes.COMPROBANTE_DATO_DUPLICADO,
                        campo + ".key",
                        dato.getKey()
                );
            }
        }
    }

    private void validarDefinicionYDatos(
            ComprobanteGeneracionRequest request,
            ComprobanteValidationResult resultado
    ) {
        OffsetDateTime fechaInicio =
                request.getFechaInicio();

        if (fechaInicio == null) {
            return;
        }

        LocalDateTime fechaEmision =
                convertirFecha(fechaInicio);

        var definicionOptional =
                documentoDefinitionProvider
                        .obtenerDefinicionVigente(
                                request.getTipoDocumento(),
                                fechaEmision
                        );

        if (definicionOptional.isEmpty()) {

            resultado.agregarError(
                    MessageCodes.COMPROBANTE_DEFINICION_NO_ENCONTRADA,
                    "tipoDocumento",
                    request.getTipoDocumento()
            );

            return;
        }

        DocumentDefinitionModel definicion =
                definicionOptional.get();

        Map<String, Object> datos =
                convertirDatos(request.getDatos());

        documentDefinitionDataValidator.validar(
                definicion,
                datos,
                resultado
        );
    }

    private Map<String, Object> convertirDatos(
            List<DatoComprobanteRequest> datos
    ) {
        Map<String, Object> resultado =
                new LinkedHashMap<>();

        for (DatoComprobanteRequest dato : datos) {
            resultado.put(
                    dato.getKey(),
                    dato.getValue()
            );
        }

        return resultado;
    }

    private LocalDateTime convertirFecha(
            OffsetDateTime fecha
    ) {
        return fecha.toLocalDateTime();
    }
}