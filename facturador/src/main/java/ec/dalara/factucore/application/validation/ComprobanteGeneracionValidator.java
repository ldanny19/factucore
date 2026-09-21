package ec.dalara.factucore.application.validation;

import ec.dalara.factucore.application.MessageResolver;
import ec.dalara.factucore.application.contract.request.ComprobanteGeneracionRequest;
import ec.dalara.factucore.application.contract.request.DatoComprobanteRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class ComprobanteGeneracionValidator
        implements ComprobanteValidator {

    private static final String CODIGO_TIPO_DOCUMENTO_REQUERIDO =
            "FACTUCORE.COMPROBANTE.TIPO_DOCUMENTO.REQUERIDO";

    private static final String CODIGO_DATOS_REQUERIDOS =
            "FACTUCORE.COMPROBANTE.DATOS.REQUERIDOS";

    private static final String CODIGO_DATO_DUPLICADO =
            "FACTUCORE.COMPROBANTE.DATO.DUPLICADO";

    private static final String CODIGO_DATO_KEY_REQUERIDO =
            "FACTUCORE.COMPROBANTE.DATO.KEY.REQUERIDA";

    private final MessageResolver messageResolver;

    @Override
    public ComprobanteValidationResult validar(
            ComprobanteGeneracionRequest request
    ) {
        ComprobanteValidationResult resultado =
                new ComprobanteValidationResult(messageResolver);

        if (request == null) {
            resultado.agregarError(
                    "FACTUCORE.COMPROBANTE.REQUEST.REQUERIDO",
                    null
            );
            return resultado;
        }

        validarTipoDocumento(request, resultado);
        validarDatos(request, resultado);

        return resultado;
    }

    private void validarTipoDocumento(
            ComprobanteGeneracionRequest request,
            ComprobanteValidationResult resultado
    ) {
        if (request.getTipoDocumento() == null
                || request.getTipoDocumento().isBlank()) {

            resultado.agregarError(
                    CODIGO_TIPO_DOCUMENTO_REQUERIDO,
                    "tipoDocumento"
            );
        }
    }

    private void validarDatos(
            ComprobanteGeneracionRequest request,
            ComprobanteValidationResult resultado
    ) {
        List<DatoComprobanteRequest> datos = request.getDatos();

        if (datos == null || datos.isEmpty()) {
            resultado.agregarError(
                    CODIGO_DATOS_REQUERIDOS,
                    "datos"
            );
            return;
        }

        Set<String> claves = new HashSet<>();

        for (int i = 0; i < datos.size(); i++) {

            DatoComprobanteRequest dato = datos.get(i);

            if (dato == null) {
                resultado.agregarError(
                        CODIGO_DATO_KEY_REQUERIDO,
                        "datos[" + i + "]"
                );
                continue;
            }

            if (dato.getKey() == null
                    || dato.getKey().isBlank()) {

                resultado.agregarError(
                        CODIGO_DATO_KEY_REQUERIDO,
                        "datos[" + i + "].key"
                );

                continue;
            }

            if (!claves.add(dato.getKey())) {
                resultado.agregarError(
                        CODIGO_DATO_DUPLICADO,
                        "datos[" + i + "].key",
                        dato.getKey()
                );
            }
        }
    }
}