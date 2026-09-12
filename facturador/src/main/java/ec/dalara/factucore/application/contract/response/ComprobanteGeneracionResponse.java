package ec.dalara.factucore.application.contract.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComprobanteGeneracionResponse {

    private String idTransaccion;

    private OffsetDateTime fechaInicio;

    private OffsetDateTime fechaFin;

    private Boolean exitoso;

    private String estado;

    private ResultadoResponse resultado;

    private String claveAcceso;

    private String numeroComprobante;

    private String tipoDocumento;

    private String estadoSri;

    private byte[] archivoPdf;
}